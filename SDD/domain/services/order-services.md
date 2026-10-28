# Order Management Services

## Introduction

This document specifies the services of the **Order Management** subdomain of NexusMarket.

Order confirmation from the cart, payment validation and order consultation.

All services live in the package `nexusmarket.domain.services.order`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Exposed in (role ports) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `ConfirmOrderService` | `BuyerPort` | `BUYER`. |
| 2 | `ValidatePaymentService` | `BuyerPort` | `BUYER` over its own order. |
| 3 | `ConsultOrderService` | `BuyerPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` | `BUYER` (own orders); `SELLER` (orders containing its products); `LOGISTICS_OPERATOR` and `SUPERVISOR`. |
| 4 | `ConsultBuyerOrdersService` | `BuyerPort`, `SupervisorPort` | `BUYER` over its own orders; `SUPERVISOR`. |

---

# 1. ConfirmOrderService

## Description

Converts the requesting buyer's cart into an order in `PENDING_PAYMENT`, reserves the inventory of its physical items and empties the cart.

## Implementation

* Class: `nexusmarket.domain.services.order.ConfirmOrderService` (`@Service`, `@RequiredArgsConstructor`).

```java
Order confirmOrder(User requestingUser);
```

## Authorized Roles

`BUYER`.

Exposed in: `BuyerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |

**Output:** `Order`. Returns the confirmed order.

## Business Rules

* The buyer must be able to purchase: user `ACTIVE` and `CommercialStatus.ENABLED`.
* Every product must still be `PUBLISHED`.
* Each order item keeps the unit price captured in the cart.
* For each physical item, stock is reserved in the first warehouse whose record is `AVAILABLE` and has enough stock for the whole item; that warehouse is stored as `OrderItem.originWarehouse`. Digital items have no origin warehouse.
* Availability of all items is checked before reserving any of them.
* After the order is created the cart is emptied.

## Validations

* RG-01 and role `BUYER`.
* The cart cannot be empty.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active buyer. |
| `EntityNotFoundException` | The buyer, its cart or a product does not exist. |
| `InvalidUserException` | The buyer's commercial status does not allow purchases. |
| `InvalidCartException` | The cart is empty. |
| `InvalidProductStatusException` | A product is no longer `PUBLISHED`. |
| `InsufficientInventoryException` | Not enough available stock for a physical item. |

## Output Ports Used

* `BuyerRepositoryPort`
* `CartRepositoryPort`
* `ProductRepositoryPort`
* `InventoryRepositoryPort`
* `OrderRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ReserveInventoryService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.ORDER_CONFIRMED`
* `AuditAction.INVENTORY_RESERVED`

---

# 2. ValidatePaymentService

## Description

Processes the payment of the buyer's own `PENDING_PAYMENT` order through the payment gateway.

## Implementation

* Class: `nexusmarket.domain.services.order.ValidatePaymentService` (`@Service`, `@RequiredArgsConstructor`).

```java
Order validatePayment(User requestingUser, String orderId);
```

## Authorized Roles

`BUYER` over its own order.

Exposed in: `BuyerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `orderId` | `String` | Identifier of the order. |

**Output:** `Order`. Returns the paid order.

## Business Rules

* The payment is processed through `PaymentGatewayPort.processPayment(order)`.
* `APPROVED`: the order goes to `PAID` and its invoice is generated (`GenerateInvoiceService`). If the order has no physical items it goes directly `PAID` → `DELIVERED` (digital products are delivered immediately after payment).
* `REJECTED`: the rejection is audited, `PaymentRejectedException` is raised and the order remains in `PENDING_PAYMENT`.

## Validations

* RG-01 and role `BUYER`.
* Ownership of the order (RG-03).
* The order must be `PENDING_PAYMENT`.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or order of another buyer. |
| `EntityNotFoundException` | The order does not exist. |
| `InvalidOrderStatusTransitionException` | The order is not `PENDING_PAYMENT`. |
| `OrderAlreadyFinalizedException` | The order is `DELIVERED`. |
| `PaymentRejectedException` | The payment gateway rejected the payment. |

## Output Ports Used

* `OrderRepositoryPort`
* `PaymentGatewayPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateBuyerOwnershipService`
* `GenerateInvoiceService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.PAYMENT_APPROVED`
* `AuditAction.PAYMENT_REJECTED`
* `AuditAction.ORDER_DELIVERED`

---

# 3. ConsultOrderService

## Description

Retrieves an order.

## Implementation

* Class: `nexusmarket.domain.services.order.ConsultOrderService` (`@Service`, `@RequiredArgsConstructor`).

```java
Order consultOrder(User requestingUser, String orderId);
```

## Authorized Roles

`BUYER` (own orders); `SELLER` (orders containing its products); `LOGISTICS_OPERATOR` and `SUPERVISOR`.

Exposed in: `BuyerPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `orderId` | `String` | Identifier of the order. |

**Output:** `Order`. Returns the order.

## Business Rules

* Read-only operation.

## Validations

* RG-01 and allowed role.
* Buyer: only its own orders; seller: only orders with at least one of its products (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or order outside the role. |
| `EntityNotFoundException` | The order does not exist. |

## Output Ports Used

* `OrderRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateBuyerOwnershipService`

## Audit

Not audited (read-only or authentication check).

---

# 4. ConsultBuyerOrdersService

## Description

Retrieves the historical and active orders of a buyer.

## Implementation

* Class: `nexusmarket.domain.services.order.ConsultBuyerOrdersService` (`@Service`, `@RequiredArgsConstructor`).

```java
List<Order> consultBuyerOrders(User requestingUser, String buyerId);
```

## Authorized Roles

`BUYER` over its own orders; `SUPERVISOR`.

Exposed in: `BuyerPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `buyerId` | `String` | Identifier of the buyer. |

**Output:** `List<Order>`. Returns the buyer's orders.

## Business Rules

* Read-only operation.

## Validations

* RG-01 and allowed role.
* A buyer only consults its own orders (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or orders of another buyer. |
| `EntityNotFoundException` | The buyer does not exist. |

## Output Ports Used

* `BuyerRepositoryPort`
* `OrderRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateBuyerOwnershipService`

## Audit

Not audited (read-only or authentication check).

---
