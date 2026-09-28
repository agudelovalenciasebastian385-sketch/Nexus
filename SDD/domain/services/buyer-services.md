# Buyer Management Services

## Introduction

This document specifies the services of the **Buyer Management** subdomain of NexusMarket.

Consultation and maintenance of the buyer profile, delivery addresses and commercial status.

All services live in the package `nexusmarket.domain.services.buyer`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Use case (input port) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `ConsultBuyerService` | `ConsultBuyerUseCase` | `BUYER` over itself; `ADMINISTRATOR` and `SUPERVISOR` over any buyer. |
| 2 | `UpdateBuyerProfileService` | `UpdateBuyerProfileUseCase` | `BUYER` over itself. |
| 3 | `ChangeBuyerCommercialStatusService` | `ChangeBuyerCommercialStatusUseCase` | `ADMINISTRATOR`. |

---

# 1. ConsultBuyer

## Description

Retrieves the information of a buyer.

## Implementation

* Class: `nexusmarket.domain.services.buyer.ConsultBuyerService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.ConsultBuyerUseCase`.

```java
Buyer consultBuyer(User requestingUser, String buyerId);
```

## Authorized Roles

`BUYER` over itself; `ADMINISTRATOR` and `SUPERVISOR` over any buyer.

Exposed in: `AdministratorPort`, `BuyerPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `buyerId` | `String` | Identifier of the buyer. |

**Output:** `Buyer`. Returns the buyer.

## Business Rules

* A buyer never consults information of other buyers.

## Validations

* RG-01 and allowed role.
* A buyer can only consult itself (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or another buyer's information. |
| `EntityNotFoundException` | The buyer does not exist. |

## Output Ports Used

* `BuyerRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateBuyerOwnershipService`

## Audit

Not audited (read-only or authentication check).

---

# 2. UpdateBuyerProfile

## Description

Updates the profile of the requesting buyer: full name, primary address and additional addresses.

## Implementation

* Class: `nexusmarket.domain.services.buyer.UpdateBuyerProfileService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.UpdateBuyerProfileUseCase`.

```java
Buyer updateBuyerProfile(User requestingUser, String fullName, Address primaryAddress, List<Address> additionalAddresses);
```

## Authorized Roles

`BUYER` over itself.

Exposed in: `BuyerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `fullName` | `String` | New full name. |
| `primaryAddress` | `Address` | New primary address. |
| `additionalAddresses` | `List<Address>` | New list of additional addresses (`null` = empty list). |

**Output:** `Buyer`. Returns the updated buyer.

## Business Rules

* The buyer updated is always the requesting user.
* `additionalAddresses` replaces the current list of additional addresses.

## Validations

* RG-01 and role `BUYER`.
* `fullName` cannot be empty and `primaryAddress` is required.
* Additional addresses cannot be duplicated.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active buyer. |
| `EntityNotFoundException` | The buyer does not exist. |
| `InvalidUserException` | Invalid profile data or duplicated address. |

## Output Ports Used

* `BuyerRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.BUYER_PROFILE_UPDATED`

---

# 3. ChangeBuyerCommercialStatus

## Description

Changes the commercial status of a buyer (`ENABLED`, `RESTRICTED`, `SUSPENDED`).

## Implementation

* Class: `nexusmarket.domain.services.buyer.ChangeBuyerCommercialStatusService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.ChangeBuyerCommercialStatusUseCase`.

```java
Buyer changeBuyerCommercialStatus(User requestingUser, String buyerId, CommercialStatus newStatus);
```

## Authorized Roles

`ADMINISTRATOR`.

Exposed in: `AdministratorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `buyerId` | `String` | Identifier of the buyer. |
| `newStatus` | `CommercialStatus` | New commercial status. |

**Output:** `Buyer`. Returns the updated buyer.

## Business Rules

* Only `ENABLED` buyers can confirm orders (`Buyer.canPurchase`).

## Validations

* RG-01 and role `ADMINISTRATOR`.
* The buyer must exist.
* `newStatus` is required.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active administrator. |
| `EntityNotFoundException` | The buyer does not exist. |
| `InvalidUserException` | `newStatus` is not provided. |

## Output Ports Used

* `BuyerRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.BUYER_COMMERCIAL_STATUS_CHANGED`

---
