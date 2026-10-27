# Audit Services

## Introduction

This document specifies the services of the **Audit** subdomain of NexusMarket.

Immutable registration of every significant business operation and consultation of the audit trail.

All services live in the package `nexusmarket.domain.services.audit`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Exposed in (role ports) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `RegisterAuditLogService` | — (internal) | Internal (used by every service that changes information). |
| 2 | `ConsultAuditLogsService` | `AdministratorPort`, `SupervisorPort` | `ADMINISTRATOR` and `SUPERVISOR`. |

---

# 1. RegisterAuditLogService

## Description

Registers an immutable audit record of a significant business operation.

## Implementation

* Class: `nexusmarket.domain.services.audit.RegisterAuditLogService` (`@Service`, `@RequiredArgsConstructor`).
* Internal service: it is not exposed in any role port.

```java
AuditLog execute(User performedBy, AuditAction action, AuditEntityType entityType, String entityId, Map<String, String> details);
```

## Authorized Roles

Internal (used by every service that changes information).

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `performedBy` | `User` | User who performed the operation. |
| `action` | `AuditAction` | Audited action. |
| `entityType` | `AuditEntityType` | Type of the affected entity. |
| `entityId` | `String` | Identifier of the affected entity. |
| `details` | `Map<String, String>` | Operation-specific details (may be empty). |

**Output:** `AuditLog`. Returns the stored audit record.

## Business Rules

* The record gets a generated id and the current date-time; the role is taken from `performedBy`.
* Audit records are append-only: they are never updated or deleted.
* Inventory records have no id of their own: their `entityId` is `<productId>:<warehouseId>`.

## Validations

* The data is validated by the `AuditLog` entity.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `InvalidAuditLogException` | Missing required audit data. |

## Output Ports Used

* `AuditLogRepositoryPort`

## Audit

Not applicable.

---

# 2. ConsultAuditLogsService

## Description

Retrieves audit records: all of them, those performed by a user, or those of an entity.

## Implementation

* Class: `nexusmarket.domain.services.audit.ConsultAuditLogsService` (`@Service`, `@RequiredArgsConstructor`).

```java
List<AuditLog> consultAuditLogs(User requestingUser);
List<AuditLog> consultAuditLogsByUser(User requestingUser, String userId);
List<AuditLog> consultAuditLogsByEntity(User requestingUser, AuditEntityType entityType, String entityId);
```

## Authorized Roles

`ADMINISTRATOR` and `SUPERVISOR`.

Exposed in: `AdministratorPort`, `SupervisorPort`.

## Input — `consultAuditLogs`

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |

**Output:** `List<AuditLog>`. Returns all audit records.

## Input — `consultAuditLogsByUser`

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `userId` | `String` | Identifier of the user who performed the operations. |

**Output:** `List<AuditLog>`. Returns the records performed by the user.

## Input — `consultAuditLogsByEntity`

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `entityType` | `AuditEntityType` | Type of the entity. |
| `entityId` | `String` | Identifier of the entity. |

**Output:** `List<AuditLog>`. Returns the records of the entity.

## Business Rules

* Read-only operation; the supervisor is read-only across the whole system.

## Validations

* RG-01 and role `ADMINISTRATOR` or `SUPERVISOR`.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed. |
| `EntityNotFoundException` | The user of `consultAuditLogsByUser` does not exist. |

## Output Ports Used

* `AuditLogRepositoryPort`
* `UserRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`

## Audit

Not applicable.

---
