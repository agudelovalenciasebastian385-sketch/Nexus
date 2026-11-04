package nexusmarket;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import nexusmarket.adapters.persistence.memory.MemoryTransactionManager;
import nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The memory profile over real HTTP, on a random port, without any database: this class runs with mvnw
 * test and without Docker.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.payment.simulated.approval-limit=1000.00")
@ActiveProfiles("memory")
class MemoryProfileFlowTest {

    private static final String ADDRESS = """
            {"line1": "Calle 1", "city": "Medellin", "state": "Antioquia", "country": "Colombia"}""";
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final HttpClient client = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private ApplicationContext context;

    private record Response(int status, JsonNode body) {
        String text(String field) {
            return body.path(field).asString();
        }
    }

    private Response call(String method, String path, String token, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        JsonNode json = response.body().startsWith("{") || response.body().startsWith("[")
                ? JSON.readTree(response.body()) : JSON.nullNode();
        return new Response(response.statusCode(), json);
    }

    private Response expect(int status, String method, String path, String token, String body) throws Exception {
        Response response = call(method, path, token, body);
        assertThat(response.status()).as("%s %s -> %s", method, path, response.body()).isEqualTo(status);
        return response;
    }

    private String login(String email, String password) throws Exception {
        return expect(200, "POST", "/api/v1/auth/login", null,
                "{\"email\": \"%s\", \"password\": \"%s\"}".formatted(email, password)).text("token");
    }

    private String admin() throws Exception {
        return login("admin@nexusmarket.com", "Admin123*");
    }

    private record SellerAccount(String token, String warehouseId) {
    }

    private SellerAccount registeredSeller() throws Exception {
        String unique = UUID.randomUUID().toString();
        String email = "seller-" + unique + "@example.com";
        Response seller = expect(201, "POST", "/api/v1/admin/sellers", admin(), """
                {"identificationNumber": "%s", "fullName": "Store", "email": "%s", "password": "Seller123*",
                 "warehouseName": "Main", "warehouseLocation": %s}""".formatted(unique, email, ADDRESS));
        return new SellerAccount(login(email, "Seller123*"), seller.body().at("/firstWarehouse/id").asString());
    }

    private String publishedProduct(SellerAccount seller, String price, Integer stock) throws Exception {
        String id = expect(201, "POST", "/api/v1/seller/products", seller.token(), """
                {"type": "PHYSICAL", "name": "Mouse", "description": "Mouse", "price": %s, "variants": []}"""
                .formatted(price)).text("id");
        expect(200, "PATCH", "/api/v1/seller/products/" + id + "/publish", seller.token(), null);
        if (stock != null) {
            expect(201, "POST", "/api/v1/seller/inventories", seller.token(),
                    "{\"productId\": \"%s\", \"warehouseId\": \"%s\", \"quantity\": %d}"
                            .formatted(id, seller.warehouseId(), stock));
        }
        return id;
    }

    private String registeredBuyer() throws Exception {
        String unique = UUID.randomUUID().toString();
        String email = "buyer-" + unique + "@example.com";
        expect(201, "POST", "/api/v1/auth/register/buyers", null, """
                {"identificationNumber": "%s", "fullName": "Maria", "email": "%s", "password": "Buyer123*",
                 "primaryAddress": %s}""".formatted(unique, email, ADDRESS));
        return login(email, "Buyer123*");
    }

    private void addToCart(String buyer, String product, int quantity) throws Exception {
        expect(200, "POST", "/api/v1/buyer/cart/items", buyer,
                "{\"productId\": \"%s\", \"quantity\": %d}".formatted(product, quantity));
    }

    private String confirmedOrder(String buyer, String product, int quantity) throws Exception {
        addToCart(buyer, product, quantity);
        return expect(201, "POST", "/api/v1/buyer/orders", buyer, null).text("id");
    }

    private List<String> auditActions(String entityType, String entityId) throws Exception {
        List<String> actions = new ArrayList<>();
        expect(200, "GET", "/api/v1/admin/audit-logs/entities/" + entityType + "/" + entityId, admin(), null).body()
                .forEach(log -> actions.add(log.path("action").asString()));
        return actions;
    }

    private String registeredOperator() throws Exception {
        String unique = UUID.randomUUID().toString();
        String email = "operator-" + unique + "@example.com";
        expect(201, "POST", "/api/v1/admin/staff-users", admin(), """
                {"role": "LOGISTICS_OPERATOR", "identificationNumber": "%s", "fullName": "Laura", "email": "%s",
                 "password": "Operator123*"}""".formatted(unique, email));
        return login(email, "Operator123*");
    }

    // ------------------------------------------------------------------ the requested flow

    @Test
    void shortPurchaseFlowOverHttp() throws Exception {
        SellerAccount seller = registeredSeller();
        String product = publishedProduct(seller, "100.00", 10);
        String buyer = registeredBuyer();

        String order = confirmedOrder(buyer, product, 2);
        Response paid = expect(200, "POST", "/api/v1/buyer/orders/" + order + "/payment", buyer, null);

        assertThat(paid.text("status")).isEqualTo("PAID");
    }

    @Test
    void reservesTheStockOfTheOrder() throws Exception {
        SellerAccount seller = registeredSeller();
        String product = publishedProduct(seller, "100.00", 10);

        confirmedOrder(registeredBuyer(), product, 2);

        JsonNode inventory = expect(200, "GET", "/api/v1/seller/products/" + product + "/inventories", seller.token(), null)
                .body().get(0);
        assertThat(List.of(inventory.get("quantity").asInt(), inventory.get("reservedQuantity").asInt()))
                .containsExactly(8, 2);
    }

    // ------------------------------------------------------------------ no database

    @Test
    void thereIsNoDataSource() {
        assertThat(context.getBeanNamesForType(DataSource.class)).isEmpty();
    }

    @Test
    void thereIsNoMongoClient() {
        assertThat(context.getBeanNamesForType(com.mongodb.client.MongoClient.class)).isEmpty();
    }

    @Test
    void theTransactionManagerIsTheMemoryOne() {
        assertThat(context.getBean(PlatformTransactionManager.class)).isInstanceOf(MemoryTransactionManager.class);
    }

    @Test
    void theRepositoriesAreTheMemoryAdapters() {
        List<Class<?>> ports = List.of(UserRepositoryPort.class, OrderRepositoryPort.class, AuditLogRepositoryPort.class);

        assertThat(ports).allMatch(port -> context.getBean(port).getClass().getPackageName()
                .equals("nexusmarket.adapters.persistence.memory"));
    }

    @Test
    void healthIsUpWithoutDatabases() throws Exception {
        assertThat(call("GET", "/actuator/health", null, null).text("status")).isEqualTo("UP");
    }

    @Test
    void swaggerIsAvailable() throws Exception {
        assertThat(call("GET", "/v3/api-docs", null, null).status()).isEqualTo(200);
    }

    // ------------------------------------------------------------------ sample data

    @ParameterizedTest
    @CsvSource({"vendedor@example.com,Seller123*", "operador@example.com,Operator123*",
            "supervisor@example.com,Supervisor123*", "comprador@example.com,Buyer123*"})
    void sampleUsersCanLogIn(String email, String password) throws Exception {
        assertThat(login(email, password)).isNotBlank();
    }

    @Test
    void theCatalogHasTheSampleProducts() throws Exception {
        List<String> names = new ArrayList<>();
        expect(200, "GET", "/api/v1/public/products", null, null).body().forEach(p -> names.add(p.path("name").asString()));

        assertThat(names).contains("Wireless Mouse", "Mechanical Keyboard", "Java Guide");
    }

    // ------------------------------------------------------------------ concurrency

    @Test
    void twoSimultaneousPurchasesOfTheLastUnitSellItOnce() throws Exception {
        String product = publishedProduct(registeredSeller(), "100.00", 1);
        List<String> buyers = List.of(registeredBuyer(), registeredBuyer());
        for (String buyer : buyers) {
            addToCart(buyer, product, 1);
        }
        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<Integer>> results = new ArrayList<>();
        for (String buyer : buyers) {
            Callable<Integer> purchase = () -> {
                barrier.await();
                return call("POST", "/api/v1/buyer/orders", buyer, null).status();
            };
            results.add(pool.submit(purchase));
        }
        List<Integer> statuses = List.of(results.get(0).get(), results.get(1).get());
        pool.shutdown();

        assertThat(statuses).containsExactlyInAnyOrder(201, 409);
    }

    // ------------------------------------------------------------------ audit after the commit

    @Test
    void aSuccessfulOperationIsAudited() throws Exception {
        String product = publishedProduct(registeredSeller(), "100.00", 5);

        String order = confirmedOrder(registeredBuyer(), product, 1);

        assertThat(auditActions("ORDER", order)).contains("ORDER_CONFIRMED");
    }

    @Test
    void aRejectedPaymentStaysInTheAuditTrailBecauseOfNoRollbackFor() throws Exception {
        String product = publishedProduct(registeredSeller(), "5000.00", 5);
        String buyer = registeredBuyer();
        String order = confirmedOrder(buyer, product, 1);

        expect(422, "POST", "/api/v1/buyer/orders/" + order + "/payment", buyer, null);

        assertThat(auditActions("ORDER", order)).contains("PAYMENT_REJECTED");
    }

    @Test
    void anOperationThatFailsLeavesNoAuditRecord() throws Exception {
        SellerAccount seller = registeredSeller();
        String product = publishedProduct(seller, "100.00", 5);
        String buyer = registeredBuyer();
        String order = confirmedOrder(buyer, product, 1);
        String operator = registeredOperator();
        expect(200, "POST", "/api/v1/buyer/orders/" + order + "/payment", buyer, null);
        String shipment = expect(201, "POST", "/api/v1/logistics/orders/" + order + "/shipments", operator, null)
                .body().get(0).path("id").asString();

        expect(409, "PATCH", "/api/v1/logistics/shipments/" + shipment + "/deliver", operator, null);

        assertThat(auditActions("SHIPMENT", shipment)).doesNotContain("SHIPMENT_DELIVERED");
    }

    // ------------------------------------------------------------------ services that validate before changing

    /**
     * The operator releases the reservation of a paid order and then dispatches its shipment. Memory has no
     * rollback, so this only stays consistent because the service validates the stock before changing anything.
     */
    @Test
    void aFailedDispatchLeavesTheShipmentPacked() throws Exception {
        SellerAccount seller = registeredSeller();
        String product = publishedProduct(seller, "100.00", 5);
        String buyer = registeredBuyer();
        String order = confirmedOrder(buyer, product, 2);
        expect(200, "POST", "/api/v1/buyer/orders/" + order + "/payment", buyer, null);
        String operator = registeredOperator();
        String shipment = expect(201, "POST", "/api/v1/logistics/orders/" + order + "/shipments", operator, null)
                .body().get(0).path("id").asString();
        expect(200, "PATCH", "/api/v1/logistics/shipments/" + shipment + "/pack", operator, null);
        expect(201, "POST", "/api/v1/logistics/inventories/" + product + "/" + seller.warehouseId() + "/reservation-releases",
                operator, "{\"quantity\": 2}");

        expect(400, "PATCH", "/api/v1/logistics/shipments/" + shipment + "/dispatch", operator, null);

        String status = expect(200, "GET", "/api/v1/logistics/orders/" + order, operator, null)
                .body().at("/shipments/0/status").asString();
        assertThat(status).isEqualTo("PACKED");
    }

    /** With a price above zero, the invoice total is positive, so the refund of an approval cannot fail. */
    @Test
    void anApprovedReturnCreatesItsRefund() throws Exception {
        SellerAccount seller = registeredSeller();
        String product = publishedProduct(seller, "0.01", 5);
        String buyer = registeredBuyer();
        String order = confirmedOrder(buyer, product, 1);
        expect(200, "POST", "/api/v1/buyer/orders/" + order + "/payment", buyer, null);
        String operator = registeredOperator();
        String shipment = expect(201, "POST", "/api/v1/logistics/orders/" + order + "/shipments", operator, null)
                .body().get(0).path("id").asString();
        for (String step : List.of("pack", "dispatch", "deliver")) {
            expect(200, "PATCH", "/api/v1/logistics/shipments/" + shipment + "/" + step, operator, null);
        }
        String returnRequest = expect(201, "POST", "/api/v1/buyer/orders/" + order + "/returns", buyer,
                "{\"reason\": \"Wrong color\"}").text("id");

        expect(200, "PATCH", "/api/v1/admin/returns/" + returnRequest + "/approve", admin(), null);

        assertThat(expect(200, "GET", "/api/v1/admin/returns/" + returnRequest + "/refund", admin(), null).text("status"))
                .isEqualTo("PENDING");
    }

    @Test
    void aProductWithAZeroPriceIsRejected() throws Exception {
        expect(400, "POST", "/api/v1/seller/products", registeredSeller().token(), """
                {"type": "PHYSICAL", "name": "Mouse", "description": "Mouse", "price": 0.00, "variants": []}""");
    }
}
