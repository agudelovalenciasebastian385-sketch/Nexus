# Seller Management Services

## Introduction

This document specifies the services of the **Seller Management** subdomain of NexusMarket.

Administrative onboarding of sellers together with their first warehouse, and maintenance of seller information.

All services live in the package `nexusmarket.domain.services.seller`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Exposed in (role ports) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `RegisterSellerService` | `AdministratorPort` | `ADMINISTRATOR` (sellers cannot self-register). |
| 2 | `ConsultSellerService` | `AdministratorPort`, `SellerPort`, `SupervisorPort` | `SELLER` over itself; `ADMINISTRATOR` and `SUPERVISOR` over any seller. |
| 3 | `UpdateSellerService` | `AdministratorPort`, `SellerPort` | `ADMINISTRATOR` over any seller; `SELLER` over itself. |

---

# 1. RegisterSellerService

## Description

Onboards a seller together with its first warehouse of type `SELLER` in the same operation.

## Implementation

* Class: `nexusmarket.domain.services.seller.RegisterSellerService` (`@Service`, `@RequiredArgsConstructor`).

```java
Seller registerSeller(User requestingUser, String identificationNumber, String fullName, String email, String rawPassword, String warehouseName, Address warehouseLocation);
```

## Authorized Roles

`ADMINISTRATOR` (sellers cannot self-register).

Exposed in: `AdministratorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `identificationNumber` | `String` | Identity document number. |
| `fullName` | `String` | Official name. |
| `email` | `String` | Access and communication email. |
| `rawPassword` | `String` | Initial plain password. |
| `warehouseName` | `String` | Name of the first warehouse. |
| `warehouseLocation` | `Address` | Location of the first warehouse. |

**Output:** `Seller`. Returns the seller with its first warehouse associated.

## Business Rules

* Sellers cannot self-register; they are onboarded exclusively by the administrator.
* The seller is created `ACTIVE` and its first `Warehouse` of type `SELLER` is created and associated.
* The password is encrypted through `PasswordServicePort`.
* The email is normalized with `User.normalizeEmail` (trimmed and lower-cased) before the uniqueness check, so `Juan@Gmail.com` and `juan@gmail.com` are the same email.

## Validations

* RG-01 and role `ADMINISTRATOR`.
* `identificationNumber` and `email` must be unique.
* `rawPassword` cannot be empty.
* Seller and warehouse data are validated by the entities.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active administrator. |
| `UserAlreadyExistsException` | The identification number or the email is already registered. |
| `InvalidUserException` | Empty password or invalid seller data. |
| `InvalidWarehouseException` | Invalid warehouse data. |

## Output Ports Used

* `UserRepositoryPort`
* `SellerRepositoryPort`
* `WarehouseRepositoryPort`
* `PasswordServicePort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.SELLER_REGISTERED`
* `AuditAction.WAREHOUSE_REGISTERED`

---

# 2. ConsultSellerService

## Description

Retrieves the information of a seller.

## Implementation

* Class: `nexusmarket.domain.services.seller.ConsultSellerService` (`@Service`, `@RequiredArgsConstructor`).

```java
Seller consultSeller(User requestingUser, String sellerId);
```

## Authorized Roles

`SELLER` over itself; `ADMINISTRATOR` and `SUPERVISOR` over any seller.

Exposed in: `AdministratorPort`, `SellerPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `sellerId` | `String` | Identifier of the seller. |

**Output:** `Seller`. Returns the seller.

## Business Rules

* Read-only operation.

## Validations

* RG-01 and allowed role.
* A seller can only consult itself (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or another seller's information. |
| `EntityNotFoundException` | The seller does not exist. |

## Output Ports Used

* `SellerRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`

## Audit

Not audited (read-only or authentication check).

---

# 3. UpdateSellerService

## Description

Updates the full name of a seller.

## Implementation

* Class: `nexusmarket.domain.services.seller.UpdateSellerService` (`@Service`, `@RequiredArgsConstructor`).

```java
Seller updateSeller(User requestingUser, String sellerId, String fullName);
```

## Authorized Roles

`ADMINISTRATOR` over any seller; `SELLER` over itself.

Exposed in: `AdministratorPort`, `SellerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `sellerId` | `String` | Identifier of the seller. |
| `fullName` | `String` | New full name. |

**Output:** `Seller`. Returns the updated seller.

## Business Rules

* Only `fullName` can be updated; `identificationNumber` and `email` are immutable.

## Validations

* RG-01 and allowed role.
* A seller can only update itself (RG-03).
* `fullName` cannot be empty.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or another seller's information. |
| `EntityNotFoundException` | The seller does not exist. |
| `InvalidUserException` | Empty full name. |

## Output Ports Used

* `SellerRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.SELLER_UPDATED`

---
