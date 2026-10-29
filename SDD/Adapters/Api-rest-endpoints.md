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
| `LogisticsOperatorRestController` | `/api/v1/logistics` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort` |
| `AdministratorRestController` | `/api/v1/admin` | `ADMINISTRATOR` | `AdministratorPort` |
| `SupervisorRestController` | `/api/v1/supervisor` | `SUPERVISOR` | `SupervisorPort` |

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
| 43 | `GET` | `/api/v1/logistics/account` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.consultUser` | — | 200 `UserResponseDTO` | 401, 403 |
| 44 | `PUT` | `/api/v1/logistics/account/password` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.changeUserPassword` | `ChangePasswordRequestDTO` | 204 | 400, 401, 403 |
| 45 | `GET` | `/api/v1/logistics/warehouses` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.consultWarehouses` | — | 200 `List<WarehouseResponseDTO>` | 401, 403 |
| 46 | `GET` | `/api/v1/logistics/warehouses/{warehouseId}` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.consultWarehouse` | — | 200 `WarehouseResponseDTO` | 404 |
| 47 | `POST` | `/api/v1/logistics/inventories` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.registerInitialStock` | `InitialStockRequestDTO` | 201 `InventoryResponseDTO` | 400, 404, 409 |
| 48 | `POST` | `/api/v1/logistics/inventories/{productId}/{warehouseId}/inbounds` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.registerInventoryInbound` | `StockQuantityRequestDTO` | 201 `InventoryResponseDTO` | 400, 404, 409 |
| 49 | `POST` | `/api/v1/logistics/inventories/{productId}/{warehouseId}/adjustments` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.registerInventoryAdjustment` | `InventoryAdjustmentRequestDTO` | 201 `InventoryResponseDTO` | 400, 404, 409 |
| 50 | `POST` | `/api/v1/logistics/inventories/{productId}/{warehouseId}/reservations` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.reserveInventory` | `StockQuantityRequestDTO` | 201 `InventoryResponseDTO` | 400, 404, 409 |
| 51 | `POST` | `/api/v1/logistics/inventories/{productId}/{warehouseId}/reservation-releases` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.releaseInventoryReservation` | `StockQuantityRequestDTO` | 201 `InventoryResponseDTO` | 400, 404, 409 |
| 52 | `GET` | `/api/v1/logistics/products/{productId}/inventories` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.consultProductInventory` | — | 200 `List<InventoryResponseDTO>` | 404 |
| 53 | `GET` | `/api/v1/logistics/warehouses/{warehouseId}/inventories` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.consultWarehouseInventory` | — | 200 `List<InventoryResponseDTO>` | 404 |
| 54 | `GET` | `/api/v1/logistics/orders/{orderId}` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.consultOrder` | — | 200 `OrderResponseDTO` | 404 |
| 55 | `POST` | `/api/v1/logistics/orders/{orderId}/shipments` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.createShipments` | — | 201 `List<ShipmentResponseDTO>` | 400, 404, 409 |
| 56 | `PATCH` | `/api/v1/logistics/shipments/{shipmentId}/pack` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.packShipment` | — | 200 `ShipmentResponseDTO` | 403, 404, 409 |
| 57 | `PATCH` | `/api/v1/logistics/shipments/{shipmentId}/dispatch` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.dispatchShipment` | — | 200 `ShipmentResponseDTO` | 403, 404, 409 |
| 58 | `PATCH` | `/api/v1/logistics/shipments/{shipmentId}/deliver` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.confirmDelivery` | — | 200 `ShipmentResponseDTO` | 403, 404, 409 |
| 59 | `GET` | `/api/v1/logistics/reports/inventory` | `LOGISTICS_OPERATOR` | `LogisticsOperatorPort.consultInventoryReport` | query `lowStockThreshold` | 200 `InventoryReportResponseDTO` | 400 |
| 60 | `PUT` | `/api/v1/admin/account/password` | `ADMINISTRATOR` | `AdministratorPort.changeUserPassword` | `ChangePasswordRequestDTO` | 204 | 400, 401, 403 |
| 61 | `POST` | `/api/v1/admin/staff-users` | `ADMINISTRATOR` | `AdministratorPort.registerStaffUser` | `RegisterStaffUserRequestDTO` | 201 `UserResponseDTO` | 400, 409 |
| 62 | `GET` | `/api/v1/admin/users/{userId}` | `ADMINISTRATOR` | `AdministratorPort.consultUser` | — | 200 `UserResponseDTO` | 404 |
| 63 | `PATCH` | `/api/v1/admin/users/{userId}/status` | `ADMINISTRATOR` | `AdministratorPort.changeUserStatus` | `ChangeUserStatusRequestDTO` | 200 `UserResponseDTO` | 400, 403, 404 |
| 64 | `POST` | `/api/v1/admin/sellers` | `ADMINISTRATOR` | `AdministratorPort.registerSeller` | `RegisterSellerRequestDTO` | 201 `SellerRegistrationResponseDTO` | 400, 409 |
| 65 | `GET` | `/api/v1/admin/sellers/{sellerId}` | `ADMINISTRATOR` | `AdministratorPort.consultSeller` | — | 200 `SellerResponseDTO` | 404 |
| 66 | `PATCH` | `/api/v1/admin/sellers/{sellerId}` | `ADMINISTRATOR` | `AdministratorPort.updateSeller` | `UpdateSellerRequestDTO` | 200 `SellerResponseDTO` | 400, 404 |
| 67 | `GET` | `/api/v1/admin/sellers/{sellerId}/products` | `ADMINISTRATOR` | `AdministratorPort.consultSellerProducts` | — | 200 `List<ProductResponseDTO>` | 404 |
| 68 | `GET` | `/api/v1/admin/buyers/{buyerId}` | `ADMINISTRATOR` | `AdministratorPort.consultBuyer` | — | 200 `BuyerResponseDTO` | 404 |
| 69 | `PATCH` | `/api/v1/admin/buyers/{buyerId}/commercial-status` | `ADMINISTRATOR` | `AdministratorPort.changeBuyerCommercialStatus` | `ChangeCommercialStatusRequestDTO` | 200 `BuyerResponseDTO` | 400, 404 |
| 70 | `POST` | `/api/v1/admin/warehouses` | `ADMINISTRATOR` | `AdministratorPort.registerWarehouse` | `WarehouseRequestDTO` | 201 `WarehouseResponseDTO` | 400 |
| 71 | `GET` | `/api/v1/admin/warehouses` | `ADMINISTRATOR` | `AdministratorPort.consultWarehouses` | — | 200 `List<WarehouseResponseDTO>` | 401, 403 |
| 72 | `GET` | `/api/v1/admin/warehouses/{warehouseId}` | `ADMINISTRATOR` | `AdministratorPort.consultWarehouse` | — | 200 `WarehouseResponseDTO` | 404 |
| 73 | `PUT` | `/api/v1/admin/warehouses/{warehouseId}` | `ADMINISTRATOR` | `AdministratorPort.updateWarehouse` | `WarehouseRequestDTO` | 200 `WarehouseResponseDTO` | 400, 404 |
| 74 | `GET` | `/api/v1/admin/orders/{orderId}/invoice` | `ADMINISTRATOR` | `AdministratorPort.consultInvoice` | — | 200 `InvoiceResponseDTO` | 404 |
| 75 | `GET` | `/api/v1/admin/returns/{returnRequestId}` | `ADMINISTRATOR` | `AdministratorPort.consultReturn` | — | 200 `ReturnRequestResponseDTO` | 404 |
| 76 | `PATCH` | `/api/v1/admin/returns/{returnRequestId}/approve` | `ADMINISTRATOR` | `AdministratorPort.approveReturn` | — | 200 `ReturnRequestResponseDTO` | 404, 409 |
| 77 | `PATCH` | `/api/v1/admin/returns/{returnRequestId}/reject` | `ADMINISTRATOR` | `AdministratorPort.rejectReturn` | — | 200 `ReturnRequestResponseDTO` | 404, 409 |
| 78 | `GET` | `/api/v1/admin/returns/{returnRequestId}/refund` | `ADMINISTRATOR` | `AdministratorPort.consultRefund` | — | 200 `RefundResponseDTO` | 404 |
| 79 | `PATCH` | `/api/v1/admin/refunds/{refundId}/process` | `ADMINISTRATOR` | `AdministratorPort.processRefund` | — | 200 `RefundResponseDTO` | 404, 409 |
| 80 | `GET` | `/api/v1/admin/audit-logs` | `ADMINISTRATOR` | `AdministratorPort.consultAuditLogs` | — | 200 `List<AuditLogResponseDTO>` | 401, 403 |
| 81 | `GET` | `/api/v1/admin/audit-logs/users/{userId}` | `ADMINISTRATOR` | `AdministratorPort.consultAuditLogsByUser` | — | 200 `List<AuditLogResponseDTO>` | 404 |
| 82 | `GET` | `/api/v1/admin/audit-logs/entities/{entityType}/{entityId}` | `ADMINISTRATOR` | `AdministratorPort.consultAuditLogsByEntity` | — | 200 `List<AuditLogResponseDTO>` | 400 |
| 83 | `GET` | `/api/v1/admin/reports/commercial` | `ADMINISTRATOR` | `AdministratorPort.consultCommercialReport` | query `startDate`, `endDate` | 200 `CommercialReportResponseDTO` | 400 |
| 84 | `GET` | `/api/v1/admin/reports/sellers/{sellerId}/performance` | `ADMINISTRATOR` | `AdministratorPort.consultSellerPerformanceReport` | query `startDate`, `endDate` | 200 `SellerPerformanceReportResponseDTO` | 400, 404 |
| 85 | `GET` | `/api/v1/admin/reports/inventory` | `ADMINISTRATOR` | `AdministratorPort.consultInventoryReport` | query `lowStockThreshold` | 200 `InventoryReportResponseDTO` | 400 |
| 86 | `GET` | `/api/v1/admin/reports/logistics` | `ADMINISTRATOR` | `AdministratorPort.consultLogisticsReport` | query `startDate`, `endDate` | 200 `LogisticsReportResponseDTO` | 400 |
| 87 | `PUT` | `/api/v1/supervisor/account/password` | `SUPERVISOR` | `SupervisorPort.changeUserPassword` | `ChangePasswordRequestDTO` | 204 | 400, 401, 403 |
| 88 | `GET` | `/api/v1/supervisor/users/{userId}` | `SUPERVISOR` | `SupervisorPort.consultUser` | — | 200 `UserResponseDTO` | 404 |
| 89 | `GET` | `/api/v1/supervisor/sellers/{sellerId}` | `SUPERVISOR` | `SupervisorPort.consultSeller` | — | 200 `SellerResponseDTO` | 404 |
| 90 | `GET` | `/api/v1/supervisor/buyers/{buyerId}` | `SUPERVISOR` | `SupervisorPort.consultBuyer` | — | 200 `BuyerResponseDTO` | 404 |
| 91 | `GET` | `/api/v1/supervisor/warehouses` | `SUPERVISOR` | `SupervisorPort.consultWarehouses` | — | 200 `List<WarehouseResponseDTO>` | 401, 403 |
| 92 | `GET` | `/api/v1/supervisor/warehouses/{warehouseId}` | `SUPERVISOR` | `SupervisorPort.consultWarehouse` | — | 200 `WarehouseResponseDTO` | 404 |
| 93 | `GET` | `/api/v1/supervisor/sellers/{sellerId}/products` | `SUPERVISOR` | `SupervisorPort.consultSellerProducts` | — | 200 `List<ProductResponseDTO>` | 404 |
| 94 | `GET` | `/api/v1/supervisor/products/{productId}/inventories` | `SUPERVISOR` | `SupervisorPort.consultProductInventory` | — | 200 `List<InventoryResponseDTO>` | 404 |
| 95 | `GET` | `/api/v1/supervisor/warehouses/{warehouseId}/inventories` | `SUPERVISOR` | `SupervisorPort.consultWarehouseInventory` | — | 200 `List<InventoryResponseDTO>` | 404 |
| 96 | `GET` | `/api/v1/supervisor/buyers/{buyerId}/cart` | `SUPERVISOR` | `SupervisorPort.consultCart` | — | 200 `CartResponseDTO` | 404 |
| 97 | `GET` | `/api/v1/supervisor/orders/{orderId}` | `SUPERVISOR` | `SupervisorPort.consultOrder` | — | 200 `OrderResponseDTO` | 404 |
| 98 | `GET` | `/api/v1/supervisor/buyers/{buyerId}/orders` | `SUPERVISOR` | `SupervisorPort.consultBuyerOrders` | — | 200 `List<OrderResponseDTO>` | 404 |
| 99 | `GET` | `/api/v1/supervisor/orders/{orderId}/invoice` | `SUPERVISOR` | `SupervisorPort.consultInvoice` | — | 200 `InvoiceResponseDTO` | 404 |
| 100 | `GET` | `/api/v1/supervisor/returns/{returnRequestId}` | `SUPERVISOR` | `SupervisorPort.consultReturn` | — | 200 `ReturnRequestResponseDTO` | 404 |
| 101 | `GET` | `/api/v1/supervisor/returns/{returnRequestId}/refund` | `SUPERVISOR` | `SupervisorPort.consultRefund` | — | 200 `RefundResponseDTO` | 404 |
| 102 | `GET` | `/api/v1/supervisor/audit-logs` | `SUPERVISOR` | `SupervisorPort.consultAuditLogs` | — | 200 `List<AuditLogResponseDTO>` | 401, 403 |
| 103 | `GET` | `/api/v1/supervisor/audit-logs/users/{userId}` | `SUPERVISOR` | `SupervisorPort.consultAuditLogsByUser` | — | 200 `List<AuditLogResponseDTO>` | 404 |
| 104 | `GET` | `/api/v1/supervisor/audit-logs/entities/{entityType}/{entityId}` | `SUPERVISOR` | `SupervisorPort.consultAuditLogsByEntity` | — | 200 `List<AuditLogResponseDTO>` | 400 |
| 105 | `GET` | `/api/v1/supervisor/reports/commercial` | `SUPERVISOR` | `SupervisorPort.consultCommercialReport` | query `startDate`, `endDate` | 200 `CommercialReportResponseDTO` | 400 |
| 106 | `GET` | `/api/v1/supervisor/reports/sellers/{sellerId}/performance` | `SUPERVISOR` | `SupervisorPort.consultSellerPerformanceReport` | query `startDate`, `endDate` | 200 `SellerPerformanceReportResponseDTO` | 400, 404 |
| 107 | `GET` | `/api/v1/supervisor/reports/inventory` | `SUPERVISOR` | `SupervisorPort.consultInventoryReport` | query `lowStockThreshold` | 200 `InventoryReportResponseDTO` | 400 |
| 108 | `GET` | `/api/v1/supervisor/reports/logistics` | `SUPERVISOR` | `SupervisorPort.consultLogisticsReport` | query `startDate`, `endDate` | 200 `LogisticsReportResponseDTO` | 400 |

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

---

## 8. Logistics Operator Endpoints (`LogisticsOperatorRestController`, `LogisticsOperatorPort`, role `LOGISTICS_OPERATOR`)

### 8.1. Conventions
- **Account without id:** an operator only consults itself (RG-03), so `/account` takes no id.
- The operator works on **any product and warehouse** (including `MARKETPLACE` warehouses).
- **Inventory movements** are `POST` sub-resources of `/inventories/{productId}/{warehouseId}`: `/inbounds`, `/adjustments`, `/reservations`, `/reservation-releases`. Each one creates a movement and returns the updated inventory (201).
- **Shipments:** `POST /orders/{orderId}/shipments` creates one shipment per origin warehouse (only for `PAID` orders with physical items; the operator becomes the shipment's operator). Transitions use `PATCH`: `/pack` (`PREPARING` → `PACKED`), `/dispatch` (→ `IN_TRANSIT`, registers the `SALE_OUTBOUND` of the reserved stock and marks the order `SHIPPED`), `/deliver` (→ `DELIVERED`; the order becomes `DELIVERED` when all its shipments are delivered). Another operator's shipment → 403; a transition the state does not allow → 409.

### 8.2. Inventory adjustment
- `POST /api/v1/logistics/inventories/{productId}/{warehouseId}/adjustments`
```json
{ "quantity": -3, "condition": "DAMAGED" }
```
`quantity` is a signed correction of the available stock (0 = none); `condition` is an optional `InventoryCondition` code. At least one of them must change something, and the stock can never become negative (400).

### 8.3. Inventory report
- `GET /api/v1/logistics/reports/inventory?lowStockThreshold=5` → 200 `InventoryReportResponseDTO` (`lowStockThreshold`, `generatedAt`, `warehouses` with `warehouseId`, `warehouseName`, `warehouseType`, `totalUnits`, `reservedUnits`, `damagedUnits`, and `lowStockProducts` with `productId`, `productName`, `sellerId`, `availableUnits`).
- `lowStockThreshold` is required and must be greater than 0 (`@Positive`): `0` or `-1` → 400.

### 8.4. Inventory through a purchase (verified)

| Stage | `quantity` | `reservedQuantity` | Movement added |
| --- | ---: | ---: | --- |
| Initial stock 10 | 10 | 0 | `INBOUND` 10 |
| Order of 2 confirmed | 8 | 2 | `RESERVATION` 2 |
| Shipment dispatched | 8 | 0 | `SALE_OUTBOUND` 2 |
| Return approved | 10 | 0 | `RETURN` 2 |

---

## 9. Administrator Endpoints (`AdministratorRestController`, `AdministratorPort`, role `ADMINISTRATOR`)

### 9.1. Conventions
- The administrator consults any user, seller or buyer by id (`/users/{userId}`, `/sellers/{sellerId}`, `/buyers/{buyerId}`).
- **Warehouses:** the body has no `type`; an administrator only registers `MARKETPLACE` warehouses, so the controller passes `WarehouseType.MARKETPLACE`.
- **Catalog codes** in bodies and paths (`role`, `status`, `commercialStatus`, `entityType`) are converted with `fromCode`; an unknown code → 400.
- **Returns and refunds** use `PATCH` actions: `/returns/{id}/approve` (adds a `RETURN` movement per physical item and creates a `PENDING` refund for the invoice total), `/returns/{id}/reject`, `/refunds/{refundId}/process` (`PENDING` → `PROCESSED` with `processedAt`). The `refundId` is read from `GET /returns/{id}/refund`.

### 9.2. Register seller
- `POST /api/v1/admin/sellers`
```json
{
  "identificationNumber": "900123456",
  "fullName": "Tech Store",
  "email": "tech.store@gmail.com",
  "password": "Seller123*",
  "warehouseName": "Tech Store Medellin",
  "warehouseLocation": { "line1": "Calle 1 # 2-3", "city": "Medellin", "state": "Antioquia", "country": "Colombia" }
}
```
- **Response (`SellerRegistrationResponseDTO` - HTTP 201):** `{ "seller": SellerResponseDTO, "firstWarehouse": WarehouseResponseDTO }`. A seller loaded later does not carry its warehouses, so the first warehouse id is returned here.
- Errors: 400 (validation), 409 (email — compared normalized — or identification number already registered).

### 9.3. Users
- `POST /api/v1/admin/staff-users` registers a `LOGISTICS_OPERATOR` or `SUPERVISOR` (`role` code; another role → 400).
- `PATCH /api/v1/admin/users/{userId}/status` with `{ "status": "BLOCKED" }` (`ACTIVE`, `INACTIVE` or `BLOCKED`). Changing the status advances the user's `authTokenVersion`: its tokens stop working immediately (401) and a `BLOCKED`/`INACTIVE` user cannot log in (403). **An administrator cannot change its own status → 403** ("An administrator cannot change its own status.").

### 9.4. Audit
| Route | Port method |
| --- | --- |
| `GET /api/v1/admin/audit-logs` | `consultAuditLogs` |
| `GET /api/v1/admin/audit-logs/users/{userId}` | `consultAuditLogsByUser` |
| `GET /api/v1/admin/audit-logs/entities/{entityType}/{entityId}` | `consultAuditLogsByEntity` (`entityType` is an `AuditEntityType` code, for example `ORDER`, `INVENTORY`, `USER`) |

`AuditLogResponseDTO`: `id`, `action`, `performedById`, `performedByEmail`, `role`, `entityType`, `entityId`, `occurredAt`, `details`.

### 9.5. Reports
| Route | Response |
| --- | --- |
| `GET /api/v1/admin/reports/commercial?startDate&endDate` | `CommercialReportResponseDTO` (`ordersByStatus`, `totalOrders`, `totalInvoiced`, `totalRefunded`, `netRevenue`) |
| `GET /api/v1/admin/reports/sellers/{sellerId}/performance?startDate&endDate` | `SellerPerformanceReportResponseDTO` |
| `GET /api/v1/admin/reports/inventory?lowStockThreshold=` | `InventoryReportResponseDTO` (`lowStockThreshold` > 0) |
| `GET /api/v1/admin/reports/logistics?startDate&endDate` | `LogisticsReportResponseDTO` (`shipmentsByStatus`, `totalShipments`, `pendingDispatch`, `delivered`) |

Dates are ISO (`YYYY-MM-DD`) and inclusive; a missing or malformed date, or `startDate` after `endDate`, → 400.

---

## 10. Supervisor Endpoints (`SupervisorRestController`, `SupervisorPort`, role `SUPERVISOR`)

- **Read-only:** every endpoint is a `GET`, except `PUT /api/v1/supervisor/account/password` (its own credential). Any attempt to modify information goes to a route of another role and is rejected with **403** by `SecurityConfig` (for example `POST /api/v1/admin/sellers` or `PATCH /api/v1/seller/products/{productId}/publish`).
- Same route shapes as the administrator for users, sellers, buyers, warehouses, audit and reports, plus `GET /buyers/{buyerId}/cart`, `GET /buyers/{buyerId}/orders`, `GET /orders/{orderId}`, `GET /products/{productId}/inventories` and `GET /warehouses/{warehouseId}/inventories`.
- Same validations: report dates as in section 9.5, `lowStockThreshold` > 0, `entityType` as an `AuditEntityType` code.

---

## 11. Verified End-to-End Flow (step 4C)

Executed against the running application (66/66 steps as expected):

| Area | Checks |
| --- | --- |
| Seller onboarding | Seller with first warehouse 201; same email in upper case 409; same document 409; staff users 201 |
| Physical purchase | Product registered (`DRAFT`) and published; stock 10; cart → order `PENDING_PAYMENT` → paid `PAID` → invoice; shipments created, packed, dispatched, delivered → order `DELIVERED`; return requested, approved; refund `PENDING` → `PROCESSED`; inventory as in section 8.4 |
| Digital purchase | Order of a digital product paid → `DELIVERED` with no shipments |
| Reports and audit | Seller performance, commercial, inventory and logistics reports 200; order audit `ORDER_CONFIRMED`, `PAYMENT_APPROVED`, `ORDER_DELIVERED`; inverted dates 400; `lowStockThreshold` 0 and -1 400; unknown `entityType` 400 |
| Buyer ownership | Another buyer consulting the order, invoice or return 403 |
| Seller ownership | Another seller consulting an order without its products, updating or suspending a foreign product, registering stock in a foreign warehouse or an inbound of a foreign product 403 |
| Supervisor | Registering a seller or publishing a product 403 |
| User status | Blocked buyer: previous token 401, login 403; reactivated: login 200 |
| Self status | Administrator blocking itself 403 |
