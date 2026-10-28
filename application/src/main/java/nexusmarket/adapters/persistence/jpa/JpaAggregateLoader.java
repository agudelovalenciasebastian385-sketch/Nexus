package nexusmarket.adapters.persistence.jpa;

import java.util.ArrayList;
import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.CartItemJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.InventoryJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.ProductJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.ReturnRequestJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import nexusmarket.adapters.persistence.jpa.mappers.CartItemJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.InventoryMovementJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.OrderItemJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.OrderJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.ProductJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.ReturnRequestJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.ShipmentJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.UserJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaCartItemRepository;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaInventoryMovementRepository;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaOrderItemRepository;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaOrderRepository;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaReturnRequestRepository;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaShipmentRepository;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaWarehouseRepository;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.CartItem;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryMovement;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import org.springframework.stereotype.Component;

/**
 * Rebuilds Domain Models whose references span several tables.
 *
 * <p>Entities store their relations as identifiers. Several adapters need the same chains
 * (order, buyer, items, product, seller, warehouse), so they are resolved here once, using the
 * Spring Data repositories, the mappers and the domain rehydration constructors.
 *
 * <p>Cycles are cut as follows:
 * <ul>
 *   <li>Order and Shipment: a base Order (items, no shipments) is built first, its shipments are
 *       built pointing to it, and the final Order receives those shipments. Order checks shipment
 *       ownership by id, so the constructor accepts them.</li>
 *   <li>Order and Invoice: an Order is always loaded without its invoice (see
 *       {@code OrderJpaMapper}); the invoice points to the order.</li>
 *   <li>Buyer.cart/orders, Seller.warehouses/products, Warehouse.inventories and
 *       PhysicalProduct.inventories are inverse lists and are not loaded; each one is read
 *       through its own output port.</li>
 * </ul>
 *
 * <p>A missing referenced row means the database is inconsistent and raises
 * {@link IllegalStateException}.
 */
@Component
public class JpaAggregateLoader {

    private final SpringDataJpaUserRepository userRepository;
    private final SpringDataJpaWarehouseRepository warehouseRepository;
    private final SpringDataJpaProductRepository productRepository;
    private final SpringDataJpaInventoryMovementRepository inventoryMovementRepository;
    private final SpringDataJpaCartItemRepository cartItemRepository;
    private final SpringDataJpaOrderRepository orderRepository;
    private final SpringDataJpaOrderItemRepository orderItemRepository;
    private final SpringDataJpaShipmentRepository shipmentRepository;
    private final SpringDataJpaReturnRequestRepository returnRequestRepository;

    public JpaAggregateLoader(SpringDataJpaUserRepository userRepository,
                              SpringDataJpaWarehouseRepository warehouseRepository,
                              SpringDataJpaProductRepository productRepository,
                              SpringDataJpaInventoryMovementRepository inventoryMovementRepository,
                              SpringDataJpaCartItemRepository cartItemRepository,
                              SpringDataJpaOrderRepository orderRepository,
                              SpringDataJpaOrderItemRepository orderItemRepository,
                              SpringDataJpaShipmentRepository shipmentRepository,
                              SpringDataJpaReturnRequestRepository returnRequestRepository) {
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.shipmentRepository = shipmentRepository;
        this.returnRequestRepository = returnRequestRepository;
    }

    // ---------------------------------------------------------------- users

    public <T extends User> T requireUser(String id, Class<T> type) {
        User user = userRepository.findById(id)
                .map(UserJpaMapper::toDomain)
                .orElseThrow(() -> dangling(type.getSimpleName(), id));
        if (!type.isInstance(user)) {
            throw new IllegalStateException("User " + id + " is a " + user.getRole().getCode()
                    + ", expected " + type.getSimpleName() + ".");
        }
        return type.cast(user);
    }

    // ----------------------------------------------------------- warehouses

    public Warehouse toWarehouse(WarehouseJpaEntity entity) {
        Seller seller = entity.getSellerId() == null ? null : requireUser(entity.getSellerId(), Seller.class);
        return WarehouseJpaMapper.toDomain(entity, seller);
    }

    public Warehouse requireWarehouse(String id) {
        return warehouseRepository.findById(id)
                .map(this::toWarehouse)
                .orElseThrow(() -> dangling("Warehouse", id));
    }

    // ------------------------------------------------------------- products

    public Product toProduct(ProductJpaEntity entity) {
        return ProductJpaMapper.toDomain(entity, requireUser(entity.getSellerId(), Seller.class));
    }

    public Product requireProduct(String id) {
        return productRepository.findById(id)
                .map(this::toProduct)
                .orElseThrow(() -> dangling("Product", id));
    }

    public PhysicalProduct requirePhysicalProduct(String id) {
        Product product = requireProduct(id);
        if (!(product instanceof PhysicalProduct physicalProduct)) {
            throw new IllegalStateException("Product " + id + " is not a physical product.");
        }
        return physicalProduct;
    }

    // ------------------------------------------------------------ inventory

    public Inventory toInventory(InventoryJpaEntity entity, PhysicalProduct product, Warehouse warehouse) {
        List<InventoryMovement> movements = inventoryMovementRepository
                .findByProductIdAndWarehouseIdOrderByIdAsc(entity.getId().getProductId(),
                        entity.getId().getWarehouseId())
                .stream()
                .map(InventoryMovementJpaMapper::toDomain)
                .toList();
        return InventoryJpaMapper.toDomain(entity, product, warehouse, movements);
    }

    // ----------------------------------------------------------------- cart

    /**
     * Loads the items of a cart through the CartItem rehydration factory, so items whose product
     * was suspended, discontinued or lost the variant after being added are kept.
     */
    public List<CartItem> loadCartItems(String cartId) {
        List<CartItem> items = new ArrayList<>();
        for (CartItemJpaEntity entity : cartItemRepository.findByCartIdOrderByIdAsc(cartId)) {
            items.add(CartItemJpaMapper.toDomain(entity, requireProduct(entity.getProductId())));
        }
        return items;
    }

    // --------------------------------------------------------------- orders

    public Order toOrder(OrderJpaEntity entity) {
        return toOrder(entity, requireUser(entity.getBuyerId(), Buyer.class));
    }

    public Order toOrder(OrderJpaEntity entity, Buyer buyer) {
        List<OrderItem> items = orderItemRepository.findByOrderIdOrderByIdAsc(entity.getId()).stream()
                .map(this::toOrderItem)
                .toList();
        Order base = OrderJpaMapper.toDomain(entity, buyer, items, null);
        List<Shipment> shipments = shipmentRepository.findByOrderIdOrderByCreatedAtAsc(entity.getId()).stream()
                .map(shipment -> toShipment(shipment, base))
                .toList();
        if (shipments.isEmpty()) {
            return base;
        }
        return OrderJpaMapper.toDomain(entity, buyer, items, shipments);
    }

    public Order requireOrder(String id) {
        return orderRepository.findById(id)
                .map(this::toOrder)
                .orElseThrow(() -> dangling("Order", id));
    }

    private OrderItem toOrderItem(OrderItemJpaEntity entity) {
        Warehouse originWarehouse = entity.getOriginWarehouseId() == null
                ? null
                : requireWarehouse(entity.getOriginWarehouseId());
        return OrderItemJpaMapper.toDomain(entity, requireProduct(entity.getProductId()), originWarehouse);
    }

    // ------------------------------------------------------------ shipments

    public Shipment toShipment(ShipmentJpaEntity entity, Order order) {
        return ShipmentJpaMapper.toDomain(entity, order, requireWarehouse(entity.getOriginWarehouseId()),
                requireUser(entity.getLogisticsOperatorId(), LogisticsOperator.class));
    }

    // -------------------------------------------------------------- returns

    public ReturnRequest toReturnRequest(ReturnRequestJpaEntity entity) {
        return ReturnRequestJpaMapper.toDomain(entity, requireOrder(entity.getOrderId()));
    }

    public ReturnRequest requireReturnRequest(String id) {
        return returnRequestRepository.findById(id)
                .map(this::toReturnRequest)
                .orElseThrow(() -> dangling("ReturnRequest", id));
    }

    private static IllegalStateException dangling(String type, String id) {
        return new IllegalStateException("Referenced " + type + " " + id + " does not exist in the database.");
    }
}
