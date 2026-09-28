# Input Ports — NexusMarket

## 1. Overview

Input ports define the entry contracts through which external delivery mechanisms (REST controllers in a later phase) interact with the domain. They live in `nexusmarket.domain.ports.in` and exist at two levels:

* **Use case interfaces** (`<Name>UseCase`): one per domain service; implemented by the service `<Name>Service`.
* **Role ports** (`<Role>Port`): one per system role plus a public port; they group the operations each role is allowed to execute and are implemented by the use case adapters (see [Use Cases Adapters](../Adapters/Use-cases-adapters.md)).

## 2. General Principles

* Methods operate on Domain Models and Value Objects; they never receive DTOs.
* Protected methods receive the authenticated `User` (reconstructed from the token) as `requestingUser`.
* Existing aggregates are referenced by identifier (`String`).
* A role port only contains the operations allowed to its role (RG-03); the service validates the role again.

---

# 3. Use Case Interfaces

## User Management

### RegisterBuyerUserUseCase

Implemented by `RegisterBuyerUserService`.

```java
public interface RegisterBuyerUserUseCase {

    Buyer registerBuyerUser(String identificationNumber, String fullName, String email, String rawPassword, Address primaryAddress);
}
```

### RegisterStaffUserUseCase

Implemented by `RegisterStaffUserService`.

```java
public interface RegisterStaffUserUseCase {

    User registerStaffUser(User requestingUser, SystemRole role, String identificationNumber, String fullName, String email, String rawPassword);
}
```

### LoginUseCase

Implemented by `LoginService`.

```java
public interface LoginUseCase {

    String login(String email, String rawPassword);
}
```

### ConsultUserUseCase

Implemented by `ConsultUserService`.

```java
public interface ConsultUserUseCase {

    User consultUser(User requestingUser, String userId);
}
```

### ChangeUserStatusUseCase

Implemented by `ChangeUserStatusService`.

```java
public interface ChangeUserStatusUseCase {

    User changeUserStatus(User requestingUser, String userId, UserStatus newStatus);
}
```

### ChangeUserPasswordUseCase

Implemented by `ChangeUserPasswordService`.

```java
public interface ChangeUserPasswordUseCase {

    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);
}
```

## Seller Management

### RegisterSellerUseCase

Implemented by `RegisterSellerService`.

```java
public interface RegisterSellerUseCase {

    Seller registerSeller(User requestingUser, String identificationNumber, String fullName, String email, String rawPassword, String warehouseName, Address warehouseLocation);
}
```

### ConsultSellerUseCase

Implemented by `ConsultSellerService`.

```java
public interface ConsultSellerUseCase {

    Seller consultSeller(User requestingUser, String sellerId);
}
```

### UpdateSellerUseCase

Implemented by `UpdateSellerService`.

```java
public interface UpdateSellerUseCase {

    Seller updateSeller(User requestingUser, String sellerId, String fullName);
}
```

## Buyer Management

### ConsultBuyerUseCase

Implemented by `ConsultBuyerService`.

```java
public interface ConsultBuyerUseCase {

    Buyer consultBuyer(User requestingUser, String buyerId);
}
```

### UpdateBuyerProfileUseCase

Implemented by `UpdateBuyerProfileService`.

```java
public interface UpdateBuyerProfileUseCase {

    Buyer updateBuyerProfile(User requestingUser, String fullName, Address primaryAddress, List<Address> additionalAddresses);
}
```

### ChangeBuyerCommercialStatusUseCase

Implemented by `ChangeBuyerCommercialStatusService`.

```java
public interface ChangeBuyerCommercialStatusUseCase {

    Buyer changeBuyerCommercialStatus(User requestingUser, String buyerId, CommercialStatus newStatus);
}
```

## Warehouse Management

### RegisterWarehouseUseCase

Implemented by `RegisterWarehouseService`.

```java
public interface RegisterWarehouseUseCase {

    Warehouse registerWarehouse(User requestingUser, String name, Address location, WarehouseType type);
}
```

### ConsultWarehouseUseCase

Implemented by `ConsultWarehouseService`.

```java
public interface ConsultWarehouseUseCase {

    Warehouse consultWarehouse(User requestingUser, String warehouseId);
    List<Warehouse> consultWarehouses(User requestingUser);
}
```

### UpdateWarehouseUseCase

Implemented by `UpdateWarehouseService`.

```java
public interface UpdateWarehouseUseCase {

    Warehouse updateWarehouse(User requestingUser, String warehouseId, String name, Address location);
}
```

## Catalog Management

### RegisterProductUseCase

Implemented by `RegisterProductService`.

```java
public interface RegisterProductUseCase {

    Product registerProduct(User requestingUser, ProductType type, String name, String description, BigDecimal price, List<Variant> variants);
}
```

### UpdateProductUseCase

Implemented by `UpdateProductService`.

```java
public interface UpdateProductUseCase {

    Product updateProduct(User requestingUser, String productId, String name, String description, BigDecimal price, List<Variant> variants);
}
```

### PublishProductUseCase

Implemented by `PublishProductService`.

```java
public interface PublishProductUseCase {

    Product publishProduct(User requestingUser, String productId);
}
```

### SuspendProductUseCase

Implemented by `SuspendProductService`.

```java
public interface SuspendProductUseCase {

    Product suspendProduct(User requestingUser, String productId);
}
```

### DiscontinueProductUseCase

Implemented by `DiscontinueProductService`.

```java
public interface DiscontinueProductUseCase {

    Product discontinueProduct(User requestingUser, String productId);
}
```

### ConsultCatalogUseCase

Implemented by `ConsultCatalogService`.

```java
public interface ConsultCatalogUseCase {

    List<Product> consultCatalog();
    Product consultCatalogProduct(String productId);
}
```

### ConsultSellerProductsUseCase

Implemented by `ConsultSellerProductsService`.

```java
public interface ConsultSellerProductsUseCase {

    List<Product> consultSellerProducts(User requestingUser, String sellerId);
}
```

## Inventory Management

### RegisterInitialStockUseCase

Implemented by `RegisterInitialStockService`.

```java
public interface RegisterInitialStockUseCase {

    Inventory registerInitialStock(User requestingUser, String productId, String warehouseId, int quantity);
}
```

### RegisterInventoryInboundUseCase

Implemented by `RegisterInventoryInboundService`.

```java
public interface RegisterInventoryInboundUseCase {

    Inventory registerInventoryInbound(User requestingUser, String productId, String warehouseId, int quantity);
}
```

### RegisterInventoryAdjustmentUseCase

Implemented by `RegisterInventoryAdjustmentService`.

```java
public interface RegisterInventoryAdjustmentUseCase {

    Inventory registerInventoryAdjustment(User requestingUser, String productId, String warehouseId, int quantity, InventoryCondition condition);
}
```

### ReserveInventoryUseCase

Implemented by `ReserveInventoryService`.

```java
public interface ReserveInventoryUseCase {

    Inventory reserveInventory(User requestingUser, String productId, String warehouseId, int quantity);
}
```

### ReleaseInventoryReservationUseCase

Implemented by `ReleaseInventoryReservationService`.

```java
public interface ReleaseInventoryReservationUseCase {

    Inventory releaseInventoryReservation(User requestingUser, String productId, String warehouseId, int quantity);
}
```

### ConsultInventoryUseCase

Implemented by `ConsultInventoryService`.

```java
public interface ConsultInventoryUseCase {

    List<Inventory> consultProductInventory(User requestingUser, String productId);
    List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId);
}
```

## Cart Management

### AddItemToCartUseCase

Implemented by `AddItemToCartService`.

```java
public interface AddItemToCartUseCase {

    Cart addItemToCart(User requestingUser, String productId, Variant variant, int quantity);
}
```

### RemoveItemFromCartUseCase

Implemented by `RemoveItemFromCartService`.

```java
public interface RemoveItemFromCartUseCase {

    Cart removeItemFromCart(User requestingUser, String productId, Variant variant);
}
```

### UpdateCartItemQuantityUseCase

Implemented by `UpdateCartItemQuantityService`.

```java
public interface UpdateCartItemQuantityUseCase {

    Cart updateCartItemQuantity(User requestingUser, String productId, Variant variant, int quantity);
}
```

### ConsultCartUseCase

Implemented by `ConsultCartService`.

```java
public interface ConsultCartUseCase {

    Cart consultCart(User requestingUser, String buyerId);
}
```

### ClearCartUseCase

Implemented by `ClearCartService`.

```java
public interface ClearCartUseCase {

    Cart clearCart(User requestingUser);
}
```

## Order Management

### ConfirmOrderUseCase

Implemented by `ConfirmOrderService`.

```java
public interface ConfirmOrderUseCase {

    Order confirmOrder(User requestingUser);
}
```

### ValidatePaymentUseCase

Implemented by `ValidatePaymentService`.

```java
public interface ValidatePaymentUseCase {

    Order validatePayment(User requestingUser, String orderId);
}
```

### ConsultOrderUseCase

Implemented by `ConsultOrderService`.

```java
public interface ConsultOrderUseCase {

    Order consultOrder(User requestingUser, String orderId);
}
```

### ConsultBuyerOrdersUseCase

Implemented by `ConsultBuyerOrdersService`.

```java
public interface ConsultBuyerOrdersUseCase {

    List<Order> consultBuyerOrders(User requestingUser, String buyerId);
}
```

## Invoice Management

### GenerateInvoiceUseCase

Implemented by `GenerateInvoiceService`.

```java
public interface GenerateInvoiceUseCase {

    Invoice generateInvoice(Order order, User performedBy);
}
```

### ConsultInvoiceUseCase

Implemented by `ConsultInvoiceService`.

```java
public interface ConsultInvoiceUseCase {

    Invoice consultInvoice(User requestingUser, String orderId);
}
```

## Shipment Management

### CreateShipmentsUseCase

Implemented by `CreateShipmentsService`.

```java
public interface CreateShipmentsUseCase {

    List<Shipment> createShipments(User requestingUser, String orderId);
}
```

### PackShipmentUseCase

Implemented by `PackShipmentService`.

```java
public interface PackShipmentUseCase {

    Shipment packShipment(User requestingUser, String shipmentId);
}
```

### DispatchShipmentUseCase

Implemented by `DispatchShipmentService`.

```java
public interface DispatchShipmentUseCase {

    Shipment dispatchShipment(User requestingUser, String shipmentId);
}
```

### ConfirmDeliveryUseCase

Implemented by `ConfirmDeliveryService`.

```java
public interface ConfirmDeliveryUseCase {

    Shipment confirmDelivery(User requestingUser, String shipmentId);
}
```

## Returns and Refunds Management

### RequestReturnUseCase

Implemented by `RequestReturnService`.

```java
public interface RequestReturnUseCase {

    ReturnRequest requestReturn(User requestingUser, String orderId, String reason);
}
```

### ApproveReturnUseCase

Implemented by `ApproveReturnService`.

```java
public interface ApproveReturnUseCase {

    ReturnRequest approveReturn(User requestingUser, String returnRequestId);
}
```

### RejectReturnUseCase

Implemented by `RejectReturnService`.

```java
public interface RejectReturnUseCase {

    ReturnRequest rejectReturn(User requestingUser, String returnRequestId);
}
```

### ProcessRefundUseCase

Implemented by `ProcessRefundService`.

```java
public interface ProcessRefundUseCase {

    Refund processRefund(User requestingUser, String refundId);
}
```

### ConsultReturnUseCase

Implemented by `ConsultReturnService`.

```java
public interface ConsultReturnUseCase {

    ReturnRequest consultReturn(User requestingUser, String returnRequestId);
    Refund consultRefund(User requestingUser, String returnRequestId);
}
```

## Audit

### ConsultAuditLogsUseCase

Implemented by `ConsultAuditLogsService`.

```java
public interface ConsultAuditLogsUseCase {

    List<AuditLog> consultAuditLogs(User requestingUser);
    List<AuditLog> consultAuditLogsByUser(User requestingUser, String userId);
    List<AuditLog> consultAuditLogsByEntity(User requestingUser, AuditEntityType entityType, String entityId);
}
```

---

# 4. Role Ports

## PublicAccessPort (public)

Public operations that do not require an authenticated user: buyer self-registration, login and the public catalog.

```java
public interface PublicAccessPort {

    Buyer registerBuyerUser(String identificationNumber, String fullName, String email, String rawPassword, Address primaryAddress);
    String login(String email, String rawPassword);
    List<Product> consultCatalog();
    Product consultCatalogProduct(String productId);
}
```

| Method | Delegates to |
| --- | --- |
| `registerBuyerUser` | `RegisterBuyerUserService` |
| `login` | `LoginService` |
| `consultCatalog` | `ConsultCatalogService` |
| `consultCatalogProduct` | `ConsultCatalogService` |

## BuyerPort (`BUYER`)

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

## SellerPort (`SELLER`)

Operations of a seller over its own profile, warehouses, products, inventory and the orders that contain its products.

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

## LogisticsOperatorPort (`LOGISTICS_OPERATOR`)

Physical operation of warehouses: inventory movements, reservations and the shipment lifecycle.

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

## AdministratorPort (`ADMINISTRATOR`)

Administration of users, sellers, buyers' commercial status, Marketplace warehouses, returns, refunds and audit logs.

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

## SupervisorPort (`SUPERVISOR`)

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
