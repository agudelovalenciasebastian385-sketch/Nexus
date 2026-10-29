# API REST Endpoints Specification & Contracts

## 1. Overview

This document specifies the REST API of NexusMarket: HTTP method, path, required role, the role input port operation it invokes, request and response DTOs, and error codes.

The endpoints are organized by **system role**, one controller per role port (`domain/ports/in/`). Each controller injects only its role port (see [Rest-adapters.md](Rest-adapters.md)).

| Controller | Base path | Role | Port |
| --- | --- | --- | --- |
| `AuthRestController` | `/api/v1/auth` | public / authenticated | `PublicAccessPort` |
| `PublicRestController` | `/api/v1/public` | public | `PublicAccessPort` |
| `BuyerRestController` | `/api/v1/buyer` | `BUYER` | `BuyerPort` |
| `SellerRestController` | `/api/v1/seller` | `SELLER` | `SellerPort` |
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
| 6 | `GET` | `/api/v1/buyer/account` | `BUYER` | `BuyerPort.consultUser` | — | 200 `UserResponseDTO` | 401, 403 |
| 7 | `PUT` | `/api/v1/buyer/account/password` | `BUYER` | `BuyerPort.changeUserPassword` | `ChangePasswordRequestDTO` | 204 | 400, 401, 403 |
| 8 | `GET` | `/api/v1/buyer/profile` | `BUYER` | `BuyerPort.consultBuyer` | — | 200 `BuyerResponseDTO` | 401, 403 |
| 9 | `PUT` | `/api/v1/buyer/profile` | `BUYER` | `BuyerPort.updateBuyerProfile` | `UpdateBuyerProfileRequestDTO` | 200 `BuyerResponseDTO` | 400, 401, 403 |
| 10 | `GET` | `/api/v1/buyer/cart` | `BUYER` | `BuyerPort.consultCart` | — | 200 `CartResponseDTO` | 401, 403 |
| 11 | `POST` | `/api/v1/buyer/cart/items` | `BUYER` | `BuyerPort.addItemToCart` | `AddCartItemRequestDTO` | 200 `CartResponseDTO` | 400, 404, 409 |
| 12 | `PATCH` | `/api/v1/buyer/cart/items/{productId}` | `BUYER` | `BuyerPort.updateCartItemQuantity` | `UpdateCartItemQuantityRequestDTO` | 200 `CartResponseDTO` | 400, 404 |
| 13 | `DELETE` | `/api/v1/buyer/cart/items/{productId}` | `BUYER` | `BuyerPort.removeItemFromCart` | query `variantType`, `variantValue` | 200 `CartResponseDTO` | 400, 404 |
| 14 | `DELETE` | `/api/v1/buyer/cart/items` | `BUYER` | `BuyerPort.clearCart` | — | 200 `CartResponseDTO` | 401, 403 |
| 15 | `POST` | `/api/v1/buyer/orders` | `BUYER` | `BuyerPort.confirmOrder` | — | 201 `OrderResponseDTO` | 400, 409 |
| 16 | `POST` | `/api/v1/buyer/orders/{orderId}/payment` | `BUYER` | `BuyerPort.validatePayment` | — | 200 `OrderResponseDTO` | 403, 404, 409, 422 |
| 17 | `GET` | `/api/v1/buyer/orders` | `BUYER` | `BuyerPort.consultBuyerOrders` | — | 200 `List<OrderResponseDTO>` | 401, 403 |
| 18 | `GET` | `/api/v1/buyer/orders/{orderId}` | `BUYER` | `BuyerPort.consultOrder` | — | 200 `OrderResponseDTO` | 403, 404 |
| 19 | `GET` | `/api/v1/buyer/orders/{orderId}/invoice` | `BUYER` | `BuyerPort.consultInvoice` | — | 200 `InvoiceResponseDTO` | 403, 404 |
| 20 | `POST` | `/api/v1/buyer/orders/{orderId}/returns` | `BUYER` | `BuyerPort.requestReturn` | `RequestReturnRequestDTO` | 201 `ReturnRequestResponseDTO` | 400, 403, 404, 409 |
| 21 | `GET` | `/api/v1/buyer/returns/{returnRequestId}` | `BUYER` | `BuyerPort.consultReturn` | — | 200 `ReturnRequestResponseDTO` | 403, 404 |
| 22 | `GET` | `/api/v1/buyer/returns/{returnRequestId}/refund` | `BUYER` | `BuyerPort.consultRefund` | — | 200 `RefundResponseDTO` | 403, 404 |
| 23 | `GET` | `/api/v1/seller/account` | `SELLER` | `SellerPort.consultUser` | — | 200 `UserResponseDTO` | 401, 403 |
| 24 | `PUT` | `/api/v1/seller/account/password` | `SELLER` | `SellerPort.changeUserPassword` | `ChangePasswordRequestDTO` | 204 | 400, 401, 403 |
| 25 | `GET` | `/api/v1/seller/profile` | `SELLER` | `SellerPort.consultSeller` | — | 200 `SellerResponseDTO` | 401, 403 |
| 26 | `PATCH` | `/api/v1/seller/profile` | `SELLER` | `SellerPort.updateSeller` | `UpdateSellerRequestDTO` | 200 `SellerResponseDTO` | 400, 401, 403 |
| 27 | `POST` | `/api/v1/seller/warehouses` | `SELLER` | `SellerPort.registerWarehouse` | `WarehouseRequestDTO` | 201 `WarehouseResponseDTO` | 400, 401, 403 |
| 28 | `GET` | `/api/v1/seller/warehouses` | `SELLER` | `SellerPort.consultWarehouses` | — | 200 `List<WarehouseResponseDTO>` | 401, 403 |
| 29 | `GET` | `/api/v1/seller/warehouses/{warehouseId}` | `SELLER` | `SellerPort.consultWarehouse` | — | 200 `WarehouseResponseDTO` | 403, 404 |
| 30 | `PUT` | `/api/v1/seller/warehouses/{warehouseId}` | `SELLER` | `SellerPort.updateWarehouse` | `WarehouseRequestDTO` | 200 `WarehouseResponseDTO` | 400, 403, 404 |
| 31 | `POST` | `/api/v1/seller/products` | `SELLER` | `SellerPort.registerProduct` | `RegisterProductRequestDTO` | 201 `ProductResponseDTO` | 400, 401, 403 |
| 32 | `GET` | `/api/v1/seller/products` | `SELLER` | `SellerPort.consultSellerProducts` | — | 200 `List<ProductResponseDTO>` | 401, 403 |
| 33 | `PUT` | `/api/v1/seller/products/{productId}` | `SELLER` | `SellerPort.updateProduct` | `UpdateProductRequestDTO` | 200 `ProductResponseDTO` | 400, 403, 404 |
| 34 | `PATCH` | `/api/v1/seller/products/{productId}/publish` | `SELLER` | `SellerPort.publishProduct` | — | 200 `ProductResponseDTO` | 403, 404, 409 |
| 35 | `PATCH` | `/api/v1/seller/products/{productId}/suspend` | `SELLER` | `SellerPort.suspendProduct` | — | 200 `ProductResponseDTO` | 403, 404, 409 |
| 36 | `PATCH` | `/api/v1/seller/products/{productId}/discontinue` | `SELLER` | `SellerPort.discontinueProduct` | — | 200 `ProductResponseDTO` | 403, 404, 409 |
| 37 | `POST` | `/api/v1/seller/inventories` | `SELLER` | `SellerPort.registerInitialStock` | `InitialStockRequestDTO` | 201 `InventoryResponseDTO` | 400, 403, 404 |
| 38 | `POST` | `/api/v1/seller/inventories/{productId}/{warehouseId}/inbounds` | `SELLER` | `SellerPort.registerInventoryInbound` | `StockQuantityRequestDTO` | 201 `InventoryResponseDTO` | 400, 403, 404, 409 |
| 39 | `GET` | `/api/v1/seller/products/{productId}/inventories` | `SELLER` | `SellerPort.consultProductInventory` | — | 200 `List<InventoryResponseDTO>` | 403, 404 |
| 40 | `GET` | `/api/v1/seller/warehouses/{warehouseId}/inventories` | `SELLER` | `SellerPort.consultWarehouseInventory` | — | 200 `List<InventoryResponseDTO>` | 403, 404 |
| 41 | `GET` | `/api/v1/seller/orders/{orderId}` | `SELLER` | `SellerPort.consultOrder` | — | 200 `OrderResponseDTO` | 403, 404 |
| 42 | `GET` | `/api/v1/seller/reports/performance` | `SELLER` | `SellerPort.consultSellerPerformanceReport` | query `startDate`, `endDate` | 200 `SellerPerformanceReportResponseDTO` | 400, 401, 403 |

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

---

## 6. Buyer Endpoints (`BuyerRestController`, `BuyerPort`, role `BUYER`)

### 6.1. Conventions
- **Own resources without id.** `/account`, `/profile`, `/cart` and `/orders` (list) take no id in the path: the controller passes the authenticated buyer's id, and the services still validate ownership (RG-03). A buyer cannot name another buyer's id.
- **Cart line = product + variant.** The product goes in the path; the variant goes in the body for `PATCH` and in the query (`variantType` and `variantValue`, both or none) for `DELETE`. Only one of the two query parameters → 400.
- **Password change** (`PUT /account/password`) answers 204 and advances `authTokenVersion`: the current token stops working and the client must log in again.
- Mutations of the cart return the updated cart (200).

### 6.2. Cart
- **Add item** — `POST /api/v1/buyer/cart/items`
```json
{ "productId": "5b1e...", "variant": { "type": "color", "value": "black" }, "quantity": 2 }
```
`variant` is optional. Adding the same product and variant accumulates the quantity. Errors: 400 (validation, variant not offered), 404 (unknown product), 409 (product not published).
- **Change quantity** — `PATCH /api/v1/buyer/cart/items/{productId}` with `{ "variant": {...} | null, "quantity": 3 }`.
- **Remove line** — `DELETE /api/v1/buyer/cart/items/{productId}?variantType=color&variantValue=black` (no query when the line has no variant).
- **Clear** — `DELETE /api/v1/buyer/cart/items`.
- **Response (`CartResponseDTO`):**
```json
{
  "id": "a18d...", "buyerId": "7991...",
  "items": [ { "productId": "5b1e...", "productName": "Wireless Mouse", "productStatus": "PUBLISHED",
               "variant": { "type": "color", "value": "black" }, "quantity": 2,
               "unitPrice": 89900.00, "subtotal": 179800.00 } ],
  "totalAmount": 179800.00
}
```
`productStatus` shows items whose product is no longer published; they stay in the cart and `POST /api/v1/buyer/orders` rejects them.

### 6.3. Orders, payment and invoice
- **Confirm order** — `POST /api/v1/buyer/orders` (no body): converts the cart into an order (`PENDING_PAYMENT`), reserves the stock and empties the cart. 201 `OrderResponseDTO`. Errors: 400 (empty cart, buyer not enabled), 409 (insufficient stock, product not published, concurrent modification).
- **Pay** — `POST /api/v1/buyer/orders/{orderId}/payment`: 200 with the order `PAID` (or `DELIVERED` when it only has digital products). **422** when the gateway rejects the payment (the order stays `PENDING_PAYMENT`); 409 when the order is not pending payment.
- **Response (`OrderResponseDTO`):** `id`, `buyerId`, `status`, `createdAt`, `totalAmount`, `items` (`productId`, `productName`, `productType`, `variant`, `quantity`, `unitPrice`, `subtotal`, `originWarehouseId`) and `shipments` (`ShipmentResponseDTO`). The invoice is consulted at `GET /api/v1/buyer/orders/{orderId}/invoice` (`InvoiceResponseDTO`: `id`, `orderId`, `totalAmount`, `issuedAt`).

### 6.4. Returns
- **Request** — `POST /api/v1/buyer/orders/{orderId}/returns` with `{ "reason": "..." }` → 201 `ReturnRequestResponseDTO` (`id`, `orderId`, `reason`, `status`).
- **Consult** — `GET /api/v1/buyer/returns/{returnRequestId}` and `GET /api/v1/buyer/returns/{returnRequestId}/refund` (`RefundResponseDTO`: `id`, `returnRequestId`, `amount`, `status`, `processedAt`).
- Another buyer's order, invoice or return → 403 (ownership validated by the services).

---

## 7. Seller Endpoints (`SellerRestController`, `SellerPort`, role `SELLER`)

### 7.1. Conventions
- **Own resources without id.** `/account`, `/profile`, `/products` (list) and `/reports/performance` take no id: the controller passes the authenticated seller's id.
- **Warehouses:** the body has no `type`; a seller only registers `SELLER` warehouses, so the controller passes `WarehouseType.SELLER`.
- **Product state changes** use `PATCH` on action sub-resources: `/publish`, `/suspend`, `/discontinue`. A transition the current state does not allow → 409.
- **Inventory** is identified by product and warehouse. `SellerPort` has no query of a single own product: a product is read from the products list or, when published, from the public catalog.
- Another seller's warehouse, product or inventory → 403.

### 7.2. Products
- **Register** — `POST /api/v1/seller/products`
```json
{
  "type": "PHYSICAL",
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "price": 89900.00,
  "variants": [ { "type": "color", "value": "black" } ]
}
```
→ 201 `ProductResponseDTO`, status `DRAFT`. `type` is `PHYSICAL` or `DIGITAL` (another value → 400). `PUT /api/v1/seller/products/{productId}` takes the same body without `type`; `variants` replaces the current list.

### 7.3. Inventory
- **Initial stock** — `POST /api/v1/seller/inventories` with `{ "productId": "...", "warehouseId": "...", "quantity": 10 }` → 201 `InventoryResponseDTO`.
- **Inbound** — `POST /api/v1/seller/inventories/{productId}/{warehouseId}/inbounds` with `{ "quantity": 5 }` → 201 `InventoryResponseDTO`.
- **Response (`InventoryResponseDTO`):** `productId`, `productName`, `warehouseId`, `warehouseName`, `quantity`, `reservedQuantity`, `condition` and `movements` (`type`, `quantity`, `date`).

### 7.4. Performance report
- `GET /api/v1/seller/reports/performance?startDate=2026-10-01&endDate=2026-10-31` → 200 `SellerPerformanceReportResponseDTO` (`sellerId`, `sellerName`, `startDate`, `endDate`, `generatedAt`, `productsByStatus`, `ordersWithSellerProducts`, `unitsSold`, `amountSold`, `approvedReturns`, `returnRate`).
- Dates are ISO (`YYYY-MM-DD`), inclusive. Missing or malformed date → 400; `startDate` after `endDate` → 400 (`InvalidReportPeriodException`).
