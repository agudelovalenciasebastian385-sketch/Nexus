# Inventory Management Services

## Introduction

This document specifies the services of the **Inventory Management** subdomain of NexusMarket.

Distributed stock per product and warehouse: initial stock, inbounds, adjustments, reservations and consultation.

All services live in the package `nexusmarket.domain.services.inventory`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Exposed in (role ports) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `RegisterInitialStockService` | `LogisticsOperatorPort`, `SellerPort` | `SELLER` (own products in own warehouses); `LOGISTICS_OPERATOR` (any product and warehouse). |
| 2 | `RegisterInventoryInboundService` | `LogisticsOperatorPort`, `SellerPort` | `SELLER` (own products in own warehouses); `LOGISTICS_OPERATOR`. |
| 3 | `RegisterInventoryAdjustmentService` | `LogisticsOperatorPort` | `LOGISTICS_OPERATOR`. |
| 4 | `ReserveInventoryService` | `LogisticsOperatorPort` | `LOGISTICS_OPERATOR` (use case); internally used by `ConfirmOrderService`. |
| 5 | `ReleaseInventoryReservationService` | `LogisticsOperatorPort` | `LOGISTICS_OPERATOR`. |
| 6 | `ConsultInventoryService` | `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` | `SELLER` (own products / own warehouses); `LOGISTICS_OPERATOR` and `SUPERVISOR`. |

---

# 1. RegisterInitialStockService

## Description

Creates the inventory record of a physical product in a warehouse with its initial stock.

## Implementation

* Class: `nexusmarket.domain.services.inventory.RegisterInitialStockService` (`@Service`, `@RequiredArgsConstructor`).

```java
Inventory registerInitialStock(User requestingUser, String productId, String warehouseId, int quantity);
```

## Authorized Roles

`SELLER` (own products in own warehouses); `LOGISTICS_OPERATOR` (any product and warehouse).

Exposed in: `LogisticsOperatorPort`, `SellerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `productId` | `String` | Identifier of the physical product. |
| `warehouseId` | `String` | Identifier of the warehouse. |
| `quantity` | `int` | Initial stock (> 0). |

**Output:** `Inventory`. Returns the new inventory record.

## Business Rules

* Inventory only exists for physical products.
* The initial stock is recorded as an `INBOUND` movement for traceability.
* A seller can only register stock of its own products in its own `SELLER` warehouses; `MARKETPLACE` warehouses are operated by the logistics operator.

## Validations

* RG-01 and allowed role.
* The product must be a `PhysicalProduct`.
* There must not be an inventory record for the same product and warehouse.
* `quantity` must be positive.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or product/warehouse outside the seller. |
| `EntityNotFoundException` | The product or the warehouse does not exist. |
| `InvalidInventoryException` | Digital product, existing record or invalid quantity. |

## Output Ports Used

* `ProductRepositoryPort`
* `WarehouseRepositoryPort`
* `InventoryRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.INVENTORY_INITIAL_STOCK_REGISTERED`

---

# 2. RegisterInventoryInboundService

## Description

Adds stock to an existing inventory record (`INBOUND` movement).

## Implementation

* Class: `nexusmarket.domain.services.inventory.RegisterInventoryInboundService` (`@Service`, `@RequiredArgsConstructor`).

```java
Inventory registerInventoryInbound(User requestingUser, String productId, String warehouseId, int quantity);
```

## Authorized Roles

`SELLER` (own products in own warehouses); `LOGISTICS_OPERATOR`.

Exposed in: `LogisticsOperatorPort`, `SellerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `productId` | `String` | Identifier of the physical product. |
| `warehouseId` | `String` | Identifier of the warehouse. |
| `quantity` | `int` | Units received (> 0). |

**Output:** `Inventory`. Returns the updated inventory record.

## Business Rules

* The movement is applied by `Inventory.registerMovement(INBOUND, ...)`.

## Validations

* RG-01 and allowed role.
* Seller ownership over product and warehouse (RG-03).
* The inventory record must exist.
* `quantity` must be positive.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or product/warehouse outside the seller. |
| `EntityNotFoundException` | The product, the warehouse or the inventory record does not exist. |
| `InvalidInventoryException` | Digital product or invalid quantity. |

## Output Ports Used

* `ProductRepositoryPort`
* `WarehouseRepositoryPort`
* `InventoryRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.INVENTORY_INBOUND_REGISTERED`

---

# 3. RegisterInventoryAdjustmentService

## Description

Applies a manual correction of stock and/or changes the condition of an inventory record (e.g. marks it `DAMAGED`).

## Implementation

* Class: `nexusmarket.domain.services.inventory.RegisterInventoryAdjustmentService` (`@Service`, `@RequiredArgsConstructor`).

```java
Inventory registerInventoryAdjustment(User requestingUser, String productId, String warehouseId, int quantity, InventoryCondition condition);
```

## Authorized Roles

`LOGISTICS_OPERATOR`.

Exposed in: `LogisticsOperatorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `productId` | `String` | Identifier of the physical product. |
| `warehouseId` | `String` | Identifier of the warehouse. |
| `quantity` | `int` | Signed correction of the available stock (0 = no correction). |
| `condition` | `InventoryCondition` | New condition of the record (`null` = unchanged). |

**Output:** `Inventory`. Returns the updated inventory record.

## Business Rules

* A non-zero `quantity` is recorded as an `ADJUSTMENT` movement.
* Negative stock is never allowed.

## Validations

* RG-01 and role `LOGISTICS_OPERATOR`.
* The inventory record must exist.
* At least a non-zero quantity or a condition must be provided.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active logistics operator. |
| `EntityNotFoundException` | The product, the warehouse or the inventory record does not exist. |
| `InsufficientInventoryException` | The correction would leave the stock below zero. |
| `InvalidInventoryException` | Nothing to adjust or digital product. |

## Output Ports Used

* `ProductRepositoryPort`
* `WarehouseRepositoryPort`
* `InventoryRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.INVENTORY_ADJUSTED`

---

# 4. ReserveInventoryService

## Description

Holds stock of a physical product for an order in progress (`RESERVATION` movement: available → reserved).

## Implementation

* Class: `nexusmarket.domain.services.inventory.ReserveInventoryService` (`@Service`, `@RequiredArgsConstructor`).

```java
Inventory reserveInventory(User requestingUser, String productId, String warehouseId, int quantity);
```

## Authorized Roles

`LOGISTICS_OPERATOR` (use case); internally used by `ConfirmOrderService`.

Exposed in: `LogisticsOperatorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `productId` | `String` | Identifier of the physical product. |
| `warehouseId` | `String` | Identifier of the warehouse. |
| `quantity` | `int` | Units to reserve (> 0). |

**Output:** `Inventory`. Returns the updated inventory record.

## Business Rules

* Only inventory in condition `AVAILABLE` can be reserved; a record that does not exist or is `RESERVED`, `OUT_OF_STOCK` or `DAMAGED` can never be reserved (`InvalidInventoryException`).
* `ConfirmOrderService` selects the warehouse of each order item and reserves it through the internal method `execute(Inventory inventory, int quantity, User performedBy)`, which applies the movement, stores the record and audits it without a role check (the buyer is authorized by `ConfirmOrderService`).

## Validations

* RG-01 and role `LOGISTICS_OPERATOR` (use case).
* The inventory record must exist.
* The available stock must be enough.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active logistics operator. |
| `EntityNotFoundException` | The product, the warehouse or the inventory record does not exist. |
| `InsufficientInventoryException` | Not enough available stock in any warehouse. |
| `InvalidInventoryException` | The record is not `AVAILABLE` (`RESERVED`, `OUT_OF_STOCK` or `DAMAGED`) or the quantity is invalid. |

## Output Ports Used

* `ProductRepositoryPort`
* `WarehouseRepositoryPort`
* `InventoryRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.INVENTORY_RESERVED`

---

# 5. ReleaseInventoryReservationService

## Description

Returns reserved stock to the available stock (`RESERVATION_RELEASE` movement).

## Implementation

* Class: `nexusmarket.domain.services.inventory.ReleaseInventoryReservationService` (`@Service`, `@RequiredArgsConstructor`).

```java
Inventory releaseInventoryReservation(User requestingUser, String productId, String warehouseId, int quantity);
```

## Authorized Roles

`LOGISTICS_OPERATOR`.

Exposed in: `LogisticsOperatorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `productId` | `String` | Identifier of the physical product. |
| `warehouseId` | `String` | Identifier of the warehouse. |
| `quantity` | `int` | Units to release (> 0). |

**Output:** `Inventory`. Returns the updated inventory record.

## Business Rules

* The movement is applied by `Inventory.registerMovement(RESERVATION_RELEASE, ...)`.

## Validations

* RG-01 and role `LOGISTICS_OPERATOR`.
* The inventory record must exist.
* The reserved stock must be enough.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active logistics operator. |
| `EntityNotFoundException` | The product, the warehouse or the inventory record does not exist. |
| `InvalidInventoryException` | Not enough reserved stock or invalid quantity. |

## Output Ports Used

* `ProductRepositoryPort`
* `WarehouseRepositoryPort`
* `InventoryRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.INVENTORY_RESERVATION_RELEASED`

---

# 6. ConsultInventoryService

## Description

Retrieves the inventory records of a product or of a warehouse.

## Implementation

* Class: `nexusmarket.domain.services.inventory.ConsultInventoryService` (`@Service`, `@RequiredArgsConstructor`).

```java
List<Inventory> consultProductInventory(User requestingUser, String productId);
List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId);
```

## Authorized Roles

`SELLER` (own products / own warehouses); `LOGISTICS_OPERATOR` and `SUPERVISOR`.

Exposed in: `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort`.

## Input — `consultProductInventory`

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `productId` | `String` | Identifier of the physical product. |

**Output:** `List<Inventory>`. Returns the product's records in every warehouse.

## Input — `consultWarehouseInventory`

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `warehouseId` | `String` | Identifier of the warehouse. |

**Output:** `List<Inventory>`. Returns the records held in the warehouse.

## Business Rules

* Read-only operation.

## Validations

* RG-01 and allowed role.
* Seller ownership over the product or the warehouse (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or information of another seller. |
| `EntityNotFoundException` | The product or the warehouse does not exist. |
| `InvalidInventoryException` | The product is digital. |

## Output Ports Used

* `ProductRepositoryPort`
* `WarehouseRepositoryPort`
* `InventoryRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`

## Audit

Not audited (read-only or authentication check).

---
