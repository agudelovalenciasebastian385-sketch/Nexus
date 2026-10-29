package nexusmarket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import nexusmarket.adapters.persistence.jpa.InventoryJpaAdapter;
import nexusmarket.adapters.persistence.jpa.OrderJpaAdapter;
import nexusmarket.domain.exceptions.PaymentRejectedException;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.in.AdministratorPort;
import nexusmarket.domain.ports.in.BuyerPort;
import nexusmarket.domain.ports.in.LogisticsOperatorPort;
import nexusmarket.domain.ports.in.PublicAccessPort;
import nexusmarket.domain.ports.in.SellerPort;
import nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import nexusmarket.domain.ports.out.InventoryRepositoryPort;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.MovementType;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test of the transaction boundary (use case adapters) and of the optimistic locking of
 * inventory, against the docker-compose MySQL and MongoDB containers, in the separate databases of the
 * {@code test} profile (nexusmarket_test).
 *
 * <p>Calls go through the role ports, which are the transactional boundary. Two {@code @Primary}
 * decorators of the output ports delegate to the real adapters and let a test inject a failure after
 * the inventory reservation, or hold two concurrent reservations until both have read the inventory.
 */
@SpringBootTest
@ActiveProfiles("test")
class TransactionalConsistencyIT {

    @Autowired private PublicAccessPort publicAccessPort;
    @Autowired private AdministratorPort administratorPort;
    @Autowired private SellerPort sellerPort;
    @Autowired private LogisticsOperatorPort logisticsOperatorPort;
    @Autowired private BuyerPort buyerPort;
    @Autowired private UserRepositoryPort userRepositoryPort;
    @Autowired private ProductRepositoryPort productRepositoryPort;
    @Autowired private WarehouseRepositoryPort warehouseRepositoryPort;
    @Autowired private InventoryRepositoryPort inventoryRepositoryPort;
    @Autowired private OrderRepositoryPort orderRepositoryPort;
    @Autowired private AuditLogRepositoryPort auditLogRepositoryPort;
    @Autowired private FailingOrderRepository failingOrderRepository;
    @Autowired private BarrierInventoryRepository barrierInventoryRepository;

    private static final Address ADDRESS = new Address("Calle 10 # 20-30", null, "Medellin", "Antioquia", "Colombia");

    private String run;
    private User admin;
    private Seller seller;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        run = UUID.randomUUID().toString().substring(0, 8);
        admin = userRepositoryPort.findByEmail("admin@nexusmarket.com").orElseThrow();
        seller = administratorPort.registerSeller(admin, "itest-s-" + run, "ITest Seller " + run,
                "itest-seller-" + run + "@nexusmarket.test", "Seller123*", "ITest Warehouse " + run, ADDRESS);
        warehouse = seller.getWarehouses().get(0);
    }

    @AfterEach
    void disarm() {
        failingOrderRepository.failOnSave = false;
        barrierInventoryRepository.barrier = null;
    }

    /** (a) + (c): a failure after the reservation rolls everything back, and no audit record remains. */
    @Test
    void failureAfterReservationRollsBackReservationAndAudit() {
        Product product = publishedProductWithStock("A", new BigDecimal("50.00"), 1);
        Buyer buyer = registerBuyer("a");
        buyerPort.addItemToCart(buyer, product.getId(), null, 1);

        failingOrderRepository.failOnSave = true;
        assertThatThrownBy(() -> buyerPort.confirmOrder(buyer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("simulated failure");

        Inventory inventory = inventory(product);
        assertThat(inventory.getQuantity()).isEqualTo(1);
        assertThat(inventory.getReservedQuantity()).isZero();
        assertThat(inventory.getMovements()).noneMatch(m -> m.getType() == MovementType.RESERVATION);
        assertThat(buyerPort.consultCart(buyer, buyer.getId()).getItems()).hasSize(1);
        assertThat(orderRepositoryPort.findByBuyer(buyer)).isEmpty();

        assertThat(actions(auditLogRepositoryPort.findByEntity(AuditEntityType.INVENTORY, inventoryKey(product))))
                .contains(AuditAction.INVENTORY_INITIAL_STOCK_REGISTERED)
                .doesNotContain(AuditAction.INVENTORY_RESERVED);
        assertThat(actions(auditLogRepositoryPort.findByPerformedBy(buyer)))
                .doesNotContain(AuditAction.INVENTORY_RESERVED, AuditAction.ORDER_CONFIRMED);
    }

    /** (b): two simultaneous reservations of the last unit: one commits, the other is rejected. */
    @Test
    void concurrentReservationsOfLastUnitOnlyOneSucceeds() throws Exception {
        Product product = publishedProductWithStock("B", new BigDecimal("50.00"), 1);
        User operator = administratorPort.registerStaffUser(admin, SystemRole.LOGISTICS_OPERATOR, "itest-o-" + run,
                "ITest Operator " + run, "itest-operator-" + run + "@nexusmarket.test", "Operator123*");

        barrierInventoryRepository.barrier = new CyclicBarrier(2);
        Callable<Inventory> reserve = () ->
                logisticsOperatorPort.reserveInventory(operator, product.getId(), warehouse.getId(), 1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<Inventory>> results = List.of(pool.submit(reserve), pool.submit(reserve));
        pool.shutdown();
        assertThat(pool.awaitTermination(60, TimeUnit.SECONDS)).isTrue();

        int succeeded = 0;
        List<Throwable> failures = new ArrayList<>();
        for (Future<Inventory> result : results) {
            try {
                result.get();
                succeeded++;
            } catch (ExecutionException e) {
                failures.add(e.getCause());
            }
        }
        assertThat(succeeded).isEqualTo(1);
        assertThat(failures).singleElement().isInstanceOf(OptimisticLockingFailureException.class);

        Inventory inventory = inventory(product);
        assertThat(inventory.getQuantity()).isZero();
        assertThat(inventory.getReservedQuantity()).isEqualTo(1);
        assertThat(inventory.getMovements()).filteredOn(m -> m.getType() == MovementType.RESERVATION).hasSize(1);
        assertThat(actions(auditLogRepositoryPort.findByEntity(AuditEntityType.INVENTORY, inventoryKey(product))))
                .filteredOn(action -> action == AuditAction.INVENTORY_RESERVED).hasSize(1);
    }

    /** (d): a rejected payment keeps the order PENDING_PAYMENT and its PAYMENT_REJECTED audit record. */
    @Test
    void rejectedPaymentKeepsOrderPendingAndAuditsRejection() {
        Product product = publishedProductWithStock("D", new BigDecimal("5000.00"), 2);
        Buyer buyer = registerBuyer("d");
        buyerPort.addItemToCart(buyer, product.getId(), null, 1);
        Order order = buyerPort.confirmOrder(buyer);

        assertThatThrownBy(() -> buyerPort.validatePayment(buyer, order.getId()))
                .isInstanceOf(PaymentRejectedException.class);

        assertThat(orderRepositoryPort.findById(order.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(actions(auditLogRepositoryPort.findByEntity(AuditEntityType.ORDER, order.getId())))
                .contains(AuditAction.PAYMENT_REJECTED)
                .doesNotContain(AuditAction.PAYMENT_APPROVED);
    }

    // ------------------------------------------------------------------ helpers

    private Product publishedProductWithStock(String tag, BigDecimal price, int stock) {
        Product product = sellerPort.registerProduct(seller, ProductType.PHYSICAL, "ITest product " + tag + " " + run,
                "Integration test product", price, List.of());
        sellerPort.publishProduct(seller, product.getId());
        sellerPort.registerInitialStock(seller, product.getId(), warehouse.getId(), stock);
        return product;
    }

    private Buyer registerBuyer(String tag) {
        return publicAccessPort.registerBuyerUser("itest-b" + tag + "-" + run, "ITest Buyer " + run,
                "itest-buyer-" + tag + "-" + run + "@nexusmarket.test", "Buyer123*", ADDRESS);
    }

    private Inventory inventory(Product product) {
        PhysicalProduct physical = (PhysicalProduct) productRepositoryPort.findById(product.getId()).orElseThrow();
        Warehouse stored = warehouseRepositoryPort.findById(warehouse.getId()).orElseThrow();
        return inventoryRepositoryPort.findByProductAndWarehouse(physical, stored).orElseThrow();
    }

    /** Audit entity id of an inventory: "productId:warehouseId". */
    private String inventoryKey(Product product) {
        return product.getId() + ":" + warehouse.getId();
    }

    private static List<AuditAction> actions(List<AuditLog> logs) {
        return logs.stream().map(AuditLog::getAction).toList();
    }

    // ------------------------------------------------------------------ test decorators

    @TestConfiguration
    static class DecoratorConfig {

        @Bean
        @Primary
        FailingOrderRepository failingOrderRepository(OrderJpaAdapter delegate) {
            return new FailingOrderRepository(delegate);
        }

        @Bean
        @Primary
        BarrierInventoryRepository barrierInventoryRepository(InventoryJpaAdapter delegate) {
            return new BarrierInventoryRepository(delegate);
        }
    }

    /** Delegates to the real adapter; when armed, save fails as if the order could not be stored. */
    static class FailingOrderRepository implements OrderRepositoryPort {

        private final OrderRepositoryPort delegate;
        volatile boolean failOnSave;

        FailingOrderRepository(OrderRepositoryPort delegate) {
            this.delegate = delegate;
        }

        @Override
        public Order save(Order order) {
            if (failOnSave) {
                throw new IllegalStateException("simulated failure while saving the order");
            }
            return delegate.save(order);
        }

        @Override
        public void update(Order order) {
            delegate.update(order);
        }

        @Override
        public Optional<Order> findById(String id) {
            return delegate.findById(id);
        }

        @Override
        public List<Order> findByBuyer(Buyer buyer) {
            return delegate.findByBuyer(buyer);
        }

        @Override
        public List<Order> findByCreatedAtBetween(java.time.LocalDateTime from, java.time.LocalDateTime toExclusive) {
            return delegate.findByCreatedAtBetween(from, toExclusive);
        }
    }

    /**
     * Delegates to the real adapter; when armed, update waits until both concurrent transactions
     * reach it, so both have read the same inventory version before either commits.
     */
    static class BarrierInventoryRepository implements InventoryRepositoryPort {

        private final InventoryRepositoryPort delegate;
        volatile CyclicBarrier barrier;

        BarrierInventoryRepository(InventoryRepositoryPort delegate) {
            this.delegate = delegate;
        }

        @Override
        public Inventory save(Inventory inventory) {
            return delegate.save(inventory);
        }

        @Override
        public void update(Inventory inventory) {
            CyclicBarrier current = barrier;
            if (current != null) {
                try {
                    current.await(30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new IllegalStateException("Barrier not reached", e);
                }
            }
            delegate.update(inventory);
        }

        @Override
        public Optional<Inventory> findByProductAndWarehouse(PhysicalProduct product, Warehouse warehouse) {
            return delegate.findByProductAndWarehouse(product, warehouse);
        }

        @Override
        public List<Inventory> findByProduct(PhysicalProduct product) {
            return delegate.findByProduct(product);
        }

        @Override
        public List<Inventory> findByWarehouse(Warehouse warehouse) {
            return delegate.findByWarehouse(warehouse);
        }

        @Override
        public List<Inventory> findAll() {
            return delegate.findAll();
        }
    }
}
