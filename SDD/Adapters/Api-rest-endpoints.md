# API REST Endpoints Specification & Contracts

## 1. Overview

This document specifies the REST API of NexusMarket: HTTP method, path, required role, the role input port operation it invokes, request and response DTOs, and error codes.

The endpoints are organized by **system role**, one controller per role port (`domain/ports/in/`). Each controller injects only its role port (see [Rest-adapters.md](Rest-adapters.md)).

| Controller | Base path | Role | Port |
| --- | --- | --- | --- |
| `AuthRestController` | `/api/v1/auth` | public / authenticated | `PublicAccessPort` |
| `PublicRestController` | `/api/v1/public` | public | `PublicAccessPort` |
| `BuyerRestController` | `/api/v1/buyer` | `BUYER` | `BuyerPort` (step 4B) |
| `SellerRestController` | `/api/v1/seller` | `SELLER` | `SellerPort` (step 4B) |
| `LogisticsOperatorRestController` | `/api/v1/logistics` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort` (step 4C) |
| `AdministratorRestController` | `/api/v1/admin` | `ADMINISTRATOR` | `AdministratorPort` (step 4C) |
| `SupervisorRestController` | `/api/v1/supervisor` | `SUPERVISOR` | `SupervisorPort` (step 4C) |

---

## 2. Global Headers & Security Conventions

- `Authorization: Bearer <jwt_token>` — required on every endpoint except login, public registration and the public catalog.
- `Content-Type: application/json` — required on `POST`, `PUT` and `PATCH` with a body.
- Errors always use the standard body of [Global-exception-handler.md](Global-exception-handler.md); request validation follows [Rest-validation.md](Rest-validation.md); CORS and route security follow [Rest-security-cors.md](Rest-security-cors.md).
- Common error codes of every protected endpoint: `401` (missing, invalid or expired token, user not `ACTIVE`, token version changed), `403` (role not allowed for the route or operation), `404` (unknown route).
- Responses never include passwords, password hashes or token versions.

---

## 3. Endpoint Summary

| # | Method | Path | Role | Port → method | Request | Response | Errors |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | `POST` | `/api/v1/auth/login` | public | `PublicAccessPort.login` | `LoginRequestDTO` | 200 `LoginResponseDTO` | 400, 401, 403 |
| 2 | `POST` | `/api/v1/auth/logout` | any authenticated | `PublicAccessPort.logout` | — | 204 | 401 |
| 3 | `POST` | `/api/v1/auth/register/buyers` | public | `PublicAccessPort.registerBuyerUser` | `RegisterBuyerRequestDTO` | 201 `BuyerResponseDTO` | 400, 409 |
| 4 | `GET` | `/api/v1/public/products` | public | `PublicAccessPort.consultCatalog` | — | 200 `List<ProductResponseDTO>` | — |
| 5 | `GET` | `/api/v1/public/products/{productId}` | public | `PublicAccessPort.consultCatalogProduct` | — | 200 `ProductResponseDTO` | 404 |

---

## 4. Authentication Endpoints (`AuthRestController`, `PublicAccessPort`)

### 4.1. Login
- **HTTP Method:** `POST`
- **Path:** `/api/v1/auth/login`
- **Request Body (`LoginRequestDTO`):**
```json
{
  "email": "admin@nexusmarket.com",
  "password": "Admin123*"
}
```
- **Response (`LoginResponseDTO` - HTTP 200 OK):** `expiresIn` is in seconds (`app.security.jwt.expiration-minutes` × 60).
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 7200,
  "user": {
    "id": "657886e6-0c3a-44e2-b91d-3cddb72f8281",
    "identificationNumber": "1000000000",
    "fullName": "NexusMarket Administrator",
    "email": "admin@nexusmarket.com",
    "role": "ADMINISTRATOR",
    "status": "ACTIVE"
  }
}
```
- **Errors:** `400` invalid body; `401` unknown email or wrong password (same response for both); `403` user `BLOCKED` or `INACTIVE`.
- The email is matched case-insensitively and without surrounding spaces (see [Domain Model](../domain/Domain%20Model.md), `User.normalizeEmail`).

### 4.2. Logout
- **HTTP Method:** `POST`
- **Path:** `/api/v1/auth/logout`
- **Headers:** `Authorization: Bearer <jwt_token>`
- **Request Body:** none
- **Behavior:** advances the authenticated user's `authTokenVersion`; every token issued before is rejected from then on with `401`.
- **Response:** HTTP 204 No Content.

### 4.3. Self-Register Buyer
- **HTTP Method:** `POST`
- **Path:** `/api/v1/auth/register/buyers`
- **Request Body (`RegisterBuyerRequestDTO`):** `line2` is optional.
```json
{
  "identificationNumber": "1030405060",
  "fullName": "Maria Gomez",
  "email": "maria.gomez@gmail.com",
  "password": "Buyer123*",
  "primaryAddress": {
    "line1": "Carrera 45 # 30-12",
    "line2": null,
    "city": "Medellin",
    "state": "Antioquia",
    "country": "Colombia"
  }
}
```
- **Response (`BuyerResponseDTO` - HTTP 201 Created):** the buyer starts `ACTIVE` and `ENABLED`, with an empty cart.
```json
{
  "id": "79917971-e3d2-4c3d-b0e9-7d3e0c085b3b",
  "identificationNumber": "1030405060",
  "fullName": "Maria Gomez",
  "email": "maria.gomez@gmail.com",
  "status": "ACTIVE",
  "commercialStatus": "ENABLED",
  "primaryAddress": { "line1": "Carrera 45 # 30-12", "line2": null, "city": "Medellin", "state": "Antioquia", "country": "Colombia" },
  "additionalAddresses": []
}
```
- **Errors:** `400` invalid body; `409` email already registered (compared normalized: `Maria.Gomez@GMAIL.com` is a duplicate) or identification number already registered; `409` "The record already exists." when two simultaneous registrations collide on a unique key.

---

## 5. Public Catalog Endpoints (`PublicRestController`, `PublicAccessPort`)

### 5.1. Consult Catalog
- **HTTP Method:** `GET`
- **Path:** `/api/v1/public/products`
- **Response (`List<ProductResponseDTO>` - HTTP 200 OK):** only `PUBLISHED` products.

### 5.2. Consult Catalog Product
- **HTTP Method:** `GET`
- **Path:** `/api/v1/public/products/{productId}`
- **Response (`ProductResponseDTO` - HTTP 200 OK):**
```json
{
  "id": "5b1e...",
  "type": "PHYSICAL",
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "price": 89900.00,
  "status": "PUBLISHED",
  "sellerId": "a41c...",
  "sellerName": "Tech Store",
  "variants": [ { "type": "color", "value": "black" } ]
}
```
- **Errors:** `404` unknown product or product not published.
