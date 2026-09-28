# Catalog Management Services

## Introduction

This document specifies the services of the **Catalog Management** subdomain of NexusMarket.

Registration and maintenance of physical and digital products, their publication lifecycle and the public catalog.

All services live in the package `nexusmarket.domain.services.product`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Use case (input port) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `RegisterProductService` | `RegisterProductUseCase` | `SELLER`. |
| 2 | `UpdateProductService` | `UpdateProductUseCase` | `SELLER` over its own products. |
| 3 | `PublishProductService` | `PublishProductUseCase` | `SELLER` over its own products. |
| 4 | `SuspendProductService` | `SuspendProductUseCase` | `SELLER` over its own products. |
| 5 | `DiscontinueProductService` | `DiscontinueProductUseCase` | `SELLER` over its own products. |
| 6 | `ConsultCatalogService` | `ConsultCatalogUseCase` | Public (no authentication required). |
| 7 | `ConsultSellerProductsService` | `ConsultSellerProductsUseCase` | `SELLER` over its own products; `ADMINISTRATOR` and `SUPERVISOR` over any seller. |

---

# 1. RegisterProductService

## Description

Registers a physical or digital product of the requesting seller in `DRAFT` status.

## Implementation

* Class: `nexusmarket.domain.services.product.RegisterProductService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.RegisterProductUseCase`.

```java
Product registerProduct(User requestingUser, ProductType type, String name, String description, BigDecimal price, List<Variant> variants);
```

## Authorized Roles

`SELLER`.

Exposed in: `SellerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `type` | `ProductType` | `PHYSICAL` or `DIGITAL`. |
| `name` | `String` | Commercial name. |
| `description` | `String` | Functional description. |
| `price` | `BigDecimal` | Current unit price. |
| `variants` | `List<Variant>` | Offered variants (may be empty). |

**Output:** `Product`. Returns the registered product.

## Business Rules

* A `PhysicalProduct` or a `DigitalProduct` is created according to `type`.
* The product starts in `DRAFT` (not visible in the catalog) and belongs to the requesting seller.

## Validations

* RG-01 and role `SELLER`.
* `type` is required.
* Product data is validated by the entity.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active seller. |
| `EntityNotFoundException` | The requesting seller does not exist. |
| `InvalidProductException` | Invalid product data. |

## Output Ports Used

* `SellerRepositoryPort`
* `ProductRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.PRODUCT_REGISTERED`

---

# 2. UpdateProductService

## Description

Updates the commercial information of a product of the requesting seller.

## Implementation

* Class: `nexusmarket.domain.services.product.UpdateProductService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.UpdateProductUseCase`.

```java
Product updateProduct(User requestingUser, String productId, String name, String description, BigDecimal price, List<Variant> variants);
```

## Authorized Roles

`SELLER` over its own products.

Exposed in: `SellerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `productId` | `String` | Identifier of the product. |
| `name` | `String` | New name. |
| `description` | `String` | New description. |
| `price` | `BigDecimal` | New unit price. |
| `variants` | `List<Variant>` | New list of variants. |

**Output:** `Product`. Returns the updated product.

## Business Rules

* A seller only manages its own products.
* The new price only affects future cart items; order items keep the price of the purchase.

## Validations

* RG-01 and role `SELLER`.
* Ownership of the product (RG-03).
* Product data is validated by the entity.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or product of another seller. |
| `EntityNotFoundException` | The product does not exist. |
| `InvalidProductException` | Invalid product data. |

## Output Ports Used

* `ProductRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.PRODUCT_UPDATED`

---

# 3. PublishProductService

## Description

Makes a product visible in the public catalog (`DRAFT` or `SUSPENDED` → `PUBLISHED`).

## Implementation

* Class: `nexusmarket.domain.services.product.PublishProductService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.PublishProductUseCase`.

```java
Product publishProduct(User requestingUser, String productId);
```

## Authorized Roles

`SELLER` over its own products.

Exposed in: `SellerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `productId` | `String` | Identifier of the product. |

**Output:** `Product`. Returns the published product.

## Business Rules

* The transition is applied by `Product.publish()`.

## Validations

* RG-01 and role `SELLER`.
* Ownership of the product (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or product of another seller. |
| `EntityNotFoundException` | The product does not exist. |
| `InvalidProductStatusException` | The product is not `DRAFT` or `SUSPENDED`. |

## Output Ports Used

* `ProductRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.PRODUCT_PUBLISHED`

---

# 4. SuspendProductService

## Description

Temporarily removes a product from the catalog (`PUBLISHED` → `SUSPENDED`).

## Implementation

* Class: `nexusmarket.domain.services.product.SuspendProductService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.SuspendProductUseCase`.

```java
Product suspendProduct(User requestingUser, String productId);
```

## Authorized Roles

`SELLER` over its own products.

Exposed in: `SellerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `productId` | `String` | Identifier of the product. |

**Output:** `Product`. Returns the suspended product.

## Business Rules

* The transition is applied by `Product.suspend()`.

## Validations

* RG-01 and role `SELLER`.
* Ownership of the product (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or product of another seller. |
| `EntityNotFoundException` | The product does not exist. |
| `InvalidProductStatusException` | The product is not `PUBLISHED`. |

## Output Ports Used

* `ProductRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.PRODUCT_SUSPENDED`

---

# 5. DiscontinueProductService

## Description

Permanently withdraws a product from the catalog (any status except `DISCONTINUED` → `DISCONTINUED`).

## Implementation

* Class: `nexusmarket.domain.services.product.DiscontinueProductService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.DiscontinueProductUseCase`.

```java
Product discontinueProduct(User requestingUser, String productId);
```

## Authorized Roles

`SELLER` over its own products.

Exposed in: `SellerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `productId` | `String` | Identifier of the product. |

**Output:** `Product`. Returns the discontinued product.

## Business Rules

* The transition is applied by `Product.discontinue()`; `DISCONTINUED` is terminal.

## Validations

* RG-01 and role `SELLER`.
* Ownership of the product (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or product of another seller. |
| `EntityNotFoundException` | The product does not exist. |
| `InvalidProductStatusException` | The product is already `DISCONTINUED`. |

## Output Ports Used

* `ProductRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.PRODUCT_DISCONTINUED`

---

# 6. ConsultCatalogService

## Description

Retrieves the public catalog: only products in `PUBLISHED` status.

## Implementation

* Class: `nexusmarket.domain.services.product.ConsultCatalogService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.ConsultCatalogUseCase`.

```java
List<Product> consultCatalog();
Product consultCatalogProduct(String productId);
```

## Authorized Roles

Public (no authentication required).

Exposed in: `PublicAccessPort`.

## Input — `consultCatalog`

No parameters.

**Output:** `List<Product>`. Returns the published products.

## Input — `consultCatalogProduct`

| Parameter | Type | Description |
| --- | --- | --- |
| `productId` | `String` | Identifier of the product. |

**Output:** `Product`. Returns one published product.

## Business Rules

* Products in `DRAFT`, `SUSPENDED` or `DISCONTINUED` are never visible in the public catalog.

## Validations

* A product that is not `PUBLISHED` is treated as not found.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `EntityNotFoundException` | The product does not exist or is not published. |

## Output Ports Used

* `ProductRepositoryPort`

## Audit

Not audited (read-only or authentication check).

---

# 7. ConsultSellerProductsService

## Description

Retrieves all the products of a seller, in any status.

## Implementation

* Class: `nexusmarket.domain.services.product.ConsultSellerProductsService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.ConsultSellerProductsUseCase`.

```java
List<Product> consultSellerProducts(User requestingUser, String sellerId);
```

## Authorized Roles

`SELLER` over its own products; `ADMINISTRATOR` and `SUPERVISOR` over any seller.

Exposed in: `AdministratorPort`, `SellerPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `sellerId` | `String` | Identifier of the seller. |

**Output:** `List<Product>`. Returns the seller's products.

## Business Rules

* Read-only operation.

## Validations

* RG-01 and allowed role.
* A seller only consults its own products (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or products of another seller. |
| `EntityNotFoundException` | The seller does not exist. |

## Output Ports Used

* `SellerRepositoryPort`
* `ProductRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`

## Audit

Not audited (read-only or authentication check).

---
