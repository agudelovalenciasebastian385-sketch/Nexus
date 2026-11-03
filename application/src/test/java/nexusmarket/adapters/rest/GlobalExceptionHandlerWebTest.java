package nexusmarket.adapters.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.OptimisticLockException;
import java.util.stream.Stream;
import nexusmarket.adapters.RolePortsWebTestBase;
import nexusmarket.domain.exceptions.DomainException;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InsufficientInventoryException;
import nexusmarket.domain.exceptions.InvalidAddressException;
import nexusmarket.domain.exceptions.InvalidAuditLogException;
import nexusmarket.domain.exceptions.InvalidCartException;
import nexusmarket.domain.exceptions.InvalidCatalogCodeException;
import nexusmarket.domain.exceptions.InvalidCredentialsException;
import nexusmarket.domain.exceptions.InvalidInventoryException;
import nexusmarket.domain.exceptions.InvalidInvoiceException;
import nexusmarket.domain.exceptions.InvalidOrderException;
import nexusmarket.domain.exceptions.InvalidOrderStatusTransitionException;
import nexusmarket.domain.exceptions.InvalidProductException;
import nexusmarket.domain.exceptions.InvalidProductStatusException;
import nexusmarket.domain.exceptions.InvalidRefundException;
import nexusmarket.domain.exceptions.InvalidReportParameterException;
import nexusmarket.domain.exceptions.InvalidReportPeriodException;
import nexusmarket.domain.exceptions.InvalidReturnRequestException;
import nexusmarket.domain.exceptions.InvalidShipmentException;
import nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.exceptions.InvalidVariantException;
import nexusmarket.domain.exceptions.InvalidWarehouseException;
import nexusmarket.domain.exceptions.OrderAlreadyFinalizedException;
import nexusmarket.domain.exceptions.PaymentRejectedException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.exceptions.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Every exception of the table in SDD/Adapters/Global-exception-handler.md answers its HTTP code with
 * the standard body. The role port of a public route throws each one.
 */
@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerWebTest extends RolePortsWebTestBase {

    private static final String ROUTE = "/api/v1/public/products";
    private static final String SECRET_DETAIL = "SQL select password from users at db-host-42";

    static Stream<Arguments> domainExceptions() {
        return Stream.of(
                Arguments.of(new InvalidAddressException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidAuditLogException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidCartException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidCatalogCodeException("ProductType", "FOO"), 400, "Bad Request"),
                Arguments.of(new InvalidInventoryException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidInvoiceException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidOrderException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidProductException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidRefundException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidReportParameterException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidReportPeriodException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidReturnRequestException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidShipmentException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidUserException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidVariantException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidWarehouseException("message"), 400, "Bad Request"),
                Arguments.of(new DomainException("message"), 400, "Bad Request"),
                Arguments.of(new InvalidCredentialsException(), 401, "Unauthorized"),
                Arguments.of(new UnauthorizedOperationException("message"), 403, "Forbidden"),
                Arguments.of(new EntityNotFoundException("Order", "o-1"), 404, "Not Found"),
                Arguments.of(new UserAlreadyExistsException("message"), 409, "Conflict"),
                Arguments.of(new OrderAlreadyFinalizedException("o-1"), 409, "Conflict"),
                Arguments.of(new InvalidOrderStatusTransitionException("PAID", "PAID"), 409, "Conflict"),
                Arguments.of(new InvalidStatusTransitionException("shipment", "PACKED", "PACKED"), 409, "Conflict"),
                Arguments.of(new InvalidProductStatusException("message"), 409, "Conflict"),
                Arguments.of(new InsufficientInventoryException(5, 1), 409, "Conflict"),
                Arguments.of(new PaymentRejectedException("o-1"), 422, "Unprocessable Content"));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("domainExceptions")
    void domainExceptionAnswersItsDocumentedCode(RuntimeException exception, int code, String reason) throws Exception {
        when(publicAccessPort.consultCatalog()).thenThrow(exception);

        mockMvc.perform(get(ROUTE)).andExpect(status().is(code));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("domainExceptions")
    void domainExceptionUsesTheStandardBody(RuntimeException exception, int code, String reason) throws Exception {
        when(publicAccessPort.consultCatalog()).thenThrow(exception);

        mockMvc.perform(get(ROUTE))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(code))
                .andExpect(jsonPath("$.error").value(reason))
                .andExpect(jsonPath("$.message").value(exception.getMessage()))
                .andExpect(jsonPath("$.path").value(ROUTE))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void theTableCoversEveryDomainExceptionClass() {
        assertThat(domainExceptions()).hasSize(27);
    }

    // ------------------------------------------------------------------ persistence conflicts

    @Test
    void uniqueKeyViolationAnswers409WithAFixedMessage() throws Exception {
        when(publicAccessPort.consultCatalog()).thenThrow(new DataIntegrityViolationException(SECRET_DETAIL));

        mockMvc.perform(get(ROUTE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The record already exists."));
    }

    @Test
    void springOptimisticLockFailureAnswers409WithAFixedMessage() throws Exception {
        when(publicAccessPort.consultCatalog())
                .thenThrow(new ObjectOptimisticLockingFailureException(SECRET_DETAIL, new RuntimeException()));

        mockMvc.perform(get(ROUTE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The resource was modified by another operation; please try again."));
    }

    @Test
    void jpaOptimisticLockFailureAnswers409() throws Exception {
        when(publicAccessPort.consultCatalog()).thenThrow(new OptimisticLockException(SECRET_DETAIL));

        mockMvc.perform(get(ROUTE)).andExpect(status().isConflict());
    }

    // ------------------------------------------------------------------ unexpected errors

    @Test
    void unexpectedErrorAnswers500WithAGenericMessage() throws Exception {
        when(publicAccessPort.consultCatalog()).thenThrow(new RuntimeException(SECRET_DETAIL));

        mockMvc.perform(get(ROUTE))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Unexpected error."));
    }

    @Test
    void unexpectedErrorDoesNotExposeInternalDetails() throws Exception {
        when(publicAccessPort.consultCatalog()).thenThrow(new RuntimeException(SECRET_DETAIL));

        String body = mockMvc.perform(get(ROUTE)).andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain(SECRET_DETAIL).doesNotContain("RuntimeException");
    }

    // ------------------------------------------------------------------ transport errors

    @Test
    void unknownRouteAnswers404() throws Exception {
        mockMvc.perform(get("/api/v1/public/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Route not found: GET /api/v1/public/unknown."));
    }

    @Test
    void unsupportedMethodAnswers405() throws Exception {
        mockMvc.perform(put(ROUTE)).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void unsupportedContentTypeAnswers415() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
