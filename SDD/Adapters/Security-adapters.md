# Security Adapters Specification (Output Adapters)

## 1. Overview

Security adapters reside in `nexusmarket/adapters/security/`. They implement the security output ports of `nexusmarket/domain/ports/out/` (see [Output-ports.md](../domain/Output-ports.md)), so the domain never depends on a JWT or hashing library.

| Output port | Adapter | Technology |
| ----------- | ------- | ---------- |
| `JwtServicePort` | `JwtProvider` | JJWT 0.12.6 (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`), HS256 |
| `PasswordServicePort` | `BCryptPasswordServiceAdapter` | BCrypt (`spring-security-crypto`) |

| Component | Status |
| --------- | ------ |
| `JwtProvider`, `BCryptPasswordServiceAdapter` | Implemented |
| Initial Administrator (`infrastructure/seed/DatabaseSeeder`) | Implemented |
| `JwtAuthenticationFilter` | Implemented |
| `SecurityConfig` (filter chain, route rules, CORS, shared `PasswordEncoder` bean) | Implemented (see [Rest-security-cors.md](Rest-security-cors.md)) |

---

## 2. Structure

```text
adapters/security/
├── dtos/JwtClaimsDTO.java                  <-- claims carried by the token
├── dtos/AuthenticatedUserPrincipal.java    <-- Spring Security principal holding the domain User
├── mappers/JwtClaimsMapper.java            <-- User <-> JwtClaimsDTO <-> JWT claims
├── JwtProvider.java                        <-- implements JwtServicePort
├── BCryptPasswordServiceAdapter.java       <-- implements PasswordServicePort
├── JwtAuthenticationFilter.java            <-- authenticates each request (section 5)
└── SecurityConfig.java                     <-- filter chain, route rules, CORS, PasswordEncoder bean
```

---

## 3. `JwtServicePort` → `JwtProvider`

### 3.1 Token content

The token is **minimal**: it identifies the user and the version of its tokens, and carries no other user data. The current `User` is always loaded from the database (section 5).

| Claim | Source | Description |
| ----- | ------ | ----------- |
| `sub` | `User.id` | Identifier of the user. |
| `jti` | random UUID | Unique token id. |
| `ver` | `User.authTokenVersion` | Version of the user's tokens when this one was issued. |
| `iat` | issue instant | Issued at. |
| `exp` | `iat` + expiration | Expiration instant. |

`JwtClaimsDTO` holds these values (`userId`, `tokenId`, `version`, `issuedAt`, `expiresAt`); `JwtClaimsMapper` converts `User` → DTO → claims when issuing and claims → DTO when reading.

### 3.2 Methods

| Method | Behavior |
| ------ | -------- |
| `generateToken(User user)` | Signs a new HS256 token with the claims above. |
| `validateToken(String token)` | `true` when the signature is valid and the token is not expired; `false` otherwise (never throws). |
| `extractUserId(String token)` | `sub` of a valid token, `null` for an invalid one. |
| `extractTokenVersion(String token)` | `ver` of a valid token, `null` for an invalid one. |

`JwtServicePort` has no method that rebuilds a `User` from the token: the token does not carry the data a `User` requires.

### 3.3 Configuration (`application.yml`)

| Property | Default | Description |
| -------- | ------- | ----------- |
| `app.security.jwt.secret` (`JWT_SECRET`) | development key | HS256 key; at least 256 bits (32 bytes) or startup fails. The default is public: override it outside local development. |
| `app.security.jwt.expiration-minutes` (`JWT_EXPIRATION_MINUTES`) | `120` | Token lifetime. |

---

## 4. `PasswordServicePort` → `BCryptPasswordServiceAdapter`

| Method | Behavior |
| ------ | -------- |
| `encrypt(String rawPassword)` | BCrypt hash (random salt). |
| `matches(String rawPassword, String encodedPassword)` | BCrypt verification; `false` when either value is `null`. |

The adapter uses the `PasswordEncoder` bean (`BCryptPasswordEncoder`) declared in `SecurityConfig`.

---

## 5. JWT Filter Flow

`JwtAuthenticationFilter` (created by `SecurityConfig`, it runs only inside the Spring Security chain) does, for every request that carries a token:

1. Read the `Authorization: Bearer <token>` header.
2. Validate the token with `JwtServicePort.validateToken` (signature and expiration).
3. Extract the user id with `JwtServicePort.extractUserId`.
4. Load the user with `LoadAuthenticatedUserService.loadById`.
5. Verify that the user is `ACTIVE`.
6. Verify that `JwtServicePort.extractTokenVersion` equals the user's `authTokenVersion`. The version changes on logout, password change and status change, which invalidates every token issued before.
7. Put the loaded `User` in the security context; controllers pass it to the role ports as `requestingUser`.

If any step fails (missing or malformed header, invalid or expired token, unknown user, user not `ACTIVE`, version mismatch), the security context is left empty: the request continues unauthenticated and a protected route ends in **401 Unauthorized** through the `AuthenticationEntryPoint` of `SecurityConfig`. Domain services still validate RG-01/RG-02/RG-03 through the authorization services. Route rules, CORS and the 401/403 bodies are specified in [Rest-security-cors.md](Rest-security-cors.md).

---

## 6. Initial Administrator (`infrastructure/seed/DatabaseSeeder`)

At startup (`CommandLineRunner`), when **no** user with role `ADMINISTRATOR` exists, the seeder creates one `Administrator` (`ACTIVE`, `authTokenVersion = 1`) with the password hashed through `PasswordServicePort`. Once any Administrator exists nothing is created, so it runs safely on every startup. If the configured email or identification is already used by another user, it logs a warning and creates nothing.

| Property | Default |
| -------- | ------- |
| `app.seed.admin.identification-number` (`SEED_ADMIN_IDENTIFICATION`) | `1000000000` |
| `app.seed.admin.full-name` (`SEED_ADMIN_FULL_NAME`) | `NexusMarket Administrator` |
| `app.seed.admin.email` (`SEED_ADMIN_EMAIL`) | `admin@nexusmarket.com` |
| `app.seed.admin.password` (`SEED_ADMIN_PASSWORD`) | `Admin123*` |

`UserRepositoryPort` has no query by role, so the "any Administrator exists" check uses `SpringDataJpaUserRepository.existsByRole` directly; the seeder is technical infrastructure, outside the domain.
