# Global Exception Handler Specification (Spring Boot)

## 1. Purpose

This document defines the single error contract of every REST endpoint and of the Spring Security boundary. Domain exceptions (`domain/exceptions/`) stay framework-independent; `adapters/rest/exception/GlobalExceptionHandler` maps them to HTTP responses.

## 2. Standard Error Response

Every handled error uses this body (`ErrorResponseDTO`):

```json
{
  "timestamp": "2026-10-28T18:46:28.585372500",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed.",
  "path": "/api/v1/auth/register/buyers",
  "errors": [
    { "field": "email", "message": "must be a well-formed email address" },
    { "field": "primaryAddress", "message": "must not be null" }
  ]
}
```

| Field | Content |
| --- | --- |
| `timestamp` | Server date-time of the error. |
| `status` | HTTP status code. |
| `error` | HTTP reason phrase. |
| `message` | Safe, human-readable description (the domain exception message, or a fixed message). |
| `path` | Request URI, without query values. |
| `errors` | Invalid fields (`field`, `message`); present only for validation errors. |

Unexpected errors return a generic message and never expose stack traces, SQL, credentials, tokens or internal hostnames; the cause is written only to the server log.

## 3. Exception Mapping

| Exception | HTTP | Reason |
| --- | ---: | --- |
| `MethodArgumentNotValidException` (`@Valid` body) | 400 | Validation, with `errors` |
| `HandlerMethodValidationException` (path/query constraints) | 400 | Validation, with `errors` |
| `MissingServletRequestParameterException`, `MethodArgumentTypeMismatchException` | 400 | Missing or malformed parameter |
| `HttpMessageNotReadableException` | 400 | Malformed JSON |
| `InvalidAddressException`, `InvalidAuditLogException`, `InvalidCartException`, `InvalidCatalogCodeException`, `InvalidInventoryException`, `InvalidInvoiceException`, `InvalidOrderException`, `InvalidProductException`, `InvalidRefundException`, `InvalidReportParameterException`, `InvalidReportPeriodException`, `InvalidReturnRequestException`, `InvalidShipmentException`, `InvalidUserException`, `InvalidVariantException`, `InvalidWarehouseException` | 400 | Data that violates a domain rule |
| `InvalidCredentialsException` | 401 | Unknown email or wrong password |
| `AuthenticationException` (Spring Security entry point) | 401 | Missing, invalid or expired token; user not `ACTIVE`; token version changed |
| `UnauthorizedOperationException` | 403 | The user cannot perform the operation (includes login of a `BLOCKED` or `INACTIVE` user) |
| `AccessDeniedException` (Spring Security access-denied handler) | 403 | Role not allowed for the route |
| `EntityNotFoundException` | 404 | Entity does not exist |
| `NoResourceFoundException` | 404 | Route does not exist |
| `HttpRequestMethodNotSupportedException` | 405 | HTTP method not supported by the route |
| `HttpMediaTypeNotSupportedException` | 415 | Content type other than `application/json` |
| `UserAlreadyExistsException` | 409 | Duplicate email or identification number |
| `DataIntegrityViolationException` | 409 | Unique key violated (for example two simultaneous registrations); message "The record already exists." with no technical detail |
| `OrderAlreadyFinalizedException` | 409 | A `DELIVERED` order cannot be modified |
| `InvalidOrderStatusTransitionException`, `InvalidStatusTransitionException`, `InvalidProductStatusException` | 409 | The current state does not allow the operation |
| `InsufficientInventoryException` | 409 | Current stock does not allow the reservation |
| `OptimisticLockingFailureException` (includes `ObjectOptimisticLockingFailureException`), `OptimisticLockException` | 409 | Concurrent modification: the inventory, order or cart was changed by another operation after this one read it; the whole use case was rolled back. Message "The resource was modified by another operation; please try again." (see [Persistence-adapters.md](Persistence-adapters.md), section 5.6) |
| `PaymentRejectedException` | 422 | Request and order state are valid, but the payment gateway refused the payment. It is not a conflict with the resource state, and repeating it with the same data fails again |
| `DomainException` (fallback for future subclasses) | 400 | Business rule without a specific mapping |
| `Exception` | 500 | "Unexpected error." |

`InvalidShipmentException` stays in 400 even though some of its messages describe a state ("only for paid orders", "order already has shipments"), because the same class is used for invalid data.

## 4. Spring Boot Implementation

- `GlobalExceptionHandler` is a `@RestControllerAdvice`; Spring selects the most specific handler, so the explicit 409/422 mappings take precedence over the `DomainException` fallback.
- Spring Security failures happen in the filter chain, outside the controllers. `SecurityConfig` configures the `AuthenticationEntryPoint` (401) and the `AccessDeniedHandler` (403) to delegate to Spring MVC's `HandlerExceptionResolver`, so the same advice writes them with the same body.
- `DataIntegrityViolationException` and optimistic lock failures log the cause at `WARN`; unexpected exceptions are logged at `ERROR` with the stack trace.
- Optimistic lock failures are raised when the use case transaction commits, so they reach the handler from the use case adapter like any other exception.
- API messages are in English regardless of the OS locale (`spring.web.locale: en`, `spring.web.locale-resolver: fixed`), including Jakarta validation messages.

## 5. Verified Behavior (step 4A)

| Case | Result |
| --- | --- |
| Admin route without token | 401 |
| Admin route with the admin token after logout | 401 |
| Duplicate email (also upper-cased or with spaces) | 409 |
| Duplicate identification number | 409 |
| Invalid email and missing address | 400 with `errors` |
| Wrong password | 401 |
| Unknown catalog product | 404 |
| Two simultaneous reservations of the last unit (integration test) | one succeeds, the other fails with `ObjectOptimisticLockingFailureException` (409 through the API) |
