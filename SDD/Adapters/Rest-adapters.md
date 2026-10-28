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

| Mapper | Converts |
| --- | --- |
| `AddressRestMapper` | `AddressRequestDTO` → `Address`; `Address` → `AddressResponseDTO` |
| `UserRestMapper` | `User` → `UserResponseDTO`; `AuthenticationResult` → `LoginResponseDTO` |
| `BuyerRestMapper` | `Buyer` → `BuyerResponseDTO` |
| `ProductRestMapper` | `Product` → `ProductResponseDTO`; `Variant` → `VariantResponseDTO` |

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
