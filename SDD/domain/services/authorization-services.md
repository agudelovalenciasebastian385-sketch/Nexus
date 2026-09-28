# Authorization Services

## Introduction

This document specifies the services of the **Authorization** subdomain of NexusMarket.

Cross-cutting validations of the general restrictions RG-01, RG-02 and RG-03 and of ownership over business information.

All services live in the package `nexusmarket.domain.services.authorization`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Use case (input port) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `ValidateUserStatusService` | — (internal) | Internal (used by every protected service). |
| 2 | `ValidateRoleAuthorizationService` | — (internal) | Internal (used by every protected service). |
| 3 | `ValidateSellerOwnershipService` | — (internal) | Internal. |
| 4 | `ValidateBuyerOwnershipService` | — (internal) | Internal. |

---

# 1. ValidateUserStatusService

## Description

RG-01: validates that the operation is executed by an authenticated user that is currently `ACTIVE`.

## Implementation

* Class: `nexusmarket.domain.services.authorization.ValidateUserStatusService` (`@Service`, `@RequiredArgsConstructor`).
* Internal service: it has no use case interface and is not exposed in any role port.

```java
User execute(User user);
```

## Authorized Roles

Internal (used by every protected service).

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `user` | `User` | User reconstructed from the token. |

**Output:** `User`. Returns the stored (authoritative) user.

## Business Rules

* The user is reloaded through `UserRepositoryPort` so that a user blocked after obtaining its token cannot operate; the stored user is the one used by the calling service.

## Validations

* The user must be provided, must exist and must be `ACTIVE`.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | No user, unknown user or user not `ACTIVE`. |

## Output Ports Used

* `UserRepositoryPort`

## Audit

Not audited (read-only or authentication check).

---

# 2. ValidateRoleAuthorizationService

## Description

RG-02 and RG-03: validates RG-01 and that the single role of the user is one of the roles allowed for the operation.

## Implementation

* Class: `nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService` (`@Service`, `@RequiredArgsConstructor`).
* Internal service: it has no use case interface and is not exposed in any role port.

```java
User execute(User user, SystemRole... allowedRoles);
```

## Authorized Roles

Internal (used by every protected service).

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `user` | `User` | User reconstructed from the token. |
| `allowedRoles` | `SystemRole...` | Roles allowed for the operation. |

**Output:** `User`. Returns the stored (authoritative) user.

## Business Rules

* Delegates RG-01 to `ValidateUserStatusService`.
* Each user holds exactly one role (RG-02); it must belong to `allowedRoles` (RG-03).

## Validations

* The stored user's role must be one of `allowedRoles`.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | RG-01 fails or the role is not allowed. |

## Output Ports Used

None.

## Collaborating Services

* `ValidateUserStatusService`

## Audit

Not audited (read-only or authentication check).

---

# 3. ValidateSellerOwnershipService

## Description

RG-03 for sellers: validates that a seller only manages its own information.

## Implementation

* Class: `nexusmarket.domain.services.authorization.ValidateSellerOwnershipService` (`@Service`, `@RequiredArgsConstructor`).
* Internal service: it has no use case interface and is not exposed in any role port.

```java
void execute(User user, Seller seller);
void execute(User user, Product product);
void execute(User user, Warehouse warehouse);
```

## Authorized Roles

Internal.

## Input — `execute`

| Parameter | Type | Description |
| --- | --- | --- |
| `user` | `User` | Authoritative requesting user. |
| `seller` | `Seller` | Seller accessed. |

**Output:** Ownership over a seller.

## Input — `execute`

| Parameter | Type | Description |
| --- | --- | --- |
| `user` | `User` | Authoritative requesting user. |
| `product` | `Product` | Product accessed. |

**Output:** Ownership over a product.

## Input — `execute`

| Parameter | Type | Description |
| --- | --- | --- |
| `user` | `User` | Authoritative requesting user. |
| `warehouse` | `Warehouse` | Warehouse accessed. |

**Output:** Ownership over a warehouse (must be of type `SELLER`).

## Business Rules

* Only applies to users with role `SELLER`; for other roles the check is skipped (role authorization is done by `ValidateRoleAuthorizationService`).

## Validations

* The seller / product owner / warehouse owner must be the requesting seller.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The information belongs to another seller or to the Marketplace. |

## Output Ports Used

None.

## Audit

Not audited (read-only or authentication check).

---

# 4. ValidateBuyerOwnershipService

## Description

RG-03 for buyers: validates that a buyer only accesses its own information.

## Implementation

* Class: `nexusmarket.domain.services.authorization.ValidateBuyerOwnershipService` (`@Service`, `@RequiredArgsConstructor`).
* Internal service: it has no use case interface and is not exposed in any role port.

```java
void execute(User user, Buyer buyer);
void execute(User user, Order order);
```

## Authorized Roles

Internal.

## Input — `execute`

| Parameter | Type | Description |
| --- | --- | --- |
| `user` | `User` | Authoritative requesting user. |
| `buyer` | `Buyer` | Buyer accessed. |

**Output:** Ownership over a buyer.

## Input — `execute`

| Parameter | Type | Description |
| --- | --- | --- |
| `user` | `User` | Authoritative requesting user. |
| `order` | `Order` | Order accessed. |

**Output:** Ownership over an order.

## Business Rules

* Only applies to users with role `BUYER`; for other roles the check is skipped.

## Validations

* The buyer / order buyer must be the requesting buyer.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The information belongs to another buyer. |

## Output Ports Used

None.

## Audit

Not audited (read-only or authentication check).

---
