# REST Adapters Specification

## 1. Overview

REST Adapters reside in `nexusmarket/adapters/rest/`. They handle HTTP transport concerns: receive client requests, validate request DTOs, take the authenticated `User` placed in the security context by the JWT filter, call the corresponding **role input port**, and map Domain Models back to response DTOs. They contain no business rules.

---

## 2. Component Architecture

```text
HTTP Request (Client)
      |
      v  Authorization: Bearer <jwt>
Spring Security Filter Chain (SecurityConfig)
      |  JwtAuthenticationFilter: validate token, load User, check ACTIVE + token version
      |  Route rule: permitAll / authenticated / hasRole(<SystemRole>)
      v
REST Controller (adapters/rest/controllers)
      |  1. @Valid request DTO
      |  2. Request DTO -> domain values (mappers)
      |  3. Calls its role port, passing the authenticated User first
      v
Role Input Port (domain/ports/in/*Port)
      ^
      |  Implemented by adapters/useCases/*UseCaseImpl
Domain Services
      |
      v
Domain Model -> Response DTO (mappers) -> HTTP Response
```

---

## 3. Authentication Flow

### 3.1 Login (`POST /api/v1/auth/login`)
1. The client sends `LoginRequestDTO` (email, password).
2. `AuthRestController` calls `PublicAccessPort.login(email, password)`.
3. `LoginService` normalizes the email, verifies the password with `PasswordServicePort` and issues a JWT through `JwtServicePort` (`sub`, `jti`, `ver`, `iat`, `exp`).
4. The controller maps the `AuthenticationResult` to `LoginResponseDTO`.

### 3.2 Authenticated requests
`JwtAuthenticationFilter` follows [Security-adapters.md](Security-adapters.md), section 5: it validates the token, extracts the user id, loads the user with `LoadAuthenticatedUserService`, checks that it is `ACTIVE` and that the token version equals `authTokenVersion`. It then stores an `AuthenticatedUserPrincipal` holding the domain `User`.

Controllers receive that user with:

```java
@AuthenticationPrincipal(expression = "user") User user
```

and pass it as the first argument (`requestingUser`) of the role port. Domain services still validate RG-01/RG-02/RG-03.

---

## 4. REST Layer Structure

```text
adapters/rest/
├── controllers/              one controller per role port (+ AuthRestController)
├── dtos/
│   ├── requests/             *RequestDTO with Jakarta validation
│   └── responses/            *ResponseDTO, ErrorResponseDTO
├── mappers/                  *RestMapper: request DTO -> domain values, Domain Model -> response DTO
└── exception/
    └── GlobalExceptionHandler
```

| Controller | Injects | Endpoints |
| --- | --- | --- |
| `AuthRestController` | `PublicAccessPort` | 3 |
| `PublicRestController` | `PublicAccessPort` | 2 |
| `BuyerRestController` | `BuyerPort` | 17 |
| `SellerRestController` | `SellerPort` | 20 |
| `LogisticsOperatorRestController` | `LogisticsOperatorPort` | 17 |
| `AdministratorRestController` | `AdministratorPort` | 27 |
| `SupervisorRestController` | `SupervisorPort` | 22 |
| **Total** | | **108** |

| Mapper | Converts |
| --- | --- |
| `AddressRestMapper` | `AddressRequestDTO` (one or a list) → `Address`; `Address` → `AddressResponseDTO` |
| `VariantRestMapper` | `VariantRequestDTO` (one or a list) or query parameters → `Variant`; `Variant` → `VariantResponseDTO` |
| `UserRestMapper` | `User` → `UserResponseDTO`; `AuthenticationResult` → `LoginResponseDTO` |
| `BuyerRestMapper` | `Buyer` → `BuyerResponseDTO` |
| `SellerRestMapper` | `Seller` → `SellerResponseDTO`; new `Seller` → `SellerRegistrationResponseDTO` (with its first warehouse) |
| `AuditLogRestMapper` | `AuditLog` → `AuditLogResponseDTO` |
| `ProductRestMapper` | `Product` → `ProductResponseDTO` |
| `CartRestMapper` | `Cart` → `CartResponseDTO` (with `CartItemResponseDTO`) |
| `OrderRestMapper` | `Order` → `OrderResponseDTO` (items and shipments); `Shipment` (one or a list) → `ShipmentResponseDTO` |
| `InvoiceRestMapper` | `Invoice` → `InvoiceResponseDTO` |
| `ReturnRestMapper` | `ReturnRequest` → `ReturnRequestResponseDTO`; `Refund` → `RefundResponseDTO` |
| `WarehouseRestMapper` | `Warehouse` → `WarehouseResponseDTO` |
| `InventoryRestMapper` | `Inventory` → `InventoryResponseDTO` (with its movements) |
| `ReportRestMapper` | `SellerPerformanceReport`, `CommercialReport`, `InventoryReport` and `LogisticsReport` → their response DTOs (catalog keys as codes) |

Catalog codes received in bodies or paths (`role`, `status`, `commercialStatus`, `condition`, `type`, `entityType`) are converted by the controllers with `fromCode`; an unknown code raises `InvalidCatalogCodeException` (400). The type of a new warehouse is fixed by the role: `SELLER` in `SellerRestController`, `MARKETPLACE` in `AdministratorRestController`.

Own resources (account, profile, cart, own lists and own reports) take no id in the path: the controller passes the id of the authenticated user (`User.getId()`), and the services still validate ownership (RG-03). Controllers never handle transactions: each port call runs in the transaction opened by its `*UseCaseImpl` (see [Use-cases-adapters.md](Use-cases-adapters.md), section 3).

---

## 5. Rules

1. A controller injects **only its role port**: never domain services, repositories or other adapters.
2. Request DTOs never enter the domain: the controller passes plain values or value objects built by the mappers (`Address`, `Variant`).
3. Domain Models never leave the API: every response is a response DTO. Passwords, hashes and token versions are never mapped.
4. Status codes: `201 Created` for creations, `204 No Content` for operations without a result, `200 OK` otherwise.
5. Business errors are not caught in controllers; they propagate to `GlobalExceptionHandler`.

---

## 6. Code Pattern Example

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final PublicAccessPort publicAccessPort;

    @PostMapping("/register/buyers")
    public ResponseEntity<BuyerResponseDTO> registerBuyer(@Valid @RequestBody RegisterBuyerRequestDTO request) {
        Buyer buyer = publicAccessPort.registerBuyerUser(request.getIdentificationNumber(), request.getFullName(),
                request.getEmail(), request.getPassword(), AddressRestMapper.toDomain(request.getPrimaryAddress()));
        return ResponseEntity.status(HttpStatus.CREATED).body(BuyerRestMapper.toResponse(buyer));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal(expression = "user") User user) {
        publicAccessPort.logout(user);
        return ResponseEntity.noContent().build();
    }
}
```
