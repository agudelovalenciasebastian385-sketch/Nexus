# Cart Management Services

## Introduction

This document specifies the services of the **Cart Management** subdomain of NexusMarket.

The buyer's provisional selection of published products before confirming an order.

All services live in the package `nexusmarket.domain.services.cart`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Exposed in (role ports) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `AddItemToCartService` | `BuyerPort` | `BUYER` (owner of the cart). |
| 2 | `RemoveItemFromCartService` | `BuyerPort` | `BUYER` (owner of the cart). |
| 3 | `UpdateCartItemQuantityService` | `BuyerPort` | `BUYER` (owner of the cart). |
| 4 | `ConsultCartService` | `BuyerPort`, `SupervisorPort` | `BUYER` over its own cart; `SUPERVISOR` over any cart. |
| 5 | `ClearCartService` | `BuyerPort` | `BUYER` (owner of the cart). |

---

# 1. AddItemToCartService

## Description

Adds a published product to the requesting buyer's cart at its current price.

## Implementation

* Class: `nexusmarket.domain.services.cart.AddItemToCartService` (`@Service`, `@RequiredArgsConstructor`).

```java
Cart addItemToCart(User requestingUser, String productId, Variant variant, int quantity);
```

## Authorized Roles

`BUYER` (owner of the cart).

Exposed in: `BuyerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `productId` | `String` | Identifier of the product. |
| `variant` | `Variant` | Chosen variant (`null` if not applicable). |
| `quantity` | `int` | Units (> 0). |

**Output:** `Cart`. Returns the updated cart.

## Business Rules

* Only `PUBLISHED` products can be added.
* The unit price is taken from `Product.price`; the buyer never sets it.
* Adding the same product and variant accumulates the quantity.

## Validations

* RG-01 and role `BUYER`.
* The product must exist.
* The rules of `CartItem` apply.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active buyer. |
| `EntityNotFoundException` | The buyer, its cart or the product does not exist. |
| `InvalidProductStatusException` | The product is not `PUBLISHED`. |
| `InvalidCartException` | Invalid quantity or variant not offered. |

## Output Ports Used

* `BuyerRepositoryPort`
* `CartRepositoryPort`
* `ProductRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.CART_ITEM_ADDED`

---

# 2. RemoveItemFromCartService

## Description

Removes a product/variant line from the requesting buyer's cart.

## Implementation

* Class: `nexusmarket.domain.services.cart.RemoveItemFromCartService` (`@Service`, `@RequiredArgsConstructor`).

```java
Cart removeItemFromCart(User requestingUser, String productId, Variant variant);
```

## Authorized Roles

`BUYER` (owner of the cart).

Exposed in: `BuyerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `productId` | `String` | Identifier of the product. |
| `variant` | `Variant` | Variant of the line. |

**Output:** `Cart`. Returns the updated cart.

## Business Rules

* Only the owner of the cart can modify it.

## Validations

* RG-01 and role `BUYER`.
* The line must be in the cart.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active buyer. |
| `EntityNotFoundException` | The buyer, its cart or the product does not exist. |
| `InvalidCartException` | The product is not in the cart. |

## Output Ports Used

* `BuyerRepositoryPort`
* `CartRepositoryPort`
* `ProductRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.CART_ITEM_REMOVED`

---

# 3. UpdateCartItemQuantityService

## Description

Replaces the quantity of a product/variant line of the requesting buyer's cart.

## Implementation

* Class: `nexusmarket.domain.services.cart.UpdateCartItemQuantityService` (`@Service`, `@RequiredArgsConstructor`).

```java
Cart updateCartItemQuantity(User requestingUser, String productId, Variant variant, int quantity);
```

## Authorized Roles

`BUYER` (owner of the cart).

Exposed in: `BuyerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `productId` | `String` | Identifier of the product. |
| `variant` | `Variant` | Variant of the line. |
| `quantity` | `int` | New quantity (> 0). |

**Output:** `Cart`. Returns the updated cart.

## Business Rules

* Only the owner of the cart can modify it.

## Validations

* RG-01 and role `BUYER`.
* The line must be in the cart.
* `quantity` must be positive.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active buyer. |
| `EntityNotFoundException` | The buyer, its cart or the product does not exist. |
| `InvalidCartException` | The product is not in the cart or invalid quantity. |

## Output Ports Used

* `BuyerRepositoryPort`
* `CartRepositoryPort`
* `ProductRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.CART_ITEM_QUANTITY_UPDATED`

---

# 4. ConsultCartService

## Description

Retrieves the cart of a buyer with its items and total.

## Implementation

* Class: `nexusmarket.domain.services.cart.ConsultCartService` (`@Service`, `@RequiredArgsConstructor`).

```java
Cart consultCart(User requestingUser, String buyerId);
```

## Authorized Roles

`BUYER` over its own cart; `SUPERVISOR` over any cart.

Exposed in: `BuyerPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `buyerId` | `String` | Identifier of the buyer owning the cart. |

**Output:** `Cart`. Returns the cart.

## Business Rules

* Read-only operation.

## Validations

* RG-01 and allowed role.
* A buyer only consults its own cart (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or cart of another buyer. |
| `EntityNotFoundException` | The buyer or its cart does not exist. |

## Output Ports Used

* `BuyerRepositoryPort`
* `CartRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateBuyerOwnershipService`

## Audit

Not audited (read-only or authentication check).

---

# 5. ClearCartService

## Description

Removes every item from the requesting buyer's cart.

## Implementation

* Class: `nexusmarket.domain.services.cart.ClearCartService` (`@Service`, `@RequiredArgsConstructor`).

```java
Cart clearCart(User requestingUser);
```

## Authorized Roles

`BUYER` (owner of the cart).

Exposed in: `BuyerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |

**Output:** `Cart`. Returns the empty cart.

## Business Rules

* Only the owner of the cart can modify it.

## Validations

* RG-01 and role `BUYER`.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active buyer. |
| `EntityNotFoundException` | The buyer or its cart does not exist. |

## Output Ports Used

* `BuyerRepositoryPort`
* `CartRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.CART_CLEARED`

---
