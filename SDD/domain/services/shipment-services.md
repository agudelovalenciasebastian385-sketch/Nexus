# Shipment Management Services

## Introduction

This document specifies the services of the **Shipment Management** subdomain of NexusMarket.

Logistics of physical products: shipment creation per origin warehouse, packing, dispatch and delivery confirmation.

All services live in the package `nexusmarket.domain.services.shipment`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Exposed in (role ports) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `CreateShipmentsService` | `LogisticsOperatorPort` | `LOGISTICS_OPERATOR`. |
| 2 | `PackShipmentService` | `LogisticsOperatorPort` | `LOGISTICS_OPERATOR` assigned to the shipment. |
| 3 | `DispatchShipmentService` | `LogisticsOperatorPort` | `LOGISTICS_OPERATOR` assigned to the shipment. |
| 4 | `ConfirmDeliveryService` | `LogisticsOperatorPort` | `LOGISTICS_OPERATOR` assigned to the shipment. |

---

# 1. CreateShipmentsService

## Description

Creates the shipments of a `PAID` order: one per origin warehouse, only for physical items.

## Implementation

* Class: `nexusmarket.domain.services.shipment.CreateShipmentsService` (`@Service`, `@RequiredArgsConstructor`).

```java
List<Shipment> createShipments(User requestingUser, String orderId);
```

## Authorized Roles

`LOGISTICS_OPERATOR`.

Exposed in: `LogisticsOperatorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `orderId` | `String` | Identifier of the order. |

**Output:** `List<Shipment>`. Returns the created shipments.

## Business Rules

* Physical items are grouped by `OrderItem.originWarehouse`; one `Shipment` is created per group.
* The delivery address is the buyer's primary address.
* The requesting operator is assigned to every created shipment.
* Every shipment stores its creation date-time in `Shipment.createdAt`.

## Validations

* RG-01 and role `LOGISTICS_OPERATOR`.
* The order must be `PAID`.
* The order must contain physical items.
* The order must not have shipments yet.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active logistics operator. |
| `EntityNotFoundException` | The order or the operator does not exist. |
| `InvalidShipmentException` | Order not `PAID`, without physical items or already with shipments. |

## Output Ports Used

* `OrderRepositoryPort`
* `ShipmentRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.SHIPMENT_CREATED`

---

# 2. PackShipmentService

## Description

Registers that a shipment has been packed (`PREPARING` → `PACKED`).

## Implementation

* Class: `nexusmarket.domain.services.shipment.PackShipmentService` (`@Service`, `@RequiredArgsConstructor`).

```java
Shipment packShipment(User requestingUser, String shipmentId);
```

## Authorized Roles

`LOGISTICS_OPERATOR` assigned to the shipment.

Exposed in: `LogisticsOperatorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `shipmentId` | `String` | Identifier of the shipment. |

**Output:** `Shipment`. Returns the packed shipment.

## Business Rules

* Only the operator assigned to the shipment can operate it.

## Validations

* RG-01 and role `LOGISTICS_OPERATOR`.
* The shipment must be assigned to the requesting operator.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or shipment of another operator. |
| `EntityNotFoundException` | The shipment does not exist. |
| `InvalidStatusTransitionException` | The shipment is not `PREPARING`. |

## Output Ports Used

* `ShipmentRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.SHIPMENT_PACKED`

---

# 3. DispatchShipmentService

## Description

Dispatches a packed shipment (`PACKED` → `IN_TRANSIT`), converts its reservations into sale outbounds and moves the order to `SHIPPED`.

## Implementation

* Class: `nexusmarket.domain.services.shipment.DispatchShipmentService` (`@Service`, `@RequiredArgsConstructor`).

```java
Shipment dispatchShipment(User requestingUser, String shipmentId);
```

## Authorized Roles

`LOGISTICS_OPERATOR` assigned to the shipment.

Exposed in: `LogisticsOperatorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `shipmentId` | `String` | Identifier of the shipment. |

**Output:** `Shipment`. Returns the dispatched shipment.

## Business Rules

* For each physical item whose origin warehouse is the shipment's warehouse, a `SALE_OUTBOUND` movement consumes the reserved stock.
* The order goes `PAID` → `SHIPPED` when its first shipment is dispatched.

## Validations

* RG-01 and role `LOGISTICS_OPERATOR`.
* The shipment must be assigned to the requesting operator.
* The inventory records of the shipment's items must exist and have enough reserved stock.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or shipment of another operator. |
| `EntityNotFoundException` | The shipment, its order or an inventory record does not exist. |
| `InvalidStatusTransitionException` | The shipment is not `PACKED`. |
| `InvalidInventoryException` | Not enough reserved stock. |
| `OrderAlreadyFinalizedException` | The order is `DELIVERED`. |

## Output Ports Used

* `ShipmentRepositoryPort`
* `OrderRepositoryPort`
* `InventoryRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.SHIPMENT_DISPATCHED`

---

# 4. ConfirmDeliveryService

## Description

Confirms the delivery of a shipment (`IN_TRANSIT` → `DELIVERED`) and finalizes the order when all its shipments are delivered.

## Implementation

* Class: `nexusmarket.domain.services.shipment.ConfirmDeliveryService` (`@Service`, `@RequiredArgsConstructor`).

```java
Shipment confirmDelivery(User requestingUser, String shipmentId);
```

## Authorized Roles

`LOGISTICS_OPERATOR` assigned to the shipment.

Exposed in: `LogisticsOperatorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `shipmentId` | `String` | Identifier of the shipment. |

**Output:** `Shipment`. Returns the delivered shipment.

## Business Rules

* When every shipment of the order is `DELIVERED`, the order goes `SHIPPED` → `DELIVERED` (finalized).

## Validations

* RG-01 and role `LOGISTICS_OPERATOR`.
* The shipment must be assigned to the requesting operator.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or shipment of another operator. |
| `EntityNotFoundException` | The shipment or its order does not exist. |
| `InvalidStatusTransitionException` | The shipment is not `IN_TRANSIT`. |
| `InvalidOrderStatusTransitionException` | The order is not `SHIPPED`. |

## Output Ports Used

* `ShipmentRepositoryPort`
* `OrderRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.SHIPMENT_DELIVERED`
* `AuditAction.ORDER_DELIVERED`

---
