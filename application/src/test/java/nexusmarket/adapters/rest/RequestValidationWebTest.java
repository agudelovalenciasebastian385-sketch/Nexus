package nexusmarket.adapters.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import nexusmarket.adapters.RolePortsWebTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;

/**
 * Invalid requests answer 400 without reaching the role ports. Security filters are disabled: this
 * class only checks the validation of the REST boundary (SDD/Adapters/Rest-validation.md).
 */
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
class RequestValidationWebTest extends RolePortsWebTestBase {

    private static final String ADDRESS = """
            {"line1": "Calle 1", "city": "Medellin", "state": "Antioquia", "country": "Colombia"}""";

    private static String buyerWith(String email, String password, String address) {
        return """
                {"identificationNumber": "1030", "fullName": "Maria", "email": "%s", "password": "%s",
                 "primaryAddress": %s}""".formatted(email, password, address);
    }

    private static String productWith(String type, String price) {
        return """
                {"type": "%s", "name": "Mouse", "description": "Mouse", "price": %s, "variants": []}"""
                .formatted(type, price);
    }

    static Stream<Arguments> invalidBodies() {
        return Stream.of(
                Arguments.of("/api/v1/auth/login", "{\"email\": \"not-an-email\", \"password\": \"Secret123*\"}", "email"),
                Arguments.of("/api/v1/auth/login", "{\"email\": \"a@b.com\", \"password\": \"\"}", "password"),
                Arguments.of("/api/v1/auth/register/buyers", buyerWith("not-an-email", "Buyer123*", ADDRESS), "email"),
                Arguments.of("/api/v1/auth/register/buyers", buyerWith("a@b.com", "short", ADDRESS), "password"),
                Arguments.of("/api/v1/auth/register/buyers", buyerWith("a@b.com", "Buyer123*", "null"), "primaryAddress"),
                Arguments.of("/api/v1/auth/register/buyers", buyerWith("a@b.com", "Buyer123*", """
                        {"line1": "Calle 1", "city": " ", "state": "Antioquia", "country": "Colombia"}"""),
                        "primaryAddress.city"),
                Arguments.of("/api/v1/buyer/cart/items", "{\"productId\": \"p-1\", \"quantity\": 0}", "quantity"),
                Arguments.of("/api/v1/buyer/cart/items", "{\"quantity\": 1}", "productId"),
                Arguments.of("/api/v1/seller/products", productWith("PHYSICAL", "-1.00"), "price"),
                Arguments.of("/api/v1/seller/products", productWith("PHYSICAL", "1.999"), "price"),
                Arguments.of("/api/v1/seller/products", productWith("", "1.00"), "type"),
                Arguments.of("/api/v1/seller/inventories",
                        "{\"productId\": \"p-1\", \"warehouseId\": \"w-1\", \"quantity\": 0}", "quantity"),
                Arguments.of("/api/v1/logistics/inventories",
                        "{\"productId\": \"p-1\", \"warehouseId\": \"w-1\", \"quantity\": -1}", "quantity"),
                Arguments.of("/api/v1/admin/warehouses", "{\"name\": \"Hub\", \"location\": null}", "location"));
    }

    @ParameterizedTest(name = "{0} {2}")
    @MethodSource("invalidBodies")
    void invalidBodyAnswers400WithTheInvalidField(String path, String body, String field) throws Exception {
        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == '%s')]".formatted(field)).exists());
    }

    @ParameterizedTest(name = "{0} {2}")
    @MethodSource("invalidBodies")
    void invalidBodyDoesNotReachTheRolePort(String path, String body, String field) throws Exception {
        mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body));

        verifyNoRolePortWasCalled();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void lowStockThresholdThatIsNotPositiveAnswers400WithTheField(int threshold) throws Exception {
        mockMvc.perform(get("/api/v1/supervisor/reports/inventory").param("lowStockThreshold", String.valueOf(threshold)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'lowStockThreshold')]").exists());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void lowStockThresholdThatIsNotPositiveDoesNotReachTheRolePort(int threshold) throws Exception {
        mockMvc.perform(get("/api/v1/supervisor/reports/inventory").param("lowStockThreshold", String.valueOf(threshold)));

        verifyNoRolePortWasCalled();
    }

    // ------------------------------------------------------------------ 400 with a message and no field list

    @Test
    void malformedDateAnswers400NamingTheParameterWithoutFieldErrors() throws Exception {
        mockMvc.perform(get("/api/v1/supervisor/reports/commercial").param("startDate", "31-12-2026")
                        .param("endDate", "2026-12-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or missing request parameter: startDate."))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void malformedDateDoesNotReachTheRolePort() throws Exception {
        mockMvc.perform(get("/api/v1/supervisor/reports/commercial").param("startDate", "31-12-2026")
                .param("endDate", "2026-12-31"));

        verifyNoRolePortWasCalled();
    }

    @Test
    void missingDateAnswers400NamingTheParameter() throws Exception {
        mockMvc.perform(get("/api/v1/supervisor/reports/commercial").param("endDate", "2026-12-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or missing request parameter: startDate."));
    }

    @Test
    void invertedPeriodAnswers400WithoutReachingTheRolePort() throws Exception {
        mockMvc.perform(get("/api/v1/supervisor/reports/commercial").param("startDate", "2026-12-31")
                        .param("endDate", "2026-01-01"))
                .andExpect(status().isBadRequest());

        verifyNoRolePortWasCalled();
    }

    @Test
    void malformedJsonAnswers400WithoutFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request body."))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void malformedJsonDoesNotReachTheRolePort() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{not json"));

        verifyNoRolePortWasCalled();
    }

    @Test
    void unknownProductTypeAnswers400WithoutReachingTheRolePort() throws Exception {
        mockMvc.perform(post("/api/v1/seller/products").contentType(MediaType.APPLICATION_JSON)
                        .content(productWith("FOO", "1.00")))
                .andExpect(status().isBadRequest());

        verifyNoRolePortWasCalled();
    }

    @Test
    void validationErrorUsesTheStandardBody() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
