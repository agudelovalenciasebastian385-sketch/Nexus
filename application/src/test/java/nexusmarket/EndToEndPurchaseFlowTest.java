package nexusmarket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * End-to-end test of the REST API (step 4C) through MockMvc: every request goes through the real
 * Spring Security chain (JWT filter, route roles), the controllers, the use case transactions, MySQL and
 * MongoDB of the {@code test} profile (nexusmarket_test databases).
 *
 * <p>Each test creates its own sellers, products and buyers with a unique suffix, so the asserted
 * numbers (inventory, seller report) do not depend on data left by other tests. The {@code test} profile
 * approves payments up to 1000.00, so prices are kept low. The manual equivalent against a running
 * application is {@code tools/e2e_4c.py}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EndToEndPurchaseFlowTest {

    private static final String ADDRESS = """
            {"line1": "Calle 1 # 2-3", "city": "Medellin", "state": "Antioquia", "country": "Colombia"}""";

    @Autowired private MockMvc mockMvc;

    private final String run = UUID.randomUUID().toString().substring(0, 8);
    private int sequence;
    private String admin;
    private String operator;
    private String supervisor;

    @BeforeAll
    void registerStaff() throws Exception {
        admin = login("admin@nexusmarket.com", "Admin123*");
        operator = registerStaff("LOGISTICS_OPERATOR", "operator");
        supervisor = registerStaff("SUPERVISOR", "supervisor");
    }

    // ------------------------------------------------------------------ tests

    @Test
    void fullPurchaseFlowUpdatesInventoryAtEachStage() throws Exception {
        Seller seller = registerSeller("flow");
        String product = publishedProduct(seller, "PHYSICAL", "150.00");
        Response initial = expect(201, HttpMethod.POST, "/logistics/inventories", operator,
                "{\"productId\": \"%s\", \"warehouseId\": \"%s\", \"quantity\": 10}".formatted(product, seller.warehouseId));
        assertStock(initial, 10, 0, List.of("INBOUND"));

        Buyer buyer = registerBuyer("flow");
        expect(200, HttpMethod.POST, "/buyer/cart/items", buyer.token,
                "{\"productId\": \"%s\", \"quantity\": 2}".formatted(product));
        Response order = expect(201, HttpMethod.POST, "/buyer/orders", buyer.token, null);
        assertThat(order.<String>json("$.status")).isEqualTo("PENDING_PAYMENT");
        String orderId = order.json("$.id");
        assertStock(inventory(product), 8, 2, List.of("INBOUND", "RESERVATION"));

        assertThat(expect(200, HttpMethod.POST, "/buyer/orders/" + orderId + "/payment", buyer.token, null)
                .<String>json("$.status")).isEqualTo("PAID");
        assertThat(expect(200, HttpMethod.GET, "/buyer/orders/" + orderId + "/invoice", buyer.token, null)
                .<Double>json("$.totalAmount")).isEqualTo(300.0);

        Response shipments = expect(201, HttpMethod.POST, "/logistics/orders/" + orderId + "/shipments", operator, null);
        assertThat(shipments.<List<?>>json("$")).hasSize(1);
        String shipmentId = shipments.json("$[0].id");
        assertThat(shipmentStatus("pack", shipmentId)).isEqualTo("PACKED");
        assertThat(shipmentStatus("dispatch", shipmentId)).isEqualTo("IN_TRANSIT");
        assertStock(inventory(product), 8, 0, List.of("INBOUND", "RESERVATION", "SALE_OUTBOUND"));
        assertThat(shipmentStatus("deliver", shipmentId)).isEqualTo("DELIVERED");
        assertThat(expect(200, HttpMethod.GET, "/buyer/orders/" + orderId, buyer.token, null)
                .<String>json("$.status")).isEqualTo("DELIVERED");

        Response returnRequest = expect(201, HttpMethod.POST, "/buyer/orders/" + orderId + "/returns", buyer.token,
                "{\"reason\": \"Wrong color\"}");
        String returnId = returnRequest.json("$.id");
        assertThat(expect(200, HttpMethod.PATCH, "/admin/returns/" + returnId + "/approve", admin, null)
                .<String>json("$.status")).isEqualTo("APPROVED");
        assertStock(inventory(product), 10, 0, List.of("INBOUND", "RESERVATION", "SALE_OUTBOUND", "RETURN"));

        Response refund = expect(200, HttpMethod.GET, "/admin/returns/" + returnId + "/refund", admin, null);
        assertThat(refund.<String>json("$.status")).isEqualTo("PENDING");
        assertThat(refund.<Double>json("$.amount")).isEqualTo(300.0);
        Response processed = expect(200, HttpMethod.PATCH, "/admin/refunds/" + refund.json("$.id") + "/process",
                admin, null);
        assertThat(processed.<String>json("$.status")).isEqualTo("PROCESSED");
        assertThat(processed.<String>json("$.processedAt")).isNotNull();

        String today = LocalDate.now().toString();
        Response performance = expect(200, HttpMethod.GET,
                "/seller/reports/performance?startDate=" + today + "&endDate=" + today, seller.token, null);
        assertThat(performance.<Integer>json("$.ordersWithSellerProducts")).isEqualTo(1);
        assertThat(performance.<Integer>json("$.unitsSold")).isEqualTo(2);
        assertThat(performance.<Double>json("$.amountSold")).isEqualTo(300.0);
        assertThat(performance.<Integer>json("$.approvedReturns")).isEqualTo(1);
        assertThat(performance.<Double>json("$.returnRate")).isEqualTo(100.0);

        expect(200, HttpMethod.GET, "/supervisor/reports/commercial?startDate=" + today + "&endDate=" + today,
                supervisor, null);
        expect(200, HttpMethod.GET, "/supervisor/reports/inventory?lowStockThreshold=5", supervisor, null);
        expect(200, HttpMethod.GET, "/supervisor/reports/logistics?startDate=" + today + "&endDate=" + today,
                supervisor, null);
        assertThat(expect(200, HttpMethod.GET, "/supervisor/audit-logs/entities/ORDER/" + orderId, supervisor, null)
                .<List<String>>json("$[*].action"))
                .contains("ORDER_CONFIRMED", "PAYMENT_APPROVED", "ORDER_DELIVERED");
    }

    @Test
    void sellerRegistrationReturnsFirstWarehouseAndRejectsDuplicates() throws Exception {
        String email = "dup." + run + "@gmail.com";
        Response created = expect(201, HttpMethod.POST, "/admin/sellers", admin, sellerBody("DUP-" + run, email));
        assertThat(created.<String>json("$.firstWarehouse.type")).isEqualTo("SELLER");
        assertThat(created.<String>json("$.seller.email")).isEqualTo(email);

        expect(409, HttpMethod.POST, "/admin/sellers", admin, sellerBody("DUP2-" + run, email.toUpperCase()));
        expect(409, HttpMethod.POST, "/admin/sellers", admin, sellerBody("DUP-" + run, "other." + run + "@gmail.com"));
    }

    @Test
    void digitalOrderIsDeliveredOnPaymentWithoutShipments() throws Exception {
        Seller seller = registerSeller("digital");
        String product = publishedProduct(seller, "DIGITAL", "50.00");
        Buyer buyer = registerBuyer("digital");
        expect(200, HttpMethod.POST, "/buyer/cart/items", buyer.token,
                "{\"productId\": \"%s\", \"quantity\": 1}".formatted(product));
        String orderId = expect(201, HttpMethod.POST, "/buyer/orders", buyer.token, null).json("$.id");

        Response paid = expect(200, HttpMethod.POST, "/buyer/orders/" + orderId + "/payment", buyer.token, null);
        assertThat(paid.<String>json("$.status")).isEqualTo("DELIVERED");
        assertThat(paid.<List<?>>json("$.shipments")).isEmpty();
    }

    @Test
    void blockedUserLosesItsTokenAndCannotLogInUntilReactivated() throws Exception {
        Buyer buyer = registerBuyer("blocked");
        expect(200, HttpMethod.PATCH, "/admin/users/" + buyer.id + "/status", admin, "{\"status\": \"BLOCKED\"}");

        expect(401, HttpMethod.GET, "/buyer/profile", buyer.token, null);
        expect(403, HttpMethod.POST, "/auth/login", null, loginBody(buyer.email, "Buyer123*"));

        expect(200, HttpMethod.PATCH, "/admin/users/" + buyer.id + "/status", admin, "{\"status\": \"ACTIVE\"}");
        String newToken = login(buyer.email, "Buyer123*");
        assertThat(expect(200, HttpMethod.GET, "/buyer/profile", newToken, null).<String>json("$.status"))
                .isEqualTo("ACTIVE");
    }

    @Test
    void administratorCannotChangeItsOwnStatus() throws Exception {
        String adminId = expect(200, HttpMethod.POST, "/auth/login", null,
                loginBody("admin@nexusmarket.com", "Admin123*")).json("$.user.id");
        Response response = expect(403, HttpMethod.PATCH, "/admin/users/" + adminId + "/status", admin,
                "{\"status\": \"BLOCKED\"}");
        assertThat(response.<String>json("$.message")).isEqualTo("An administrator cannot change its own status.");
    }

    @Test
    void buyerCannotSeeAnotherBuyersOrderInvoiceOrReturn() throws Exception {
        Seller seller = registerSeller("own-b");
        String product = stockedProduct(seller, 5);
        Buyer owner = registerBuyer("owner");
        String orderId = deliveredOrder(owner, product);
        String returnId = expect(201, HttpMethod.POST, "/buyer/orders/" + orderId + "/returns", owner.token,
                "{\"reason\": \"Damaged\"}").json("$.id");

        Buyer other = registerBuyer("other");
        expect(403, HttpMethod.GET, "/buyer/orders/" + orderId, other.token, null);
        expect(403, HttpMethod.GET, "/buyer/orders/" + orderId + "/invoice", other.token, null);
        expect(403, HttpMethod.GET, "/buyer/returns/" + returnId, other.token, null);
    }

    @Test
    void sellerCannotTouchAnotherSellersOrdersProductsOrWarehouses() throws Exception {
        Seller owner = registerSeller("own-s1");
        String product = stockedProduct(owner, 5);
        Buyer buyer = registerBuyer("own-s");
        expect(200, HttpMethod.POST, "/buyer/cart/items", buyer.token,
                "{\"productId\": \"%s\", \"quantity\": 1}".formatted(product));
        String orderId = expect(201, HttpMethod.POST, "/buyer/orders", buyer.token, null).json("$.id");

        Seller intruder = registerSeller("own-s2");
        expect(403, HttpMethod.GET, "/seller/orders/" + orderId, intruder.token, null);
        expect(403, HttpMethod.PUT, "/seller/products/" + product, intruder.token,
                "{\"name\": \"Hacked\", \"description\": \"Hacked\", \"price\": 1.00, \"variants\": []}");
        expect(403, HttpMethod.PATCH, "/seller/products/" + product + "/suspend", intruder.token, null);
        String ownProduct = publishedProduct(intruder, "PHYSICAL", "20.00");
        expect(403, HttpMethod.POST, "/seller/inventories", intruder.token,
                "{\"productId\": \"%s\", \"warehouseId\": \"%s\", \"quantity\": 5}".formatted(ownProduct,
                        owner.warehouseId));
        expect(403, HttpMethod.POST, "/seller/inventories/" + product + "/" + owner.warehouseId + "/inbounds",
                intruder.token, "{\"quantity\": 5}");
    }

    @Test
    void supervisorIsReadOnlyAndInvalidParametersAreRejected() throws Exception {
        expect(403, HttpMethod.POST, "/admin/sellers", supervisor, sellerBody("SUP-" + run, "sup." + run + "@gmail.com"));
        expect(403, HttpMethod.PATCH, "/seller/products/any/publish", supervisor, null);

        expect(400, HttpMethod.GET, "/supervisor/reports/inventory?lowStockThreshold=0", supervisor, null);
        expect(400, HttpMethod.GET, "/supervisor/reports/inventory?lowStockThreshold=-1", supervisor, null);
        expect(400, HttpMethod.GET, "/supervisor/audit-logs/entities/FOO/any", supervisor, null);
        expect(400, HttpMethod.GET, "/supervisor/reports/commercial?startDate=2026-12-31&endDate=2026-01-01",
                supervisor, null);
    }

    // ------------------------------------------------------------------ actors and data

    private record Seller(String token, String warehouseId) {
    }

    private record Buyer(String token, String id, String email) {
    }

    private String unique(String tag) {
        return tag + "-" + run + "-" + (++sequence);
    }

    private String registerStaff(String role, String tag) throws Exception {
        String email = unique(tag) + "@nexusmarket.test";
        expect(201, HttpMethod.POST, "/admin/staff-users", admin, """
                {"role": "%s", "identificationNumber": "%s", "fullName": "ITest %s", "email": "%s",
                 "password": "Staff123*"}""".formatted(role, unique("doc"), tag, email));
        return login(email, "Staff123*");
    }

    private Seller registerSeller(String tag) throws Exception {
        String email = unique(tag) + "@nexusmarket.test";
        Response created = expect(201, HttpMethod.POST, "/admin/sellers", admin, sellerBody(unique("doc"), email));
        return new Seller(login(email, "Seller123*"), created.json("$.firstWarehouse.id"));
    }

    private Buyer registerBuyer(String tag) throws Exception {
        String email = unique(tag) + "@nexusmarket.test";
        Response created = expect(201, HttpMethod.POST, "/auth/register/buyers", null, """
                {"identificationNumber": "%s", "fullName": "ITest Buyer", "email": "%s", "password": "Buyer123*",
                 "primaryAddress": %s}""".formatted(unique("doc"), email, ADDRESS));
        return new Buyer(login(email, "Buyer123*"), created.json("$.id"), email);
    }

    private String sellerBody(String document, String email) {
        return """
                {"identificationNumber": "%s", "fullName": "ITest Seller", "email": "%s", "password": "Seller123*",
                 "warehouseName": "ITest Warehouse", "warehouseLocation": %s}""".formatted(document, email, ADDRESS);
    }

    private String publishedProduct(Seller seller, String type, String price) throws Exception {
        String id = expect(201, HttpMethod.POST, "/seller/products", seller.token, """
                {"type": "%s", "name": "%s", "description": "Integration test product", "price": %s,
                 "variants": []}""".formatted(type, unique("product"), price)).json("$.id");
        expect(200, HttpMethod.PATCH, "/seller/products/" + id + "/publish", seller.token, null);
        return id;
    }

    private String stockedProduct(Seller seller, int quantity) throws Exception {
        String product = publishedProduct(seller, "PHYSICAL", "100.00");
        expect(201, HttpMethod.POST, "/logistics/inventories", operator,
                "{\"productId\": \"%s\", \"warehouseId\": \"%s\", \"quantity\": %d}".formatted(product,
                        seller.warehouseId, quantity));
        return product;
    }

    /** Buys one unit, pays and runs the shipment until the order is DELIVERED. */
    private String deliveredOrder(Buyer buyer, String product) throws Exception {
        expect(200, HttpMethod.POST, "/buyer/cart/items", buyer.token,
                "{\"productId\": \"%s\", \"quantity\": 1}".formatted(product));
        String orderId = expect(201, HttpMethod.POST, "/buyer/orders", buyer.token, null).json("$.id");
        expect(200, HttpMethod.POST, "/buyer/orders/" + orderId + "/payment", buyer.token, null);
        String shipmentId = expect(201, HttpMethod.POST, "/logistics/orders/" + orderId + "/shipments", operator, null)
                .json("$[0].id");
        shipmentStatus("pack", shipmentId);
        shipmentStatus("dispatch", shipmentId);
        shipmentStatus("deliver", shipmentId);
        return orderId;
    }

    private String shipmentStatus(String action, String shipmentId) throws Exception {
        return expect(200, HttpMethod.PATCH, "/logistics/shipments/" + shipmentId + "/" + action, operator, null)
                .json("$.status");
    }

    /** Inventories of a product; the product has stock in a single warehouse, read at "$[0]". */
    private Response inventory(String product) throws Exception {
        Response inventories = expect(200, HttpMethod.GET, "/logistics/products/" + product + "/inventories",
                operator, null);
        assertThat(inventories.<List<?>>json("$")).hasSize(1);
        return inventories;
    }

    private static void assertStock(Response inventory, int quantity, int reserved, List<String> movements) {
        String root = inventory.body().stripLeading().startsWith("[") ? "$[0]" : "$";
        assertThat(inventory.<Integer>json(root + ".quantity")).as("quantity").isEqualTo(quantity);
        assertThat(inventory.<Integer>json(root + ".reservedQuantity")).as("reservedQuantity").isEqualTo(reserved);
        assertThat(inventory.<List<String>>json(root + ".movements[*].type")).as("movements").isEqualTo(movements);
    }

    // ------------------------------------------------------------------ HTTP

    private record Response(int status, String body) {
        <T> T json(String path) {
            return JsonPath.read(body, path);
        }
    }

    private String login(String email, String password) throws Exception {
        return expect(200, HttpMethod.POST, "/auth/login", null, loginBody(email, password)).json("$.token");
    }

    private static String loginBody(String email, String password) {
        return "{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, password);
    }

    private Response expect(int status, HttpMethod method, String path, String token, String body) throws Exception {
        MockHttpServletRequestBuilder builder = request(method, "/api/v1" + path).contentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            builder.content(body);
        }
        MvcResult result = mockMvc.perform(builder).andReturn();
        Response response = new Response(result.getResponse().getStatus(),
                result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        assertThat(response.status()).as("%s %s -> %s", method, path, response.body()).isEqualTo(status);
        return response;
    }
}
