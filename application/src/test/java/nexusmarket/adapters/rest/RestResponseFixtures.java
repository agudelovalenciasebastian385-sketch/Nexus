package nexusmarket.adapters.rest;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static nexusmarket.domain.DomainFixtures.NOW;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.CartItem;
import nexusmarket.domain.models.CommercialReport;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryReport;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.LogisticsReport;
import nexusmarket.domain.models.LowStockProduct;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.SellerPerformanceReport;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.models.WarehouseStockSummary;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Domain objects returned by the mocked role ports, the JSON fields they must produce, and the request
 * bodies with the domain values they must convert to. They are shared and never changed: the ports are
 * mocks, so nothing mutates them.
 */
final class RestResponseFixtures {

    // ------------------------------------------------------------------ objects returned by the ports

    static final Buyer BUYER = DomainFixtures.buyer();
    static final Seller SELLER = DomainFixtures.seller();
    static final Warehouse WAREHOUSE = DomainFixtures.sellerWarehouse(SELLER);
    static final PhysicalProduct PRODUCT = DomainFixtures.publishedPhysical(SELLER);
    static final Inventory INVENTORY = new Inventory(PRODUCT, WAREHOUSE, 10);
    static final Cart CART = new Cart("cart-1", BUYER,
            List.of(CartItem.rehydrate(PRODUCT, null, 2, PRODUCT.getPrice())));
    static final Order ORDER = new Order("order-1", BUYER, List.of(DomainFixtures.physicalItem(PRODUCT, WAREHOUSE, 2)),
            NOW);
    static final Invoice INVOICE = new Invoice("invoice-1", ORDER, NOW);
    static final Shipment SHIPMENT = new Shipment("shipment-1", ORDER, WAREHOUSE, ADDRESS, DomainFixtures.operator(),
            NOW);
    static final ReturnRequest RETURN_REQUEST = new ReturnRequest("return-1", ORDER, "Wrong color");
    static final Refund REFUND = new Refund("refund-1", RETURN_REQUEST, new BigDecimal("200.00"));
    static final AuditLog AUDIT_LOG = new AuditLog("audit-1", AuditAction.USER_LOGGED_IN, BUYER, AuditEntityType.USER,
            BUYER.getId(), NOW, Map.of("channel", "web"));

    static {
        SELLER.addWarehouse(WAREHOUSE);
    }

    // ------------------------------------------------------------------ report query parameters

    static final String START_DATE = "2026-10-01";
    static final String END_DATE = "2026-10-31";
    static final ReportPeriod PERIOD = new ReportPeriod(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
    static final String THRESHOLD = "5";

    static final CommercialReport COMMERCIAL_REPORT = new CommercialReport(PERIOD, NOW, Map.of(),
            new BigDecimal("300.00"), new BigDecimal("100.00"));
    static final SellerPerformanceReport PERFORMANCE_REPORT = new SellerPerformanceReport(SELLER, PERIOD, NOW, Map.of(),
            3, 7, new BigDecimal("700.00"), 1);
    static final InventoryReport INVENTORY_REPORT = new InventoryReport(5, NOW,
            List.of(new WarehouseStockSummary(WAREHOUSE, 10, 2, 1)), List.of(new LowStockProduct(PRODUCT, 3)));
    static final LogisticsReport LOGISTICS_REPORT = new LogisticsReport(PERIOD, NOW, Map.of());

    // ------------------------------------------------------------------ expected JSON fields

    static final Map<String, Object> BUYER_JSON = Map.of("id", BUYER.getId(), "email", BUYER.getEmail(),
            "status", "ACTIVE", "commercialStatus", "ENABLED", "primaryAddress.city", ADDRESS.getCity());
    static final Map<String, Object> SELLER_JSON = Map.of("id", SELLER.getId(), "email", SELLER.getEmail(),
            "fullName", SELLER.getFullName(), "status", "ACTIVE");
    static final Map<String, Object> WAREHOUSE_JSON = Map.of("id", WAREHOUSE.getId(), "name", WAREHOUSE.getName(),
            "type", "SELLER", "sellerId", SELLER.getId(), "location.city", ADDRESS.getCity());
    static final Map<String, Object> PRODUCT_JSON = Map.of("id", PRODUCT.getId(), "name", PRODUCT.getName(),
            "type", "PHYSICAL", "status", "PUBLISHED", "price", 100.0, "sellerId", SELLER.getId());
    static final Map<String, Object> INVENTORY_JSON = Map.of("productId", PRODUCT.getId(),
            "warehouseId", WAREHOUSE.getId(), "quantity", 10, "reservedQuantity", 0, "condition", "AVAILABLE");
    static final Map<String, Object> CART_JSON = Map.of("id", CART.getId(), "buyerId", BUYER.getId(),
            "items[0].productId", PRODUCT.getId(), "items[0].quantity", 2, "totalAmount", 200.0);
    static final Map<String, Object> ORDER_JSON = Map.of("id", ORDER.getId(), "buyerId", BUYER.getId(),
            "status", "PENDING_PAYMENT", "totalAmount", 200.0, "items[0].productId", PRODUCT.getId());
    static final Map<String, Object> INVOICE_JSON = Map.of("id", INVOICE.getId(), "orderId", ORDER.getId(),
            "totalAmount", 200.0);
    static final Map<String, Object> SHIPMENT_JSON = Map.of("id", SHIPMENT.getId(), "orderId", ORDER.getId(),
            "originWarehouseId", WAREHOUSE.getId(), "status", "PREPARING", "deliveryAddress.city", ADDRESS.getCity());
    static final Map<String, Object> RETURN_JSON = Map.of("id", RETURN_REQUEST.getId(), "orderId", ORDER.getId(),
            "reason", "Wrong color", "status", "REQUESTED");
    static final Map<String, Object> REFUND_JSON = Map.of("id", REFUND.getId(), "returnRequestId", RETURN_REQUEST.getId(),
            "amount", 200.0, "status", "PENDING");
    static final Map<String, Object> AUDIT_JSON = Map.of("id", AUDIT_LOG.getId(), "action", "USER_LOGGED_IN",
            "performedById", BUYER.getId(), "entityType", "USER", "entityId", BUYER.getId(), "details.channel", "web");
    static final Map<String, Object> COMMERCIAL_REPORT_JSON = Map.of("startDate", START_DATE, "endDate", END_DATE,
            "totalInvoiced", 300.0, "totalRefunded", 100.0, "netRevenue", 200.0);
    static final Map<String, Object> PERFORMANCE_REPORT_JSON = Map.of("sellerId", SELLER.getId(),
            "startDate", START_DATE, "endDate", END_DATE, "ordersWithSellerProducts", 3, "unitsSold", 7,
            "amountSold", 700.0, "approvedReturns", 1);
    static final Map<String, Object> INVENTORY_REPORT_JSON = Map.of("lowStockThreshold", 5,
            "warehouses[0].warehouseId", WAREHOUSE.getId(), "warehouses[0].totalUnits", 10,
            "lowStockProducts[0].productId", PRODUCT.getId(), "lowStockProducts[0].availableUnits", 3);
    static final Map<String, Object> LOGISTICS_REPORT_JSON = Map.of("startDate", START_DATE, "endDate", END_DATE,
            "totalShipments", 0);

    // ------------------------------------------------------------------ request bodies and their domain values

    static final Address REQUEST_ADDRESS = new Address("Calle 1", "Apto 2", "Medellin", "Antioquia", "Colombia");
    static final String ADDRESS_BODY = """
            {"line1": "Calle 1", "line2": "Apto 2", "city": "Medellin", "state": "Antioquia", "country": "Colombia"}""";

    static final Variant REQUEST_VARIANT = new Variant("Color", "Black");
    static final String VARIANT_BODY = "{\"type\": \"Color\", \"value\": \"Black\"}";

    static final String PASSWORD_BODY = "{\"currentPassword\": \"Current123*\", \"newPassword\": \"Newpass123*\"}";
    static final String WAREHOUSE_BODY = "{\"name\": \"Hub\", \"location\": " + ADDRESS_BODY + "}";
    static final String INITIAL_STOCK_BODY = "{\"productId\": \"p-1\", \"warehouseId\": \"w-1\", \"quantity\": 4}";
    static final String QUANTITY_BODY = "{\"quantity\": 4}";
    static final String FULL_NAME_BODY = "{\"fullName\": \"New Name\"}";

    private RestResponseFixtures() {
    }

    static Map<String, Object> userJson(nexusmarket.domain.models.User user) {
        return Map.of("id", user.getId(), "email", user.getEmail(), "fullName", user.getFullName(),
                "role", user.getRole().getCode(), "status", user.getStatus().getCode());
    }
}
