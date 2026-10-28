# Reporting Services

## Introduction

This document specifies the services of the **Reporting** subdomain of NexusMarket.

Consolidation of administrative information (OBJ-12): commercial, seller performance, inventory and logistics reports. Reports are read-only, computed when consulted and never persisted.

All services live in the package `nexusmarket.domain.services.reporting`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Exposed in (role ports) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `ConsultCommercialReportService` | `AdministratorPort`, `SupervisorPort` | `ADMINISTRATOR` and `SUPERVISOR`. |
| 2 | `ConsultSellerPerformanceReportService` | `AdministratorPort`, `SellerPort`, `SupervisorPort` | `ADMINISTRATOR` and `SUPERVISOR` over any seller; `SELLER` only over itself. |
| 3 | `ConsultInventoryReportService` | `AdministratorPort`, `LogisticsOperatorPort`, `SupervisorPort` | `ADMINISTRATOR`, `SUPERVISOR` and `LOGISTICS_OPERATOR`. |
| 4 | `ConsultLogisticsReportService` | `AdministratorPort`, `SupervisorPort` | `ADMINISTRATOR` and `SUPERVISOR`. |

---

# 1. ConsultCommercialReportService

## Description

Consolidates the commercial figures of a period: orders created per status, total invoiced, total refunded and net revenue.

## Implementation

* Class: `nexusmarket.domain.services.reporting.ConsultCommercialReportService` (`@Service`, `@RequiredArgsConstructor`).

```java
CommercialReport consultCommercialReport(User requestingUser, ReportPeriod period);
```

## Authorized Roles

`ADMINISTRATOR` and `SUPERVISOR`.

Exposed in: `AdministratorPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `period` | `ReportPeriod` | Inclusive range of days of the report. |

**Output:** `CommercialReport`. Returns the commercial report of the period.

## Business Rules

* The period is converted with `ReportPeriod.startDateTime()` (inclusive) and `ReportPeriod.endDateTimeExclusive()` (exclusive), so the end date is covered completely.
* Orders: those whose `createdAt` falls in the period, counted per `OrderStatus` (every status is reported, 0 when there are none).
* Total invoiced: sum of `Invoice.totalAmount` of the invoices whose `issuedAt` falls in the period.
* Total refunded: sum of `Refund.amount` of the refunds whose `processedAt` falls in the period (only `PROCESSED` refunds have a `processedAt`).
* Net revenue = total invoiced - total refunded (computed by `CommercialReport.getNetRevenue`).
* Read-only operation: it does not modify information; only the consultation is audited.

## Validations

* RG-01 and role `ADMINISTRATOR` or `SUPERVISOR`.
* `period` is required.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed. |
| `InvalidReportPeriodException` | `period` is not provided. |

## Output Ports Used

* `OrderRepositoryPort`
* `InvoiceRepositoryPort`
* `RefundRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.REPORT_CONSULTED`

---

# 2. ConsultSellerPerformanceReportService

## Description

Consolidates the performance of a seller in a period: products per status, units and amount sold, approved returns and return rate.

## Implementation

* Class: `nexusmarket.domain.services.reporting.ConsultSellerPerformanceReportService` (`@Service`, `@RequiredArgsConstructor`).

```java
SellerPerformanceReport consultSellerPerformanceReport(User requestingUser, String sellerId, ReportPeriod period);
```

## Authorized Roles

`ADMINISTRATOR` and `SUPERVISOR` over any seller; `SELLER` only over itself.

Exposed in: `AdministratorPort`, `SellerPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `sellerId` | `String` | Identifier of the seller. |
| `period` | `ReportPeriod` | Inclusive range of days of the report. |

**Output:** `SellerPerformanceReport`. Returns the performance report of the seller.

## Business Rules

* Products: current products of the seller (`ProductRepositoryPort.findBySeller`) counted per `ProductStatus`; it is a snapshot, because products have no date.
* Sold orders: orders whose `createdAt` falls in the period and whose status is `PAID`, `SHIPPED` or `DELIVERED`, considering only the items whose product belongs to the seller.
* Units sold = sum of those items' `quantity`; amount sold = sum of their `subtotal()` (unit price at the time of purchase).
* Approved returns: return requests in status `APPROVED` of the sold orders that contain products of the seller (a return request covers the whole order; an order has at most one active return request).
* Return rate = approved returns / sold orders with products of the seller x 100, with 2 decimals (`HALF_UP`); `0.00` when there are no such orders.
* Read-only operation: it does not modify information; only the consultation is audited.

## Validations

* RG-01 and role `ADMINISTRATOR`, `SUPERVISOR` or `SELLER`.
* `period` is required.
* The seller must exist.
* A seller only consults its own report (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or report of another seller. |
| `InvalidReportPeriodException` | `period` is not provided. |
| `EntityNotFoundException` | The seller does not exist. |

## Output Ports Used

* `SellerRepositoryPort`
* `ProductRepositoryPort`
* `OrderRepositoryPort`
* `ReturnRequestRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateSellerOwnershipService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.REPORT_CONSULTED`

---

# 3. ConsultInventoryReportService

## Description

Consolidates the current stock of every warehouse and lists the physical products with low stock.

## Implementation

* Class: `nexusmarket.domain.services.reporting.ConsultInventoryReportService` (`@Service`, `@RequiredArgsConstructor`).

```java
InventoryReport consultInventoryReport(User requestingUser, int lowStockThreshold);
```

## Authorized Roles

`ADMINISTRATOR`, `SUPERVISOR` and `LOGISTICS_OPERATOR`.

Exposed in: `AdministratorPort`, `LogisticsOperatorPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `lowStockThreshold` | `int` | Products whose available stock is strictly below this value are listed (> 0). |

**Output:** `InventoryReport`. Returns the inventory report.

## Business Rules

* It is a snapshot of the current stock; it has no period.
* One `WarehouseStockSummary` per warehouse (`WarehouseRepositoryPort.findAll`), also for warehouses without inventory records.
* Per warehouse: total units = available (`quantity`) + reserved (`reservedQuantity`) of its records; reserved units = sum of `reservedQuantity`; damaged units = available + reserved of the records in condition `DAMAGED`.
* Low stock: for every product with at least one inventory record, the available stock is summed over all warehouses excluding `DAMAGED` records; the product is listed when that sum is strictly below `lowStockThreshold`. The list is ordered by available units, lowest first.
* Read-only operation: it does not modify information; only the consultation is audited.

## Validations

* RG-01 and role `ADMINISTRATOR`, `SUPERVISOR` or `LOGISTICS_OPERATOR`.
* `lowStockThreshold` must be greater than 0.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed. |
| `InvalidReportParameterException` | `lowStockThreshold` is 0 or negative. |

## Output Ports Used

* `WarehouseRepositoryPort`
* `InventoryRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.REPORT_CONSULTED`

---

# 4. ConsultLogisticsReportService

## Description

Consolidates the shipments created in a period per status, the shipments pending dispatch and the delivered ones.

## Implementation

* Class: `nexusmarket.domain.services.reporting.ConsultLogisticsReportService` (`@Service`, `@RequiredArgsConstructor`).

```java
LogisticsReport consultLogisticsReport(User requestingUser, ReportPeriod period);
```

## Authorized Roles

`ADMINISTRATOR` and `SUPERVISOR`.

Exposed in: `AdministratorPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (loaded from the token by the JWT filter). |
| `period` | `ReportPeriod` | Inclusive range of days of the report. |

**Output:** `LogisticsReport`. Returns the logistics report of the period.

## Business Rules

* Shipments: those whose `createdAt` falls in the period, counted per `ShipmentStatus` (every status is reported, 0 when there are none).
* Pending dispatch = `PREPARING` + `PACKED`; delivered = `DELIVERED` (computed by `LogisticsReport`).
* Read-only operation: it does not modify information; only the consultation is audited.

## Validations

* RG-01 and role `ADMINISTRATOR` or `SUPERVISOR`.
* `period` is required.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed. |
| `InvalidReportPeriodException` | `period` is not provided. |

## Output Ports Used

* `ShipmentRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.REPORT_CONSULTED`

---
