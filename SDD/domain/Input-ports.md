# Input Ports (Role-Based Use Case Interfaces) — NexusMarket

## 1. Overview

Input ports define the entry contracts through which external delivery mechanisms (REST controllers in a later phase) interact with the core domain.

In this architecture, **input ports are organized strictly by system roles (`SystemRole`)**, plus a public access port for operations that do not require a specific role. This ensures that:

* Every role exposes a dedicated, cohesive interface with the exact operations allowed for that role.
* REST controllers and security filters evaluate role permissions directly against the targeted role port.
* Methods operate exclusively on Domain Models and Value Objects; they never receive DTOs.

There are no per-service use case interfaces: domain services are concrete classes and each role port is implemented by a use case adapter that injects them directly (see [Use Cases Adapters](../Adapters/Use-cases-adapters.md)).

---

# 2. General Principles

### 2.1 Role Isolation

Each system role interacts with the application through its role port in `nexusmarket.domain.ports.in`:

* `PublicAccessPort` (Public)
* `BuyerPort` (`BUYER`)
* `SellerPort` (`SELLER`)
* `LogisticsOperatorPort` (`LOGISTICS_OPERATOR`)
* `AdministratorPort` (`ADMINISTRATOR`)
* `SupervisorPort` (`SUPERVISOR`)

A role port only contains the operations allowed to its role (RG-03); the service validates the role again.

### 2.2 Parameters

* Protected methods receive the authenticated `User` (loaded from the token by the JWT filter) as `requestingUser`.
* Existing aggregates are referenced by identifier (`String`), because NexusMarket entities validate their invariants on construction and cannot be built with only an identifier.
* New information is received as primitives and Value Objects.

---

# 3. Input Port Definitions

## 3.1 PublicAccessPort (Public)

Public operations that do not require a specific role: buyer self-registration, login, logout and the public catalog.

```java
public interface PublicAccessPort {
    Buyer registerBuyerUser(String identificationNumber, String fullName, String email, String rawPassword, Address primaryAddress);
    AuthenticationResult login(String email, String rawPassword);
    void logout(User user);
    List<Product> consultCatalog();
    Product consultCatalogProduct(String productId);
}
```

| Method | Delegates to |
| --- | --- |
| `registerBuyerUser` | `RegisterBuyerUserService` |
| `login` | `LoginService` |
| `logout` | `LogoutService` |
| `consultCatalog` | `ConsultCatalogService` |
| `consultCatalogProduct` | `ConsultCatalogService` |

---

## 3.2 BuyerPort (`BUYER`)

Operations of a buyer over its own profile, cart, orders, invoices and return requests.

```java
public interface BuyerPort {
    User consultUser(User requestingUser, String userId);
    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);
    Buyer consultBuyer(User requestingUser, String buyerId);
    Buyer updateBuyerProfile(User requestingUser, String fullName, Address primaryAddress, List<Address> additionalAddresses);
    Cart addItemToCart(User requestingUser, String productId, Variant variant, int quantity);
    Cart removeItemFromCart(User requestingUser, String productId, Variant variant);
    Cart updateCartItemQuantity(User requestingUser, String productId, Variant variant, int quantity);
    Cart consultCart(User requestingUser, String buyerId);
    Cart clearCart(User requestingUser);
    Order confirmOrder(User requestingUser);
    Order validatePayment(User requestingUser, String orderId);
    Order consultOrder(User requestingUser, String orderId);
    List<Order> consultBuyerOrders(User requestingUser, String buyerId);
    Invoice consultInvoice(User requestingUser, String orderId);
    ReturnRequest requestReturn(User requestingUser, String orderId, String reason);
    ReturnRequest consultReturn(User requestingUser, String returnRequestId);
    Refund consultRefund(User requestingUser, String returnRequestId);
}
```

| Method | Delegates to |
| --- | --- |
| `consultUser` | `ConsultUserService` |
| `changeUserPassword` | `ChangeUserPasswordService` |
| `consultBuyer` | `ConsultBuyerService` |
| `updateBuyerProfile` | `UpdateBuyerProfileService` |
| `addItemToCart` | `AddItemToCartService` |
| `removeItemFromCart` | `RemoveItemFromCartService` |
| `updateCartItemQuantity` | `UpdateCartItemQuantityService` |
| `consultCart` | `ConsultCartService` |
| `clearCart` | `ClearCartService` |
| `confirmOrder` | `ConfirmOrderService` |
| `validatePayment` | `ValidatePaymentService` |
| `consultOrder` | `ConsultOrderService` |
| `consultBuyerOrders` | `ConsultBuyerOrdersService` |
| `consultInvoice` | `ConsultInvoiceService` |
| `requestReturn` | `RequestReturnService` |
| `consultReturn` | `ConsultReturnService` |
| `consultRefund` | `ConsultReturnService` |

---

## 3.3 SellerPort (`SELLER`)

Operations of a seller over its own profile, warehouses, products, inventory, the orders that contain its products and its own performance report.

```java
public interface SellerPort {
    User consultUser(User requestingUser, String userId);
    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);
    Seller consultSeller(User requestingUser, String sellerId);
    Seller updateSeller(User requestingUser, String sellerId, String fullName);
    Warehouse registerWarehouse(User requestingUser, String name, Address location, WarehouseType type);
    Warehouse consultWarehouse(User requestingUser, String warehouseId);
    List<Warehouse> consultWarehouses(User requestingUser);
    Warehouse updateWarehouse(User requestingUser, String warehouseId, String name, Address location);
    Product registerProduct(User requestingUser, ProductType type, String name, String description, BigDecimal price, List<Variant> variants);
    Product updateProduct(User requestingUser, String productId, String name, String description, BigDecimal price, List<Variant> variants);
    Product publishProduct(User requestingUser, String productId);
    Product suspendProduct(User requestingUser, String productId);
    Product discontinueProduct(User requestingUser, String productId);
    List<Product> consultSellerProducts(User requestingUser, String sellerId);
    Inventory registerInitialStock(User requestingUser, String productId, String warehouseId, int quantity);
    Inventory registerInventoryInbound(User requestingUser, String productId, String warehouseId, int quantity);
    List<Inventory> consultProductInventory(User requestingUser, String productId);
    List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId);
    Order consultOrder(User requestingUser, String orderId);
    SellerPerformanceReport consultSellerPerformanceReport(User requestingUser, String sellerId, ReportPeriod period);
}
```

| Method | Delegates to |
| --- | --- |
| `consultUser` | `ConsultUserService` |
| `changeUserPassword` | `ChangeUserPasswordService` |
| `consultSeller` | `ConsultSellerService` |
| `updateSeller` | `UpdateSellerService` |
| `registerWarehouse` | `RegisterWarehouseService` |
| `consultWarehouse` | `ConsultWarehouseService` |
| `consultWarehouses` | `ConsultWarehouseService` |
| `updateWarehouse` | `UpdateWarehouseService` |
| `registerProduct` | `RegisterProductService` |
| `updateProduct` | `UpdateProductService` |
| `publishProduct` | `PublishProductService` |
| `suspendProduct` | `SuspendProductService` |
| `discontinueProduct` | `DiscontinueProductService` |
| `consultSellerProducts` | `ConsultSellerProductsService` |
| `registerInitialStock` | `RegisterInitialStockService` |
| `registerInventoryInbound` | `RegisterInventoryInboundService` |
| `consultProductInventory` | `ConsultInventoryService` |
| `consultWarehouseInventory` | `ConsultInventoryService` |
| `consultOrder` | `ConsultOrderService` |
| `consultSellerPerformanceReport` | `ConsultSellerPerformanceReportService` |

---

## 3.4 LogisticsOperatorPort (`LOGISTICS_OPERATOR`)

Physical operation of warehouses: inventory movements, reservations, the shipment lifecycle and the inventory report.

```java
public interface LogisticsOperatorPort {
    User consultUser(User requestingUser, String userId);
    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);
    Warehouse consultWarehouse(User requestingUser, String warehouseId);
    List<Warehouse> consultWarehouses(User requestingUser);
    Inventory registerInitialStock(User requestingUser, String productId, String warehouseId, int quantity);
    Inventory registerInventoryInbound(User requestingUser, String productId, String warehouseId, int quantity);
    Inventory registerInventoryAdjustment(User requestingUser, String productId, String warehouseId, int quantity, InventoryCondition condition);
    Inventory reserveInventory(User requestingUser, String productId, String warehouseId, int quantity);
    Inventory releaseInventoryReservation(User requestingUser, String productId, String warehouseId, int quantity);
    List<Inventory> consultProductInventory(User requestingUser, String productId);
    List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId);
    Order consultOrder(User requestingUser, String orderId);
    List<Shipment> createShipments(User requestingUser, String orderId);
    Shipment packShipment(User requestingUser, String shipmentId);
    Shipment dispatchShipment(User requestingUser, String shipmentId);
    Shipment confirmDelivery(User requestingUser, String shipmentId);
    InventoryReport consultInventoryReport(User requestingUser, int lowStockThreshold);
}
```

| Method | Delegates to |
| --- | --- |
| `consultUser` | `ConsultUserService` |
| `changeUserPassword` | `ChangeUserPasswordService` |
| `consultWarehouse` | `ConsultWarehouseService` |
| `consultWarehouses` | `ConsultWarehouseService` |
| `registerInitialStock` | `RegisterInitialStockService` |
| `registerInventoryInbound` | `RegisterInventoryInboundService` |
| `registerInventoryAdjustment` | `RegisterInventoryAdjustmentService` |
| `reserveInventory` | `ReserveInventoryService` |
| `releaseInventoryReservation` | `ReleaseInventoryReservationService` |
| `consultProductInventory` | `ConsultInventoryService` |
| `consultWarehouseInventory` | `ConsultInventoryService` |
| `consultOrder` | `ConsultOrderService` |
| `createShipments` | `CreateShipmentsService` |
| `packShipment` | `PackShipmentService` |
| `dispatchShipment` | `DispatchShipmentService` |
| `confirmDelivery` | `ConfirmDeliveryService` |
| `consultInventoryReport` | `ConsultInventoryReportService` |

---

## 3.5 AdministratorPort (`ADMINISTRATOR`)

Administration of users, sellers, buyers' commercial status, Marketplace warehouses, returns, refunds, audit logs and administrative reports.

```java
public interface AdministratorPort {
    User registerStaffUser(User requestingUser, SystemRole role, String identificationNumber, String fullName, String email, String rawPassword);
    User consultUser(User requestingUser, String userId);
    User changeUserStatus(User requestingUser, String userId, UserStatus newStatus);
    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);
    Seller registerSeller(User requestingUser, String identificationNumber, String fullName, String email, String rawPassword, String warehouseName, Address warehouseLocation);
    Seller consultSeller(User requestingUser, String sellerId);
    Seller updateSeller(User requestingUser, String sellerId, String fullName);
    Buyer consultBuyer(User requestingUser, String buyerId);
    Buyer changeBuyerCommercialStatus(User requestingUser, String buyerId, CommercialStatus newStatus);
    Warehouse registerWarehouse(User requestingUser, String name, Address location, WarehouseType type);
    Warehouse consultWarehouse(User requestingUser, String warehouseId);
    List<Warehouse> consultWarehouses(User requestingUser);
    Warehouse updateWarehouse(User requestingUser, String warehouseId, String name, Address location);
    List<Product> consultSellerProducts(User requestingUser, String sellerId);
    Invoice consultInvoice(User requestingUser, String orderId);
    ReturnRequest approveReturn(User requestingUser, String returnRequestId);
    ReturnRequest rejectReturn(User requestingUser, String returnRequestId);
    Refund processRefund(User requestingUser, String refundId);
    ReturnRequest consultReturn(User requestingUser, String returnRequestId);
    Refund consultRefund(User requestingUser, String returnRequestId);
    List<AuditLog> consultAuditLogs(User requestingUser);
    List<AuditLog> consultAuditLogsByUser(User requestingUser, String userId);
    List<AuditLog> consultAuditLogsByEntity(User requestingUser, AuditEntityType entityType, String entityId);
    CommercialReport consultCommercialReport(User requestingUser, ReportPeriod period);
    SellerPerformanceReport consultSellerPerformanceReport(User requestingUser, String sellerId, ReportPeriod period);
    InventoryReport consultInventoryReport(User requestingUser, int lowStockThreshold);
    LogisticsReport consultLogisticsReport(User requestingUser, ReportPeriod period);
}
```

| Method | Delegates to |
| --- | --- |
| `registerStaffUser` | `RegisterStaffUserService` |
| `consultUser` | `ConsultUserService` |
| `changeUserStatus` | `ChangeUserStatusService` |
| `changeUserPassword` | `ChangeUserPasswordService` |
| `registerSeller` | `RegisterSellerService` |
| `consultSeller` | `ConsultSellerService` |
| `updateSeller` | `UpdateSellerService` |
| `consultBuyer` | `ConsultBuyerService` |
| `changeBuyerCommercialStatus` | `ChangeBuyerCommercialStatusService` |
| `registerWarehouse` | `RegisterWarehouseService` |
| `consultWarehouse` | `ConsultWarehouseService` |
| `consultWarehouses` | `ConsultWarehouseService` |
| `updateWarehouse` | `UpdateWarehouseService` |
| `consultSellerProducts` | `ConsultSellerProductsService` |
| `consultInvoice` | `ConsultInvoiceService` |
| `approveReturn` | `ApproveReturnService` |
| `rejectReturn` | `RejectReturnService` |
| `processRefund` | `ProcessRefundService` |
| `consultReturn` | `ConsultReturnService` |
| `consultRefund` | `ConsultReturnService` |
| `consultAuditLogs` | `ConsultAuditLogsService` |
| `consultAuditLogsByUser` | `ConsultAuditLogsService` |
| `consultAuditLogsByEntity` | `ConsultAuditLogsService` |
| `consultCommercialReport` | `ConsultCommercialReportService` |
| `consultSellerPerformanceReport` | `ConsultSellerPerformanceReportService` |
| `consultInventoryReport` | `ConsultInventoryReportService` |
| `consultLogisticsReport` | `ConsultLogisticsReportService` |

---

## 3.6 SupervisorPort (`SUPERVISOR`)

Read-only oversight: consultation operations across all business domains. It never modifies information.

```java
public interface SupervisorPort {
    User consultUser(User requestingUser, String userId);
    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);
    Seller consultSeller(User requestingUser, String sellerId);
    Buyer consultBuyer(User requestingUser, String buyerId);
    Warehouse consultWarehouse(User requestingUser, String warehouseId);
    List<Warehouse> consultWarehouses(User requestingUser);
    List<Product> consultSellerProducts(User requestingUser, String sellerId);
    List<Inventory> consultProductInventory(User requestingUser, String productId);
    List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId);
    Cart consultCart(User requestingUser, String buyerId);
    Order consultOrder(User requestingUser, String orderId);
    List<Order> consultBuyerOrders(User requestingUser, String buyerId);
    Invoice consultInvoice(User requestingUser, String orderId);
    ReturnRequest consultReturn(User requestingUser, String returnRequestId);
    Refund consultRefund(User requestingUser, String returnRequestId);
    List<AuditLog> consultAuditLogs(User requestingUser);
    List<AuditLog> consultAuditLogsByUser(User requestingUser, String userId);
    List<AuditLog> consultAuditLogsByEntity(User requestingUser, AuditEntityType entityType, String entityId);
    CommercialReport consultCommercialReport(User requestingUser, ReportPeriod period);
    SellerPerformanceReport consultSellerPerformanceReport(User requestingUser, String sellerId, ReportPeriod period);
    InventoryReport consultInventoryReport(User requestingUser, int lowStockThreshold);
    LogisticsReport consultLogisticsReport(User requestingUser, ReportPeriod period);
}
```

| Method | Delegates to |
| --- | --- |
| `consultUser` | `ConsultUserService` |
| `changeUserPassword` | `ChangeUserPasswordService` |
| `consultSeller` | `ConsultSellerService` |
| `consultBuyer` | `ConsultBuyerService` |
| `consultWarehouse` | `ConsultWarehouseService` |
| `consultWarehouses` | `ConsultWarehouseService` |
| `consultSellerProducts` | `ConsultSellerProductsService` |
| `consultProductInventory` | `ConsultInventoryService` |
| `consultWarehouseInventory` | `ConsultInventoryService` |
| `consultCart` | `ConsultCartService` |
| `consultOrder` | `ConsultOrderService` |
| `consultBuyerOrders` | `ConsultBuyerOrdersService` |
| `consultInvoice` | `ConsultInvoiceService` |
| `consultReturn` | `ConsultReturnService` |
| `consultRefund` | `ConsultReturnService` |
| `consultAuditLogs` | `ConsultAuditLogsService` |
| `consultAuditLogsByUser` | `ConsultAuditLogsService` |
| `consultAuditLogsByEntity` | `ConsultAuditLogsService` |
| `consultCommercialReport` | `ConsultCommercialReportService` |
| `consultSellerPerformanceReport` | `ConsultSellerPerformanceReportService` |
| `consultInventoryReport` | `ConsultInventoryReportService` |
| `consultLogisticsReport` | `ConsultLogisticsReportService` |

---
