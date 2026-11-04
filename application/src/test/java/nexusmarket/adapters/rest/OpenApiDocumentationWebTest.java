package nexusmarket.adapters.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import nexusmarket.adapters.rest.openapi.OpenApiConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.configuration.SpringDocSecurityConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads the real /v3/api-docs. The status of each operation is compared with the endpoint tables, so the
 * documentation cannot contradict what the endpoint tests verify.
 */
@Import(OpenApiConfig.class)
@ImportAutoConfiguration({SpringDocConfiguration.class, SpringDocSecurityConfiguration.class,
        SpringDocConfigProperties.class, SpringDocWebMvcConfiguration.class, SwaggerConfig.class, SwaggerUiConfigProperties.class,
        SwaggerUiConfigParameters.class, SwaggerUiOAuthProperties.class})
class OpenApiDocumentationWebTest extends SecuredWebTestBase {

    private static final int EXPECTED_OPERATIONS = 108;
    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete");
    private static final Set<String> PUBLIC_OPERATIONS = Set.of("POST /api/v1/auth/login",
            "POST /api/v1/auth/register/buyers", "GET /api/v1/public/products", "GET /api/v1/public/products/{productId}");
    private static final Set<String> TAGS = Set.of("Authentication", "Public catalog", "Buyer", "Seller",
            "Logistics operator", "Administrator", "Supervisor");
    private static final List<String> FORBIDDEN_RESPONSE_FIELDS = List.of("password", "passwordhash", "authtokenversion");

    /** The document does not change during the run, and generating it takes much longer than reading it. */
    private static JsonNode documentation;

    private record DocumentedOperation(String key, JsonNode node) {
    }

    private JsonNode apiDocs() throws Exception {
        if (documentation == null) {
            String body = mockMvc.perform(get("/v3/api-docs")).andReturn().getResponse().getContentAsString();
            documentation = JsonMapper.builder().build().readTree(body);
        }
        return documentation;
    }

    private List<DocumentedOperation> operations(JsonNode docs) {
        List<DocumentedOperation> operations = new ArrayList<>();
        docs.get("paths").properties().forEach(path -> path.getValue().properties().forEach(method -> {
            if (HTTP_METHODS.contains(method.getKey())) {
                operations.add(new DocumentedOperation(method.getKey().toUpperCase() + " " + path.getKey(),
                        method.getValue()));
            }
        }));
        return operations;
    }

    private JsonNode operation(String key) throws Exception {
        return operations(apiDocs()).stream().filter(candidate -> candidate.key().equals(key)).findFirst()
                .orElseThrow(() -> new AssertionError("Not documented: " + key)).node();
    }

    static Stream<EndpointCase<?>> endpoints() {
        return Stream.of(AuthRestControllerWebTest.endpoints(), PublicRestControllerWebTest.endpoints(),
                        BuyerRestControllerWebTest.endpoints(), SellerRestControllerWebTest.endpoints(),
                        LogisticsOperatorRestControllerWebTest.endpoints(), AdministratorRestControllerWebTest.endpoints(),
                        SupervisorRestControllerWebTest.endpoints())
                .flatMap(table -> table.map(endpoint -> (EndpointCase<?>) endpoint));
    }

    // ------------------------------------------------------------------ open routes

    @ParameterizedTest
    @ValueSource(strings = {"/v3/api-docs", "/v3/api-docs/swagger-config", "/swagger-ui/index.html"})
    void documentationRouteIsOpenWithoutToken(String route) throws Exception {
        mockMvc.perform(get(route)).andExpect(status().isOk());
    }

    @Test
    void swaggerUiEntryRouteIsOpenAndRedirectsToTheInterface() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
    }

    // ------------------------------------------------------------------ general information

    @Test
    void documentsExactlyTheExpectedNumberOfOperations() throws Exception {
        assertThat(operations(apiDocs())).hasSize(EXPECTED_OPERATIONS);
    }

    @Test
    void declaresTheNameOfTheApi() throws Exception {
        assertThat(apiDocs().at("/info/title").asString()).isEqualTo("NexusMarket API");
    }

    @Test
    void declaresTheVersionOfTheApi() throws Exception {
        assertThat(apiDocs().at("/info/version").asString()).isEqualTo("v1");
    }

    @Test
    void declaresADescriptionOfTheApi() throws Exception {
        assertThat(apiDocs().at("/info/description").asString()).isNotBlank();
    }

    @Test
    void declaresTheBearerJwtScheme() throws Exception {
        JsonNode scheme = apiDocs().at("/components/securitySchemes/" + OpenApiConfig.BEARER_SCHEME);

        assertThat(List.of(scheme.at("/type").asString(), scheme.at("/scheme").asString(),
                scheme.at("/bearerFormat").asString())).containsExactly("http", "bearer", "JWT");
    }

    @Test
    void doesNotPublishTheInitialAdministratorCredentials() throws Exception {
        assertThat(apiDocs().toString()).doesNotContain("admin@nexusmarket.com").doesNotContain("Admin123*");
    }

    // ------------------------------------------------------------------ every operation

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void documentsTheStatusVerifiedByTheEndpointTests(EndpointCase<?> endpoint) throws Exception {
        JsonNode responses = operation(endpoint.toString()).get("responses");

        assertThat(responses.has(String.valueOf(endpoint.expectedStatus()))).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void documentsOnlyOneSuccessStatus(EndpointCase<?> endpoint) throws Exception {
        List<String> success = new ArrayList<>();
        operation(endpoint.toString()).get("responses").properties().forEach(response -> {
            if (response.getKey().startsWith("2")) {
                success.add(response.getKey());
            }
        });

        assertThat(success).hasSize(1);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void hasASummary(EndpointCase<?> endpoint) throws Exception {
        assertThat(operation(endpoint.toString()).at("/summary").asString()).isNotBlank();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void belongsToExactlyOneRoleTag(EndpointCase<?> endpoint) throws Exception {
        JsonNode tags = operation(endpoint.toString()).get("tags");

        assertThat(tags.size() == 1 && TAGS.contains(tags.get(0).asString())).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void asksForTheTokenOnlyOnProtectedOperations(EndpointCase<?> endpoint) throws Exception {
        JsonNode operation = operation(endpoint.toString());

        boolean open = operation.has("security") && operation.get("security").isEmpty();
        assertThat(open).isEqualTo(PUBLIC_OPERATIONS.contains(endpoint.toString()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void describesEveryParameter(EndpointCase<?> endpoint) throws Exception {
        JsonNode parameters = operation(endpoint.toString()).path("parameters");
        List<String> undescribed = new ArrayList<>();
        parameters.forEach(parameter -> {
            if (parameter.path("description").asString("").isBlank()) {
                undescribed.add(parameter.get("name").asString());
            }
        });

        assertThat(undescribed).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void doesNotExposeTheAuthenticatedUserAsAParameter(EndpointCase<?> endpoint) throws Exception {
        List<String> names = new ArrayList<>();
        operation(endpoint.toString()).path("parameters").forEach(parameter -> names.add(parameter.get("name").asString()));

        assertThat(names).doesNotContain("user", "requestingUser");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void responseSchemasHaveNoInternalFields(EndpointCase<?> endpoint) throws Exception {
        JsonNode docs = apiDocs();
        JsonNode operation = operations(docs).stream().filter(candidate -> candidate.key().equals(endpoint.toString()))
                .findFirst().orElseThrow().node();
        Set<String> fields = new HashSet<>();
        operation.get("responses").forEach(response -> collectFields(docs, response, new HashSet<>(), fields));

        assertThat(fields).doesNotContainAnyElementsOf(FORBIDDEN_RESPONSE_FIELDS);
    }

    // ------------------------------------------------------------------ errors

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void protectedOperationsDocument401And403(EndpointCase<?> endpoint) throws Exception {
        if (!PUBLIC_OPERATIONS.contains(endpoint.toString())) {
            JsonNode responses = operation(endpoint.toString()).get("responses");

            assertThat(responses.has("401") && responses.has("403")).isTrue();
        }
    }

    @Test
    void errorResponsesUseTheStandardBody() throws Exception {
        JsonNode response = operation("POST /api/v1/buyer/orders/{orderId}/payment").at("/responses/422");

        assertThat(response.at("/content/application~1json/schema/$ref").asString())
                .isEqualTo("#/components/schemas/ErrorResponseDTO");
    }

    @Test
    void theStandardErrorBodyIsAPublishedSchema() throws Exception {
        assertThat(apiDocs().at("/components/schemas/ErrorResponseDTO/properties").has("message")).isTrue();
    }

    @Test
    void theRequestBodyOfLoginStillDocumentsThePassword() throws Exception {
        assertThat(apiDocs().at("/components/schemas/LoginRequestDTO/properties").has("password")).isTrue();
    }

    /** Walks every schema reachable from the node, following the $ref links, and collects its field names. */
    private void collectFields(JsonNode docs, JsonNode node, Set<String> visited, Set<String> fields) {
        if (node.isObject()) {
            node.properties().forEach(entry -> {
                if (entry.getKey().equals("$ref") && visited.add(entry.getValue().asString())) {
                    collectFields(docs, docs.at(entry.getValue().asString().substring(1)), visited, fields);
                } else if (entry.getKey().equals("properties")) {
                    entry.getValue().propertyNames().forEach(name -> fields.add(name.toLowerCase()));
                    collectFields(docs, entry.getValue(), visited, fields);
                } else {
                    collectFields(docs, entry.getValue(), visited, fields);
                }
            });
        } else if (node.isArray()) {
            node.forEach(item -> collectFields(docs, item, visited, fields));
        }
    }
}
