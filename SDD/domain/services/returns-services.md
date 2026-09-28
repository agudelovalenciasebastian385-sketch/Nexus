# Returns and Refunds Management Services

## Introduction

This document specifies the services of the **Returns and Refunds Management** subdomain of NexusMarket.

Return requests over delivered orders, their approval or rejection, inventory re-entry and refund processing.

All services live in the package `nexusmarket.domain.services.returns`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Use case (input port) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `RequestReturnService` | `RequestReturnUseCase` | `BUYER` over its own orders. |
| 2 | `ApproveReturnService` | `ApproveReturnUseCase` | `ADMINISTRATOR`. |
| 3 | `RejectReturnService` | `RejectReturnUseCase` | `ADMINISTRATOR`. |
| 4 | `ProcessRefundService` | `ProcessRefundUseCase` | `ADMINISTRATOR`. |
| 5 | `ConsultReturnService` | `ConsultReturnUseCase` | `BUYER` (own orders); `ADMINISTRATOR` and `SUPERVISOR`. |

---

# 1. RequestReturnService

## Description

Registers a return request over a delivered order of the requesting buyer.

## Implementation

* Class: `nexusmarket.domain.services.returns.RequestReturnService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.RequestReturnUseCase`.

```java
ReturnRequest requestReturn(User requestingUser, String orderId, String reason);
```

## Authorized Roles

`BUYER` over its own orders.

Exposed in: `BuyerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `orderId` | `String` | Identifier of the order. |
| `reason` | `String` | Justification. |

**Output:** `ReturnRequest`. Returns the return request in `REQUESTED`.

## Business Rules

* A return covers the whole order.
* Because a return covers the whole order, an order can have at most one return request that is not `REJECTED`.

## Validations

* RG-01 and role `BUYER`.
* Ownership of the order (RG-03).
* The order must be `DELIVERED`.
* The order must not have a `REQUESTED` or `APPROVED` return.
* `reason` cannot be empty.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or order of another buyer. |
| `EntityNotFoundException` | The order does not exist. |
| `InvalidReturnRequestException` | Order not delivered, existing active return or empty reason. |

## Output Ports Used

* `OrderRepositoryPort`
* `ReturnRequestRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateBuyerOwnershipService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.RETURN_REQUESTED`

---

# 2. ApproveReturnService

## Description

Approves a return request, re-enters the inventory of its physical items and creates the pending refund.

## Implementation

* Class: `nexusmarket.domain.services.returns.ApproveReturnService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.ApproveReturnUseCase`.

```java
ReturnRequest approveReturn(User requestingUser, String returnRequestId);
```

## Authorized Roles

`ADMINISTRATOR`.

Exposed in: `AdministratorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `returnRequestId` | `String` | Identifier of the return request. |

**Output:** `ReturnRequest`. Returns the approved return request.

## Business Rules

* For each physical item of the order, a `RETURN` movement adds its quantity to the inventory record of its origin warehouse.
* A `Refund` in `PENDING` is created for the total amount of the order's invoice.

## Validations

* RG-01 and role `ADMINISTRATOR`.
* The return request must be `REQUESTED`.
* The order's invoice and inventory records must exist.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active administrator. |
| `EntityNotFoundException` | The return request, the invoice or an inventory record does not exist. |
| `InvalidStatusTransitionException` | The return request is not `REQUESTED`. |
| `InvalidRefundException` | The invoice total is not positive. |

## Output Ports Used

* `ReturnRequestRepositoryPort`
* `InvoiceRepositoryPort`
* `InventoryRepositoryPort`
* `RefundRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.RETURN_APPROVED`
* `AuditAction.REFUND_CREATED`

---

# 3. RejectReturnService

## Description

Rejects a return request that does not meet the required conditions.

## Implementation

* Class: `nexusmarket.domain.services.returns.RejectReturnService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.RejectReturnUseCase`.

```java
ReturnRequest rejectReturn(User requestingUser, String returnRequestId);
```

## Authorized Roles

`ADMINISTRATOR`.

Exposed in: `AdministratorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `returnRequestId` | `String` | Identifier of the return request. |

**Output:** `ReturnRequest`. Returns the rejected return request.

## Business Rules

* The transition is applied by `ReturnRequest.reject()`.

## Validations

* RG-01 and role `ADMINISTRATOR`.
* The return request must be `REQUESTED`.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active administrator. |
| `EntityNotFoundException` | The return request does not exist. |
| `InvalidStatusTransitionException` | The return request is not `REQUESTED`. |

## Output Ports Used

* `ReturnRequestRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.RETURN_REJECTED`

---

# 4. ProcessRefundService

## Description

Processes a pending refund: the amount is restituted to the buyer (`PENDING` → `PROCESSED`).

## Implementation

* Class: `nexusmarket.domain.services.returns.ProcessRefundService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.ProcessRefundUseCase`.

```java
Refund processRefund(User requestingUser, String refundId);
```

## Authorized Roles

`ADMINISTRATOR`.

Exposed in: `AdministratorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `refundId` | `String` | Identifier of the refund. |

**Output:** `Refund`. Returns the processed refund.

## Business Rules

* The transition is applied by `Refund.markProcessed()`.

## Validations

* RG-01 and role `ADMINISTRATOR`.
* The refund must be `PENDING`.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active administrator. |
| `EntityNotFoundException` | The refund does not exist. |
| `InvalidStatusTransitionException` | The refund is not `PENDING`. |

## Output Ports Used

* `RefundRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.REFUND_PROCESSED`

---

# 5. ConsultReturnService

## Description

Retrieves a return request or the refund generated by it.

## Implementation

* Class: `nexusmarket.domain.services.returns.ConsultReturnService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.ConsultReturnUseCase`.

```java
ReturnRequest consultReturn(User requestingUser, String returnRequestId);
Refund consultRefund(User requestingUser, String returnRequestId);
```

## Authorized Roles

`BUYER` (own orders); `ADMINISTRATOR` and `SUPERVISOR`.

Exposed in: `AdministratorPort`, `BuyerPort`, `SupervisorPort`.

## Input — `consultReturn`

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `returnRequestId` | `String` | Identifier of the return request. |

**Output:** `ReturnRequest`. Returns the return request.

## Input — `consultRefund`

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `returnRequestId` | `String` | Identifier of the return request. |

**Output:** `Refund`. Returns the refund of the return request.

## Business Rules

* Read-only operation.

## Validations

* RG-01 and allowed role.
* A buyer only consults returns of its own orders (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or return of another buyer. |
| `EntityNotFoundException` | The return request or its refund does not exist. |

## Output Ports Used

* `ReturnRequestRepositoryPort`
* `RefundRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateBuyerOwnershipService`

## Audit

Not audited (read-only or authentication check).

---
