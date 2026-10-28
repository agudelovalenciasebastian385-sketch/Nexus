# REST Security and CORS Specification (Spring Security)

## 1. Purpose

This document defines the web security boundary of the NexusMarket API: route authorization, JWT authentication and CORS. It is implemented by `adapters/security/SecurityConfig` and `adapters/security/JwtAuthenticationFilter` (see [Security-adapters.md](Security-adapters.md) for the token and the filter flow).

## 2. Security Filter Chain

- **Stateless:** `SessionCreationPolicy.STATELESS`; no HTTP session, no cookies.
- **CSRF disabled:** the JWT travels in the `Authorization` header, not in a cookie.
- **HTTP Basic and form login disabled.** Authentication is JWT-only; Spring Boot's in-memory user (`UserDetailsServiceAutoConfiguration`) is excluded in `App`.
- `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`. It is created by `SecurityConfig` (not a `@Component`), so it runs only inside the security chain.
- The `PasswordEncoder` bean (`BCryptPasswordEncoder`) is declared in `SecurityConfig` and injected into `BCryptPasswordServiceAdapter`.

## 3. Route Rules

| Route | Rule |
| --- | --- |
| `POST /api/v1/auth/login` | open |
| `POST /api/v1/auth/register/**` | open |
| `GET /api/v1/public/**` | open |
| `OPTIONS /**` (CORS preflight) | open |
| `/error` | open |
| `/api/v1/auth/logout` | authenticated (any role) |
| `/api/v1/buyer/**` | `hasRole("BUYER")` |
| `/api/v1/seller/**` | `hasRole("SELLER")` |
| `/api/v1/logistics/**` | `hasRole("LOGISTICS_OPERATOR")` |
| `/api/v1/admin/**` | `hasRole("ADMINISTRATOR")` |
| `/api/v1/supervisor/**` | `hasRole("SUPERVISOR")` |
| any other route | authenticated |

The authority of the authenticated user is `ROLE_` followed by its `SystemRole` code (`AuthenticatedUserPrincipal`). Route rules are a first barrier: domain services still validate RG-01/RG-02/RG-03.

## 4. 401 and 403 Responses

- No authentication (missing, invalid or expired token, user not `ACTIVE`, token version changed) → the `AuthenticationEntryPoint` returns **401**.
- Authenticated user with a role not allowed for the route → the `AccessDeniedHandler` returns **403**.

Both delegate to Spring MVC's `HandlerExceptionResolver`, so `GlobalExceptionHandler` writes them with the standard error body of [Global-exception-handler.md](Global-exception-handler.md).

## 5. CORS Policy

| Setting | Value |
| --- | --- |
| Allowed origins | `app.security.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS`), comma-separated; default `http://localhost:5173,http://localhost:3000` |
| Allowed methods | `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS` |
| Allowed headers | `Authorization`, `Content-Type` |
| Credentials | not allowed (the token is not a cookie) |
| Preflight cache | 3600 seconds |

Production origins must be configured explicitly; wildcard origins are never used.

## 6. Production Notes

Production deployment requires HTTPS, an externally managed `JWT_SECRET` (the default in `application.yml` is public) and explicit CORS origins.
