# Use Cases Adapters — NexusMarket

## 1. Overview

Use case adapters live in `nexusmarket.adapters.useCases`. Each one is the concrete implementation of a role port of `nexusmarket.domain.ports.in` and bridges the incoming calls (REST controllers in a later phase) to the domain services of `nexusmarket.domain.services`.

## 2. Responsibilities

* Implement exactly one role port.
* Inject the concrete domain service classes (`@Service`, constructor injection with `@RequiredArgsConstructor`).
* Delegate every call directly to the corresponding service; they contain no business rules.

```text
REST Controller (adapters/rest — later phase)
      |
      v  calls
Role Port (domain/ports/in)
      ^
      |  implemented by
Use Case Adapter (adapters/useCases)
      |
      v  delegates to
Domain Service (domain/services) — implements <Name>UseCase
      |
      v  calls
Output Port (domain/ports/out)
```

## 3. Adapters

| Role port | Use case adapter | Injected domain services |
| --- | --- | --- |
| `PublicAccessPort` | `PublicAccessUseCaseImpl` | `RegisterBuyerUserService`, `LoginService`, `ConsultCatalogService` |
| `BuyerPort` | `BuyerUseCaseImpl` | `ConsultUserService`, `ChangeUserPasswordService`, `ConsultBuyerService`, `UpdateBuyerProfileService`, `AddItemToCartService`, `RemoveItemFromCartService`, `UpdateCartItemQuantityService`, `ConsultCartService`, `ClearCartService`, `ConfirmOrderService`, `ValidatePaymentService`, `ConsultOrderService`, `ConsultBuyerOrdersService`, `ConsultInvoiceService`, `RequestReturnService`, `ConsultReturnService` |
| `SellerPort` | `SellerUseCaseImpl` | `ConsultUserService`, `ChangeUserPasswordService`, `ConsultSellerService`, `UpdateSellerService`, `RegisterWarehouseService`, `ConsultWarehouseService`, `UpdateWarehouseService`, `RegisterProductService`, `UpdateProductService`, `PublishProductService`, `SuspendProductService`, `DiscontinueProductService`, `ConsultSellerProductsService`, `RegisterInitialStockService`, `RegisterInventoryInboundService`, `ConsultInventoryService`, `ConsultOrderService` |
| `LogisticsOperatorPort` | `LogisticsOperatorUseCaseImpl` | `ConsultUserService`, `ChangeUserPasswordService`, `ConsultWarehouseService`, `RegisterInitialStockService`, `RegisterInventoryInboundService`, `RegisterInventoryAdjustmentService`, `ReserveInventoryService`, `ReleaseInventoryReservationService`, `ConsultInventoryService`, `ConsultOrderService`, `CreateShipmentsService`, `PackShipmentService`, `DispatchShipmentService`, `ConfirmDeliveryService` |
| `AdministratorPort` | `AdministratorUseCaseImpl` | `RegisterStaffUserService`, `ConsultUserService`, `ChangeUserStatusService`, `ChangeUserPasswordService`, `RegisterSellerService`, `ConsultSellerService`, `UpdateSellerService`, `ConsultBuyerService`, `ChangeBuyerCommercialStatusService`, `RegisterWarehouseService`, `ConsultWarehouseService`, `UpdateWarehouseService`, `ConsultSellerProductsService`, `ConsultInvoiceService`, `ApproveReturnService`, `RejectReturnService`, `ProcessRefundService`, `ConsultReturnService`, `ConsultAuditLogsService` |
| `SupervisorPort` | `SupervisorUseCaseImpl` | `ConsultUserService`, `ChangeUserPasswordService`, `ConsultSellerService`, `ConsultBuyerService`, `ConsultWarehouseService`, `ConsultSellerProductsService`, `ConsultInventoryService`, `ConsultCartService`, `ConsultOrderService`, `ConsultBuyerOrdersService`, `ConsultInvoiceService`, `ConsultReturnService`, `ConsultAuditLogsService` |

---

## PublicAccessUseCaseImpl

Implements `PublicAccessPort`. Public operations that do not require an authenticated user: buyer self-registration, login and the public catalog.

| Method | Delegates to |
| --- | --- |
| `registerBuyerUser` | `RegisterBuyerUserService.registerBuyerUser` |
| `login` | `LoginService.login` |
| `consultCatalog` | `ConsultCatalogService.consultCatalog` |
| `consultCatalogProduct` | `ConsultCatalogService.consultCatalogProduct` |

## BuyerUseCaseImpl

Implements `BuyerPort`. Operations of a buyer over its own profile, cart, orders, invoices and return requests.

| Method | Delegates to |
| --- | --- |
| `consultUser` | `ConsultUserService.consultUser` |
| `changeUserPassword` | `ChangeUserPasswordService.changeUserPassword` |
| `consultBuyer` | `ConsultBuyerService.consultBuyer` |
| `updateBuyerProfile` | `UpdateBuyerProfileService.updateBuyerProfile` |
| `addItemToCart` | `AddItemToCartService.addItemToCart` |
| `removeItemFromCart` | `RemoveItemFromCartService.removeItemFromCart` |
| `updateCartItemQuantity` | `UpdateCartItemQuantityService.updateCartItemQuantity` |
| `consultCart` | `ConsultCartService.consultCart` |
| `clearCart` | `ClearCartService.clearCart` |
| `confirmOrder` | `ConfirmOrderService.confirmOrder` |
| `validatePayment` | `ValidatePaymentService.validatePayment` |
| `consultOrder` | `ConsultOrderService.consultOrder` |
| `consultBuyerOrders` | `ConsultBuyerOrdersService.consultBuyerOrders` |
| `consultInvoice` | `ConsultInvoiceService.consultInvoice` |
| `requestReturn` | `RequestReturnService.requestReturn` |
| `consultReturn` | `ConsultReturnService.consultReturn` |
| `consultRefund` | `ConsultReturnService.consultRefund` |

## SellerUseCaseImpl

Implements `SellerPort`. Operations of a seller over its own profile, warehouses, products, inventory and the orders that contain its products.

| Method | Delegates to |
| --- | --- |
| `consultUser` | `ConsultUserService.consultUser` |
| `changeUserPassword` | `ChangeUserPasswordService.changeUserPassword` |
| `consultSeller` | `ConsultSellerService.consultSeller` |
| `updateSeller` | `UpdateSellerService.updateSeller` |
| `registerWarehouse` | `RegisterWarehouseService.registerWarehouse` |
| `consultWarehouse` | `ConsultWarehouseService.consultWarehouse` |
| `consultWarehouses` | `ConsultWarehouseService.consultWarehouses` |
| `updateWarehouse` | `UpdateWarehouseService.updateWarehouse` |
| `registerProduct` | `RegisterProductService.registerProduct` |
| `updateProduct` | `UpdateProductService.updateProduct` |
| `publishProduct` | `PublishProductService.publishProduct` |
| `suspendProduct` | `SuspendProductService.suspendProduct` |
| `discontinueProduct` | `DiscontinueProductService.discontinueProduct` |
| `consultSellerProducts` | `ConsultSellerProductsService.consultSellerProducts` |
| `registerInitialStock` | `RegisterInitialStockService.registerInitialStock` |
| `registerInventoryInbound` | `RegisterInventoryInboundService.registerInventoryInbound` |
| `consultProductInventory` | `ConsultInventoryService.consultProductInventory` |
| `consultWarehouseInventory` | `ConsultInventoryService.consultWarehouseInventory` |
| `consultOrder` | `ConsultOrderService.consultOrder` |

## LogisticsOperatorUseCaseImpl

Implements `LogisticsOperatorPort`. Physical operation of warehouses: inventory movements, reservations and the shipment lifecycle.

| Method | Delegates to |
| --- | --- |
| `consultUser` | `ConsultUserService.consultUser` |
| `changeUserPassword` | `ChangeUserPasswordService.changeUserPassword` |
| `consultWarehouse` | `ConsultWarehouseService.consultWarehouse` |
| `consultWarehouses` | `ConsultWarehouseService.consultWarehouses` |
| `registerInitialStock` | `RegisterInitialStockService.registerInitialStock` |
| `registerInventoryInbound` | `RegisterInventoryInboundService.registerInventoryInbound` |
| `registerInventoryAdjustment` | `RegisterInventoryAdjustmentService.registerInventoryAdjustment` |
| `reserveInventory` | `ReserveInventoryService.reserveInventory` |
| `releaseInventoryReservation` | `ReleaseInventoryReservationService.releaseInventoryReservation` |
| `consultProductInventory` | `ConsultInventoryService.consultProductInventory` |
| `consultWarehouseInventory` | `ConsultInventoryService.consultWarehouseInventory` |
| `consultOrder` | `ConsultOrderService.consultOrder` |
| `createShipments` | `CreateShipmentsService.createShipments` |
| `packShipment` | `PackShipmentService.packShipment` |
| `dispatchShipment` | `DispatchShipmentService.dispatchShipment` |
| `confirmDelivery` | `ConfirmDeliveryService.confirmDelivery` |

## AdministratorUseCaseImpl

Implements `AdministratorPort`. Administration of users, sellers, buyers' commercial status, Marketplace warehouses, returns, refunds and audit logs.

| Method | Delegates to |
| --- | --- |
| `registerStaffUser` | `RegisterStaffUserService.registerStaffUser` |
| `consultUser` | `ConsultUserService.consultUser` |
| `changeUserStatus` | `ChangeUserStatusService.changeUserStatus` |
| `changeUserPassword` | `ChangeUserPasswordService.changeUserPassword` |
| `registerSeller` | `RegisterSellerService.registerSeller` |
| `consultSeller` | `ConsultSellerService.consultSeller` |
| `updateSeller` | `UpdateSellerService.updateSeller` |
| `consultBuyer` | `ConsultBuyerService.consultBuyer` |
| `changeBuyerCommercialStatus` | `ChangeBuyerCommercialStatusService.changeBuyerCommercialStatus` |
| `registerWarehouse` | `RegisterWarehouseService.registerWarehouse` |
| `consultWarehouse` | `ConsultWarehouseService.consultWarehouse` |
| `consultWarehouses` | `ConsultWarehouseService.consultWarehouses` |
| `updateWarehouse` | `UpdateWarehouseService.updateWarehouse` |
| `consultSellerProducts` | `ConsultSellerProductsService.consultSellerProducts` |
| `consultInvoice` | `ConsultInvoiceService.consultInvoice` |
| `approveReturn` | `ApproveReturnService.approveReturn` |
| `rejectReturn` | `RejectReturnService.rejectReturn` |
| `processRefund` | `ProcessRefundService.processRefund` |
| `consultReturn` | `ConsultReturnService.consultReturn` |
| `consultRefund` | `ConsultReturnService.consultRefund` |
| `consultAuditLogs` | `ConsultAuditLogsService.consultAuditLogs` |
| `consultAuditLogsByUser` | `ConsultAuditLogsService.consultAuditLogsByUser` |
| `consultAuditLogsByEntity` | `ConsultAuditLogsService.consultAuditLogsByEntity` |

## SupervisorUseCaseImpl

Implements `SupervisorPort`. Read-only oversight: consultation operations across all business domains. It never modifies information.

| Method | Delegates to |
| --- | --- |
| `consultUser` | `ConsultUserService.consultUser` |
| `changeUserPassword` | `ChangeUserPasswordService.changeUserPassword` |
| `consultSeller` | `ConsultSellerService.consultSeller` |
| `consultBuyer` | `ConsultBuyerService.consultBuyer` |
| `consultWarehouse` | `ConsultWarehouseService.consultWarehouse` |
| `consultWarehouses` | `ConsultWarehouseService.consultWarehouses` |
| `consultSellerProducts` | `ConsultSellerProductsService.consultSellerProducts` |
| `consultProductInventory` | `ConsultInventoryService.consultProductInventory` |
| `consultWarehouseInventory` | `ConsultInventoryService.consultWarehouseInventory` |
| `consultCart` | `ConsultCartService.consultCart` |
| `consultOrder` | `ConsultOrderService.consultOrder` |
| `consultBuyerOrders` | `ConsultBuyerOrdersService.consultBuyerOrders` |
| `consultInvoice` | `ConsultInvoiceService.consultInvoice` |
| `consultReturn` | `ConsultReturnService.consultReturn` |
| `consultRefund` | `ConsultReturnService.consultRefund` |
| `consultAuditLogs` | `ConsultAuditLogsService.consultAuditLogs` |
| `consultAuditLogsByUser` | `ConsultAuditLogsService.consultAuditLogsByUser` |
| `consultAuditLogsByEntity` | `ConsultAuditLogsService.consultAuditLogsByEntity` |

---

## 4. Code Pattern

```java
package nexusmarket.adapters.useCases;

@Service
@RequiredArgsConstructor
public class AdministratorUseCaseImpl implements AdministratorPort {

    private final ChangeUserStatusService changeUserStatusService;
    // ...

    @Override
    public User changeUserStatus(User requestingUser, String userId, UserStatus newStatus) {
        return changeUserStatusService.changeUserStatus(requestingUser, userId, newStatus);
    }
}
```
