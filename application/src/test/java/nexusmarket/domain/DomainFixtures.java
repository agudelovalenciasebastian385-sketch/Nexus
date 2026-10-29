package nexusmarket.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.DigitalProduct;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Supervisor;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.UserStatus;
import nexusmarket.domain.valueobjects.Variant;
import nexusmarket.domain.valueobjects.WarehouseType;

/**
 * Builders of valid domain objects for unit tests, so each test only prepares what it checks.
 * Every call returns new objects with unique ids.
 */
public final class DomainFixtures {

    public static final Address ADDRESS = new Address("Calle 10 # 20-30", null, "Medellin", "Antioquia", "Colombia");
    public static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 1, 10, 0);
    public static final String PASSWORD_HASH = "$2a$10$hash";

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private DomainFixtures() {
    }

    public static String id(String prefix) {
        return prefix + "-" + SEQUENCE.incrementAndGet();
    }

    // ------------------------------------------------------------------ users

    public static Buyer buyer() {
        return buyer(UserStatus.ACTIVE, CommercialStatus.ENABLED);
    }

    public static Buyer buyer(UserStatus status, CommercialStatus commercialStatus) {
        String id = id("buyer");
        return new Buyer(id, id("doc"), "Maria Gomez", id + "@gmail.com", PASSWORD_HASH, status, ADDRESS,
                commercialStatus);
    }

    public static Seller seller() {
        String id = id("seller");
        return new Seller(id, id("doc"), "Tech Store", id + "@gmail.com", PASSWORD_HASH, UserStatus.ACTIVE);
    }

    public static Administrator administrator() {
        String id = id("admin");
        return new Administrator(id, id("doc"), "Admin", id + "@nexusmarket.com", PASSWORD_HASH, UserStatus.ACTIVE);
    }

    public static LogisticsOperator operator() {
        String id = id("operator");
        return new LogisticsOperator(id, id("doc"), "Laura Operator", id + "@nexusmarket.com", PASSWORD_HASH,
                UserStatus.ACTIVE);
    }

    public static Supervisor supervisor() {
        String id = id("supervisor");
        return new Supervisor(id, id("doc"), "Pedro Supervisor", id + "@nexusmarket.com", PASSWORD_HASH,
                UserStatus.ACTIVE);
    }

    // ------------------------------------------------------------------ warehouses and products

    public static Warehouse sellerWarehouse(Seller seller) {
        return new Warehouse(id("warehouse"), "Seller Warehouse", ADDRESS, WarehouseType.SELLER, seller);
    }

    public static Warehouse marketplaceWarehouse() {
        return new Warehouse(id("warehouse"), "Marketplace Warehouse", ADDRESS, WarehouseType.MARKETPLACE, null);
    }

    public static PhysicalProduct physicalProduct(Seller seller, ProductStatus status, Variant... variants) {
        return new PhysicalProduct(id("product"), "Wireless Mouse", "Ergonomic mouse", new BigDecimal("100.00"), seller,
                List.of(variants), status);
    }

    public static PhysicalProduct publishedPhysical(Seller seller, Variant... variants) {
        return physicalProduct(seller, ProductStatus.PUBLISHED, variants);
    }

    public static DigitalProduct publishedDigital(Seller seller) {
        return new DigitalProduct(id("product"), "E-book", "Java guide", new BigDecimal("50.00"), seller, List.of(),
                ProductStatus.PUBLISHED);
    }

    // ------------------------------------------------------------------ orders

    public static OrderItem physicalItem(PhysicalProduct product, Warehouse warehouse, int quantity) {
        return new OrderItem(product, null, quantity, product.getPrice(), warehouse);
    }

    public static OrderItem digitalItem(Product product, int quantity) {
        return new OrderItem(product, null, quantity, product.getPrice(), null);
    }

    /** New order (PENDING_PAYMENT) with one physical item of 2 units at 100.00. */
    public static Order physicalOrder(Buyer buyer) {
        Seller seller = seller();
        return new Order(id("order"), buyer, List.of(physicalItem(publishedPhysical(seller), sellerWarehouse(seller), 2)),
                NOW);
    }

    /** New order (PENDING_PAYMENT) with one digital item. */
    public static Order digitalOrder(Buyer buyer) {
        return new Order(id("order"), buyer, List.of(digitalItem(publishedDigital(seller()), 1)), NOW);
    }

    /** Rehydrated physical order in the given status. */
    public static Order physicalOrder(OrderStatus status) {
        Seller seller = seller();
        return new Order(id("order"), buyer(),
                List.of(physicalItem(publishedPhysical(seller), sellerWarehouse(seller), 2)), status, NOW, null, null);
    }
}
