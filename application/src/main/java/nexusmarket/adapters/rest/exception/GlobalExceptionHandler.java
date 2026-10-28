package nexusmarket.adapters.rest.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import nexusmarket.adapters.rest.dtos.responses.ErrorResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.ErrorResponseDTO.FieldErrorDTO;
import nexusmarket.domain.exceptions.DomainException;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InsufficientInventoryException;
import nexusmarket.domain.exceptions.InvalidCredentialsException;
import nexusmarket.domain.exceptions.InvalidOrderStatusTransitionException;
import nexusmarket.domain.exceptions.InvalidProductStatusException;
import nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import nexusmarket.domain.exceptions.OrderAlreadyFinalizedException;
import nexusmarket.domain.exceptions.PaymentRejectedException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.exceptions.UserAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates every exception into the standard error body (SDD/Adapters/Global-exception-handler.md).
 *
 * <p>Domain exceptions: InvalidXException 400, InvalidCredentialsException 401,
 * UnauthorizedOperationException 403, EntityNotFoundException 404, duplicates and states that do not
 * allow the operation 409, PaymentRejectedException 422. Spring Security's 401/403 arrive here through
 * the entry point and access-denied handler of SecurityConfig. Unexpected errors return 500 without
 * internal details; the cause is only logged.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ------------------------------------------------------------------ 400

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleBodyValidation(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request) {
        List<FieldErrorDTO> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDTO(error.getField(), error.getDefaultMessage()))
                .toList();
        return body(HttpStatus.BAD_REQUEST, "Request validation failed.", request, errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDTO> handleParameterValidation(HandlerMethodValidationException ex,
                                                                      HttpServletRequest request) {
        List<FieldErrorDTO> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new FieldErrorDTO(result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage())))
                .toList();
        return body(HttpStatus.BAD_REQUEST, "Request validation failed.", request, errors);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponseDTO> handleBadParameter(Exception ex, HttpServletRequest request) {
        return body(HttpStatus.BAD_REQUEST, "Invalid or missing request parameter: " + parameterName(ex) + ".",
                request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleMalformedBody(HttpMessageNotReadableException ex,
                                                                HttpServletRequest request) {
        return body(HttpStatus.BAD_REQUEST, "Malformed JSON request body.", request, null);
    }

    // ------------------------------------------------------------- 401 / 403

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentials(InvalidCredentialsException ex,
                                                                     HttpServletRequest request) {
        return body(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthentication(AuthenticationException ex,
                                                                 HttpServletRequest request) {
        return body(HttpStatus.UNAUTHORIZED,
                "Authentication required: the token is missing, invalid, expired or no longer valid.", request, null);
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedOperation(UnauthorizedOperationException ex,
                                                                        HttpServletRequest request) {
        return body(HttpStatus.FORBIDDEN, ex.getMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return body(HttpStatus.FORBIDDEN, "Access denied: this resource is not available for your role.",
                request, null);
    }

    // ------------------------------------------------------------------ 404 / 405 / 415

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleEntityNotFound(EntityNotFoundException ex,
                                                                 HttpServletRequest request) {
        return body(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoRoute(NoResourceFoundException ex, HttpServletRequest request) {
        return body(HttpStatus.NOT_FOUND, "Route not found: " + request.getMethod() + " " + request.getRequestURI()
                + ".", request, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                     HttpServletRequest request) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method " + ex.getMethod() + " is not supported for this route.",
                request, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMediaType(HttpMediaTypeNotSupportedException ex,
                                                            HttpServletRequest request) {
        return body(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type not supported; use application/json.",
                request, null);
    }

    // ------------------------------------------------------------------ 409 / 422

    @ExceptionHandler({UserAlreadyExistsException.class, OrderAlreadyFinalizedException.class,
            InvalidOrderStatusTransitionException.class, InvalidStatusTransitionException.class,
            InvalidProductStatusException.class, InsufficientInventoryException.class})
    public ResponseEntity<ErrorResponseDTO> handleConflict(DomainException ex, HttpServletRequest request) {
        return body(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    /** A unique key was violated (for example, two simultaneous registrations with the same email). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        LOGGER.warn("Data integrity violation on {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return body(HttpStatus.CONFLICT, "The record already exists.", request, null);
    }

    @ExceptionHandler(PaymentRejectedException.class)
    public ResponseEntity<ErrorResponseDTO> handlePaymentRejected(PaymentRejectedException ex,
                                                                  HttpServletRequest request) {
        return body(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    // ------------------------------------------------------------------ 400 fallback / 500

    /** Every InvalidXException and any other business rule without a specific mapping. */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponseDTO> handleDomain(DomainException ex, HttpServletRequest request) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpected(Exception ex, HttpServletRequest request) {
        LOGGER.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error.", request, null);
    }

    private static String parameterName(Exception ex) {
        if (ex instanceof MissingServletRequestParameterException missing) {
            return missing.getParameterName();
        }
        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            return mismatch.getName();
        }
        return "unknown";
    }

    private static ResponseEntity<ErrorResponseDTO> body(HttpStatus status, String message, HttpServletRequest request,
                                                         List<FieldErrorDTO> errors) {
        ErrorResponseDTO payload = new ErrorResponseDTO(LocalDateTime.now().toString(), status.value(),
                status.getReasonPhrase(), message, request.getRequestURI(), errors);
        return ResponseEntity.status(status).body(payload);
    }
}
