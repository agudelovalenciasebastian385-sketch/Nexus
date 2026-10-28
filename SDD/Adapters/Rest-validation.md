# REST Validation Specification (Spring Boot)

## 1. Purpose

This document defines request validation at the REST boundary. It applies to every endpoint of [Api-rest-endpoints.md](Api-rest-endpoints.md) and does not replace domain validation (see [Domain Model](../domain/Domain%20Model.md) and the domain services).

Request DTOs (`adapters/rest/dtos/requests/`) use Jakarta Bean Validation (`spring-boot-starter-validation`); controllers apply `@Valid` to every request body. DTOs and Spring annotations never enter the domain.

## 2. Processing Order

1. **Security:** the filter chain authenticates the token and checks the route role (401/403).
2. **Transport:** route and method (404/405), content type (415), well-formed JSON (400).
3. **Shape and format:** required fields, sizes, email format, nested objects (`@Valid`) → 400 with `errors`.
4. **Domain:** entities and services enforce the authoritative rules (uniqueness, states, ownership, stock) → 400/403/404/409/422.

A failure stops processing; an invalid request never reaches the role port.

## 3. Shared Field Rules

| Field | Boundary rule |
| --- | --- |
| `email` | Required; valid email (`@Email`); at most 150 characters. Surrounding spaces are trimmed by the DTO setter; the domain lower-cases it (`User.normalizeEmail`). Duplicates are detected by the service (409). |
| `password` | Required; 8-72 characters on registration (72 is the BCrypt limit); at most 72 on login. Never returned or logged. |
| `identificationNumber` | Required; at most 40 characters. Uniqueness is checked by the service (409). |
| `fullName` | Required; at most 150 characters. |
| Address `line1` | Required; at most 200 characters. |
| Address `line2` | Optional; at most 200 characters. |
| Address `city`, `state`, `country` | Required; at most 100 characters each. |
| Path identifiers (`{productId}`, …) | Free text; an unknown id returns 404 from the service. |

Sizes match the column lengths of the MySQL tables (see [Persistence-adapters.md](Persistence-adapters.md)).

## 4. Endpoint-Specific Rules

- **Login** (`LoginRequestDTO`): `email` and `password` required. Unknown email and wrong password return the same 401 "Invalid credentials."
- **Buyer registration** (`RegisterBuyerRequestDTO`): `identificationNumber`, `fullName`, `email`, `password` and `primaryAddress` required; the address is validated as a nested object (`@NotNull @Valid`).

## 5. Spring Implementation Requirements

- `@Valid` on every `@RequestBody`; constraints `@NotBlank`, `@NotNull`, `@Email`, `@Size`, `@Positive`, `@PositiveOrZero` where applicable.
- Validation errors are converted by `GlobalExceptionHandler` (see [Global-exception-handler.md](Global-exception-handler.md)) into 400 with one `errors` entry per invalid field.
- Messages are in English (`spring.web.locale: en`, fixed resolver).
