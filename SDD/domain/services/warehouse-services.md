# Warehouse Management Services

## Introduction

This document specifies the services of the **Warehouse Management** subdomain of NexusMarket.

Registration, consultation and maintenance of Marketplace and Seller warehouses.

All services live in the package `nexusmarket.domain.services.warehouse`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Exposed in (role ports) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `RegisterWarehouseService` | `AdministratorPort`, `SellerPort` | `ADMINISTRATOR` (type `MARKETPLACE`); `SELLER` (type `SELLER`, owned by itself). |
| 2 | `ConsultWarehouseService` | `AdministratorPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` | `ADMINISTRATOR`, `LOGISTICS_OPERATOR` and `SUPERVISOR` over any warehouse; `SELLER` over its own warehouses. |
| 3 | `UpdateWarehouseService` | `AdministratorPort`, `SellerPort` | `ADMINISTRATOR` over `MARKETPLACE` warehouses; `SELLER` over its own warehouses. |

---

# 1. RegisterWarehouseService

## Description

Registers a warehouse: the administrator registers Marketplace warehouses and a seller registers additional warehouses of its own.

## Implementation

* Class: `nexusmarket.domain.services.warehouse.RegisterWarehouseService` (`@Service`, `@RequiredArgsConstructor`).

```java
Warehouse registerWarehouse(User requestingUser, String name, Address location, WarehouseType type);
```

## Authorized Roles

`ADMINISTRATOR` (type `MARKETPLACE`); `SELLER` (type `SELLER`, owned by itself).

Exposed in: `AdministratorPort`, `SellerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `name` | `String` | Name of the warehouse. |
| `location` | `Address` | Location of the warehouse. |
| `type` | `WarehouseType` | `MARKETPLACE` or `SELLER`. |

**Output:** `Warehouse`. Returns the registered warehouse.

## Business Rules

* An administrator can only register `MARKETPLACE` warehouses (without seller).
* A seller can only register `SELLER` warehouses; the owner is always the requesting seller.

## Validations

* RG-01 and allowed role.
* The type must correspond to the role.
* Warehouse data is validated by the entity.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or type not allowed for the role. |
| `EntityNotFoundException` | The requesting seller does not exist. |
| `InvalidWarehouseException` | Invalid warehouse data. |

## Output Ports Used

* `SellerRepositoryPort`
* `WarehouseRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.WAREHOUSE_REGISTERED`

---

# 2. ConsultWarehouseService

## Description

Retrieves one warehouse or the list of warehouses visible to the requesting user.

## Implementation

* Class: `nexusmarket.domain.services.warehouse.ConsultWarehouseService` (`@Service`, `@RequiredArgsConstructor`).

```java
Warehouse consultWarehouse(User requestingUser, String warehouseId);
List<Warehouse> consultWarehouses(User requestingUser);
```

## Authorized Roles

`ADMINISTRATOR`, `LOGISTICS_OPERATOR` and `SUPERVISOR` over any warehouse; `SELLER` over its own warehouses.

Exposed in: `AdministratorPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort`.

## Input — `consultWarehouse`

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `warehouseId` | `String` | Identifier of the warehouse. |

**Output:** `Warehouse`. Returns the warehouse.

## Input — `consultWarehouses`

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |

**Output:** `List<Warehouse>`. Returns all warehouses, or only the own ones for a seller.

## Business Rules

* Read-only operation.

## Validations

* RG-01 and allowed role.
* A seller only accesses its own warehouses (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or another seller's warehouse. |
| `EntityNotFoundException` | The warehouse or the requesting seller does not exist. |

## Output Ports Used

* `WarehouseRepositoryPort`
* `SellerRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`

## Audit

Not audited (read-only or authentication check).

---

# 3. UpdateWarehouseService

## Description

Updates the name and location of a warehouse.

## Implementation

* Class: `nexusmarket.domain.services.warehouse.UpdateWarehouseService` (`@Service`, `@RequiredArgsConstructor`).

```java
Warehouse updateWarehouse(User requestingUser, String warehouseId, String name, Address location);
```

## Authorized Roles

`ADMINISTRATOR` over `MARKETPLACE` warehouses; `SELLER` over its own warehouses.

Exposed in: `AdministratorPort`, `SellerPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `warehouseId` | `String` | Identifier of the warehouse. |
| `name` | `String` | New name. |
| `location` | `Address` | New location. |

**Output:** `Warehouse`. Returns the updated warehouse.

## Business Rules

* The type and the owner of a warehouse never change.

## Validations

* RG-01 and allowed role.
* Administrator: only `MARKETPLACE` warehouses.
* Seller: only its own warehouses (RG-03).
* Name and location are validated by the entity.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or warehouse outside the role. |
| `EntityNotFoundException` | The warehouse does not exist. |
| `InvalidWarehouseException` | Invalid name or location. |

## Output Ports Used

* `WarehouseRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.WAREHOUSE_UPDATED`

---
