package nexusmarket.adapters.persistence.memory;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static nexusmarket.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.RefundStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** The semantics of the JPA and MongoDB adapters that the memory adapters must keep. */
class MemoryAdaptersTest {

    private static final LocalDateTime FROM = NOW;
    private static final LocalDateTime TO = NOW.plusDays(1);

    private static Order orderAt(Buyer buyer, LocalDateTime createdAt) {
        return new Order(DomainFixtures.id("order"), buyer, DomainFixtures.physicalOrder(buyer).getItems(),
                OrderStatus.PENDING_PAYMENT, createdAt, null, null);
    }

    private static Shipment shipmentAt(Order order, LocalDateTime createdAt) {
        Seller seller = DomainFixtures.seller();
        return new Shipment(DomainFixtures.id("shipment"), order, DomainFixtures.sellerWarehouse(seller), ADDRESS,
                DomainFixtures.operator(), createdAt);
    }

    @Nested
    class Users {

        private final UserMemoryAdapter users = new UserMemoryAdapter();
        private final Buyer buyer = DomainFixtures.buyer();

        @Test
        void findsAUserByEmail() {
            users.save(buyer);

            assertThat(users.findByEmail(buyer.getEmail())).contains(buyer);
        }

        @Test
        void aNullEmailFindsNothing() {
            users.save(buyer);

            assertThat(users.findByEmail(null)).isEmpty();
        }

        @Test
        void aNullIdFindsNothing() {
            assertThat(users.findById(null)).isEmpty();
        }

        @Test
        void aNullEmailDoesNotExist() {
            assertThat(users.existsByEmail(null)).isFalse();
        }

        @Test
        void aNullIdentificationDoesNotExist() {
            assertThat(users.existsByIdentificationNumber(null)).isFalse();
        }

        @Test
        void detectsAnExistingIdentification() {
            users.save(buyer);

            assertThat(users.existsByIdentificationNumber(buyer.getIdentificationNumber())).isTrue();
        }

        @Test
        void detectsAnExistingRole() {
            users.save(buyer);

            assertThat(users.existsByRole(SystemRole.BUYER)).isTrue();
        }

        @Test
        void aRoleWithoutUsersDoesNotExist() {
            users.save(buyer);

            assertThat(users.existsByRole(SystemRole.ADMINISTRATOR)).isFalse();
        }

        @Test
        void aNullRoleDoesNotExist() {
            assertThat(users.existsByRole(null)).isFalse();
        }

        @Test
        void savingTheSameIdReplacesTheUser() {
            users.save(buyer);
            users.update(buyer);

            assertThat(users.existsByRole(SystemRole.BUYER)).isTrue();
        }
    }

    @Nested
    class BuyersAndSellers {

        private final UserMemoryAdapter users = new UserMemoryAdapter();
        private final BuyerMemoryAdapter buyers = new BuyerMemoryAdapter(users);
        private final SellerMemoryAdapter sellers = new SellerMemoryAdapter(users);
        private final Buyer buyer = DomainFixtures.buyer();
        private final Seller seller = DomainFixtures.seller();

        @Test
        void aSavedBuyerIsAlsoAUser() {
            buyers.save(buyer);

            assertThat(users.findById(buyer.getId())).contains(buyer);
        }

        @Test
        void aBuyerIsFoundById() {
            buyers.save(buyer);

            assertThat(buyers.findById(buyer.getId())).contains(buyer);
        }

        @Test
        void theIdOfASellerFindsNoBuyer() {
            sellers.save(seller);

            assertThat(buyers.findById(seller.getId())).isEmpty();
        }

        @Test
        void theIdOfABuyerFindsNoSeller() {
            buyers.save(buyer);

            assertThat(sellers.findById(buyer.getId())).isEmpty();
        }
    }

    @Nested
    class WarehousesAndProducts {

        private final WarehouseMemoryAdapter warehouses = new WarehouseMemoryAdapter();
        private final ProductMemoryAdapter products = new ProductMemoryAdapter();
        private final Seller seller = DomainFixtures.seller();

        @Test
        void listsTheWarehousesOfASeller() {
            Warehouse own = warehouses.save(DomainFixtures.sellerWarehouse(seller));
            warehouses.save(DomainFixtures.sellerWarehouse(DomainFixtures.seller()));

            assertThat(warehouses.findBySeller(seller)).containsExactly(own);
        }

        @Test
        void aMarketplaceWarehouseBelongsToNoSeller() {
            warehouses.save(DomainFixtures.marketplaceWarehouse());

            assertThat(warehouses.findBySeller(seller)).isEmpty();
        }

        @Test
        void aNullSellerHasNoWarehouses() {
            assertThat(warehouses.findBySeller(null)).isEmpty();
        }

        @Test
        void listsAllTheWarehousesInInsertionOrder() {
            Warehouse first = warehouses.save(DomainFixtures.marketplaceWarehouse());
            Warehouse second = warehouses.save(DomainFixtures.marketplaceWarehouse());

            assertThat(warehouses.findAll()).containsExactly(first, second);
        }

        @Test
        void changingTheReturnedListDoesNotChangeTheStore() {
            warehouses.save(DomainFixtures.marketplaceWarehouse());

            warehouses.findAll().clear();

            assertThat(warehouses.findAll()).hasSize(1);
        }

        @Test
        void filtersProductsByStatus() {
            PhysicalProduct published = DomainFixtures.publishedPhysical(seller);
            products.save(published);
            products.save(DomainFixtures.physicalProduct(seller, ProductStatus.DRAFT));

            assertThat(products.findByStatus(ProductStatus.PUBLISHED)).containsExactly(published);
        }

        @Test
        void filtersProductsBySeller() {
            PhysicalProduct own = DomainFixtures.publishedPhysical(seller);
            products.save(own);
            products.save(DomainFixtures.publishedPhysical(DomainFixtures.seller()));

            assertThat(products.findBySeller(seller)).containsExactly(own);
        }

        @Test
        void aNullSellerHasNoProducts() {
            assertThat(products.findBySeller(null)).isEmpty();
        }
    }

    @Nested
    class Inventories {

        private final InventoryMemoryAdapter inventories = new InventoryMemoryAdapter();
        private final Seller seller = DomainFixtures.seller();
        private final PhysicalProduct product = DomainFixtures.publishedPhysical(seller);
        private final Warehouse warehouse = DomainFixtures.sellerWarehouse(seller);

        @Test
        void isIdentifiedByProductAndWarehouse() {
            Inventory inventory = inventories.save(new Inventory(product, warehouse, 5));

            assertThat(inventories.findByProductAndWarehouse(product, warehouse)).contains(inventory);
        }

        @Test
        void anotherWarehouseIsAnotherRecord() {
            inventories.save(new Inventory(product, warehouse, 5));
            inventories.save(new Inventory(product, DomainFixtures.sellerWarehouse(seller), 5));

            assertThat(inventories.findByProduct(product)).hasSize(2);
        }

        @Test
        void updatingKeepsASingleRecord() {
            Inventory inventory = inventories.save(new Inventory(product, warehouse, 5));

            inventories.update(inventory);

            assertThat(inventories.findAll()).hasSize(1);
        }

        @Test
        void filtersByWarehouse() {
            Inventory inventory = inventories.save(new Inventory(product, warehouse, 5));
            inventories.save(new Inventory(product, DomainFixtures.sellerWarehouse(seller), 5));

            assertThat(inventories.findByWarehouse(warehouse)).containsExactly(inventory);
        }

        @Test
        void nullArgumentsFindNothing() {
            assertThat(inventories.findByProductAndWarehouse(null, warehouse)).isEmpty();
        }

        @Test
        void aNullProductHasNoRecords() {
            assertThat(inventories.findByProduct(null)).isEmpty();
        }

        @Test
        void aNullWarehouseHasNoRecords() {
            assertThat(inventories.findByWarehouse(null)).isEmpty();
        }
    }

    @Test
    void aCartIsFoundByItsBuyer() {
        CartMemoryAdapter carts = new CartMemoryAdapter();
        Buyer buyer = DomainFixtures.buyer();
        Cart cart = carts.save(new Cart("cart-1", buyer));

        assertThat(carts.findByBuyer(buyer)).contains(cart);
    }

    @Test
    void aNullBuyerHasNoCart() {
        assertThat(new CartMemoryAdapter().findByBuyer(null)).isEmpty();
    }

    @Nested
    class Orders {

        private final OrderMemoryAdapter orders = new OrderMemoryAdapter();
        private final Buyer buyer = DomainFixtures.buyer();

        @Test
        void listsTheOrdersOfABuyerOldestFirst() {
            Order newer = orders.save(orderAt(buyer, NOW.plusHours(2)));
            Order older = orders.save(orderAt(buyer, NOW));

            assertThat(orders.findByBuyer(buyer)).containsExactly(older, newer);
        }

        @Test
        void doesNotListTheOrdersOfAnotherBuyer() {
            orders.save(orderAt(DomainFixtures.buyer(), NOW));

            assertThat(orders.findByBuyer(buyer)).isEmpty();
        }

        @Test
        void aNullBuyerHasNoOrders() {
            assertThat(orders.findByBuyer(null)).isEmpty();
        }

        @Test
        void theLowerLimitOfTheRangeIsInclusive() {
            Order atStart = orders.save(orderAt(buyer, FROM));

            assertThat(orders.findByCreatedAtBetween(FROM, TO)).containsExactly(atStart);
        }

        @Test
        void theUpperLimitOfTheRangeIsExclusive() {
            orders.save(orderAt(buyer, TO));

            assertThat(orders.findByCreatedAtBetween(FROM, TO)).isEmpty();
        }

        @Test
        void aMomentBeforeTheRangeIsOutside() {
            orders.save(orderAt(buyer, FROM.minusNanos(1)));

            assertThat(orders.findByCreatedAtBetween(FROM, TO)).isEmpty();
        }

        @Test
        void aNullLimitFindsNothing() {
            orders.save(orderAt(buyer, FROM));

            assertThat(orders.findByCreatedAtBetween(null, TO)).isEmpty();
        }
    }

    @Test
    void invoicesAreFoundByOrder() {
        InvoiceMemoryAdapter invoices = new InvoiceMemoryAdapter();
        Order order = DomainFixtures.physicalOrder(DomainFixtures.buyer());
        Invoice invoice = invoices.save(new Invoice("invoice-1", order, NOW));

        assertThat(invoices.findByOrder(order)).contains(invoice);
    }

    @Test
    void anOrderWithoutInvoiceIsDetected() {
        assertThat(new InvoiceMemoryAdapter().existsByOrder(DomainFixtures.physicalOrder(DomainFixtures.buyer()))).isFalse();
    }

    @Test
    void aNullOrderHasNoInvoice() {
        assertThat(new InvoiceMemoryAdapter().existsByOrder(null)).isFalse();
    }

    @Test
    void theInvoiceRangeHasAnExclusiveUpperLimit() {
        InvoiceMemoryAdapter invoices = new InvoiceMemoryAdapter();
        Order order = DomainFixtures.physicalOrder(DomainFixtures.buyer());
        Invoice inside = invoices.save(new Invoice("invoice-1", order, FROM));
        invoices.save(new Invoice("invoice-2", order, TO));

        assertThat(invoices.findByIssuedAtBetween(FROM, TO)).containsExactly(inside);
    }

    @Test
    void shipmentsOfAnOrderComeOldestFirst() {
        ShipmentMemoryAdapter shipments = new ShipmentMemoryAdapter();
        Order order = DomainFixtures.physicalOrder(DomainFixtures.buyer());
        Shipment newer = shipments.save(shipmentAt(order, NOW.plusHours(1)));
        Shipment older = shipments.save(shipmentAt(order, NOW));

        assertThat(shipments.findByOrder(order)).containsExactly(older, newer);
    }

    @Test
    void theShipmentRangeHasAnExclusiveUpperLimit() {
        ShipmentMemoryAdapter shipments = new ShipmentMemoryAdapter();
        Order order = DomainFixtures.physicalOrder(DomainFixtures.buyer());
        Shipment inside = shipments.save(shipmentAt(order, FROM));
        shipments.save(shipmentAt(order, TO));

        assertThat(shipments.findByCreatedAtBetween(FROM, TO)).containsExactly(inside);
    }

    @Test
    void returnRequestsAreFoundByOrder() {
        ReturnRequestMemoryAdapter returns = new ReturnRequestMemoryAdapter();
        Order order = DomainFixtures.physicalOrder(DomainFixtures.buyer());
        ReturnRequest returnRequest = returns.save(new ReturnRequest("return-1", order, "Wrong color"));

        assertThat(returns.findByOrder(order)).containsExactly(returnRequest);
    }

    @Nested
    class Refunds {

        private final RefundMemoryAdapter refunds = new RefundMemoryAdapter();
        private final ReturnRequest returnRequest = new ReturnRequest("return-1",
                DomainFixtures.physicalOrder(DomainFixtures.buyer()), "Wrong color");

        private Refund processedAt(String id, LocalDateTime processedAt) {
            return refunds.save(new Refund(id, returnRequest, new BigDecimal("10.00"), RefundStatus.PROCESSED, processedAt));
        }

        @Test
        void isFoundByItsReturnRequest() {
            Refund refund = refunds.save(new Refund("refund-1", returnRequest, new BigDecimal("10.00")));

            assertThat(refunds.findByReturnRequest(returnRequest)).contains(refund);
        }

        @Test
        void aPendingRefundIsInNoRange() {
            refunds.save(new Refund("refund-1", returnRequest, new BigDecimal("10.00")));

            assertThat(refunds.findByProcessedAtBetween(FROM, TO)).isEmpty();
        }

        @Test
        void theRangeHasAnExclusiveUpperLimit() {
            Refund inside = processedAt("refund-1", FROM);
            processedAt("refund-2", TO);

            assertThat(refunds.findByProcessedAtBetween(FROM, TO)).containsExactly(inside);
        }
    }

    @Nested
    class AuditLogs {

        private final AuditLogMemoryAdapter audit = new AuditLogMemoryAdapter();
        private final Buyer buyer = DomainFixtures.buyer();

        private AuditLog logAt(String id, LocalDateTime occurredAt) {
            return audit.save(new AuditLog(id, AuditAction.USER_LOGGED_IN, buyer, AuditEntityType.USER, buyer.getId(),
                    occurredAt, Map.of()));
        }

        @Test
        void listsOldestFirst() {
            AuditLog newer = logAt("a-1", NOW.plusHours(1));
            AuditLog older = logAt("a-2", NOW);

            assertThat(audit.findAll()).containsExactly(older, newer);
        }

        @Test
        void filtersByPerformer() {
            AuditLog log = logAt("a-1", NOW);

            assertThat(audit.findByPerformedBy(buyer)).containsExactly(log);
        }

        @Test
        void filtersByEntity() {
            AuditLog log = logAt("a-1", NOW);

            assertThat(audit.findByEntity(AuditEntityType.USER, buyer.getId())).containsExactly(log);
        }

        @Test
        void anotherEntityTypeWithTheSameIdIsNotReturned() {
            logAt("a-1", NOW);

            assertThat(audit.findByEntity(AuditEntityType.ORDER, buyer.getId())).isEmpty();
        }

        @Test
        void aNullPerformerHasNoRecords() {
            assertThat(audit.findByPerformedBy(null)).isEmpty();
        }

        @Test
        void aNullEntityHasNoRecords() {
            assertThat(audit.findByEntity(null, "x")).isEmpty();
        }
    }

    @Test
    void concurrentWritesLoseNothing() throws Exception {
        WarehouseMemoryAdapter warehouses = new WarehouseMemoryAdapter();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<?>> tasks = new ArrayList<>();
        for (int thread = 0; thread < 8; thread++) {
            tasks.add(pool.submit(() -> {
                for (int i = 0; i < 500; i++) {
                    warehouses.save(DomainFixtures.marketplaceWarehouse());
                    warehouses.findAll();
                }
            }));
        }
        for (Future<?> task : tasks) {
            task.get();
        }
        pool.shutdown();

        assertThat(warehouses.findAll()).hasSize(4000);
    }
}
