package nexusmarket.adapters.rest.openapi;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import java.util.Map;
import nexusmarket.adapters.rest.dtos.responses.ErrorResponseDTO;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;

/**
 * General information of the API documentation (Swagger UI and /v3/api-docs), the Bearer JWT scheme
 * and the parts shared by every operation: error responses and parameter descriptions.
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    private static final String ERROR_SCHEMA = "#/components/schemas/ErrorResponseDTO";

    private static final Map<Integer, String> ERROR_DESCRIPTIONS = Map.of(
            400, "Invalid request: a field, a parameter or a business rule was not met.",
            401, "Missing, invalid or expired token, or a user that is no longer active.",
            403, "The role of the user or the ownership of the resource does not allow the operation.",
            404, "The requested resource does not exist.",
            409, "Conflict: duplicate data, invalid status transition, insufficient stock or concurrent change.",
            422, "The payment gateway rejected the payment.");

    private static final Map<String, String> PARAMETER_DESCRIPTIONS = Map.ofEntries(
            Map.entry("userId", "Id of the user."),
            Map.entry("sellerId", "Id of the seller."),
            Map.entry("buyerId", "Id of the buyer."),
            Map.entry("productId", "Id of the product."),
            Map.entry("warehouseId", "Id of the warehouse."),
            Map.entry("orderId", "Id of the order."),
            Map.entry("shipmentId", "Id of the shipment."),
            Map.entry("returnRequestId", "Id of the return request."),
            Map.entry("refundId", "Id of the refund."),
            Map.entry("entityType", "Code of the audited entity type, for example ORDER or PRODUCT."),
            Map.entry("entityId", "Id of the audited entity."),
            Map.entry("variantType", "Type of the variant of the cart line, for example Color."),
            Map.entry("variantValue", "Value of the variant of the cart line, for example Black."),
            Map.entry("startDate", "First day of the period, in yyyy-MM-dd format."),
            Map.entry("endDate", "Last day of the period, in yyyy-MM-dd format."),
            Map.entry("lowStockThreshold", "Positive number of units under which a product counts as low stock."));

    @Bean
    public OpenAPI nexusMarketOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("NexusMarket API")
                        .version("v1")
                        .description("REST API of NexusMarket, a marketplace between buyers and sellers. To call a "
                                + "protected operation, get a token with POST /api/v1/auth/login, press Authorize "
                                + "and paste it, without the Bearer prefix."))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .tags(List.of(
                        new Tag().name("Authentication").description("Login, logout and buyer self-registration."),
                        new Tag().name("Public catalog").description("Published products, open without a token."),
                        new Tag().name("Buyer").description("Account, profile, cart, orders, invoices and returns."),
                        new Tag().name("Seller").description("Account, warehouses, products, stock and performance."),
                        new Tag().name("Logistics operator").description("Stock, reservations and shipments."),
                        new Tag().name("Administrator").description("Users, sellers, warehouses, returns and reports."),
                        new Tag().name("Supervisor").description("Read-only access to information and reports.")));
    }

    /** Adds the documented errors of each operation and describes its parameters. */
    @Bean
    public OperationCustomizer documentedOperations() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            ApiErrors errors = handlerMethod.getMethodAnnotation(ApiErrors.class);
            if (errors != null) {
                for (int code : errors.value()) {
                    responses.addApiResponse(String.valueOf(code), errorResponse(code));
                }
            }
            if (!handlerMethod.hasMethodAnnotation(SecurityRequirements.class)) {
                responses.addApiResponse(String.valueOf(HttpStatus.UNAUTHORIZED.value()),
                        errorResponse(HttpStatus.UNAUTHORIZED.value()));
                responses.addApiResponse(String.valueOf(HttpStatus.FORBIDDEN.value()),
                        errorResponse(HttpStatus.FORBIDDEN.value()));
            }
            if (operation.getParameters() != null) {
                for (Parameter parameter : operation.getParameters()) {
                    parameter.setDescription(PARAMETER_DESCRIPTIONS.get(parameter.getName()));
                }
            }
            return operation;
        };
    }

    /** No operation returns ErrorResponseDTO in its signature, so its schema is registered here. */
    @Bean
    public OpenApiCustomizer errorResponseSchema() {
        return openApi -> ModelConverters.getInstance().readAll(ErrorResponseDTO.class)
                .forEach((name, schema) -> openApi.getComponents().addSchemas(name, schema));
    }

    private static ApiResponse errorResponse(int code) {
        return new ApiResponse()
                .description(ERROR_DESCRIPTIONS.get(code))
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new Schema<>().$ref(ERROR_SCHEMA))));
    }
}
