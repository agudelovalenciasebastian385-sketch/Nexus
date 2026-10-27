# Invoice Management Services

## Introduction

This document specifies the services of the **Invoice Management** subdomain of NexusMarket.

Generation of the unique invoice of an order once its payment is validated, and invoice consultation.

All services live in the package `nexusmarket.domain.services.invoice`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Exposed in (role ports) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `GenerateInvoiceService` | — (internal) | Internal (executed within `ValidatePaymentService`). |
| 2 | `ConsultInvoiceService` | `AdministratorPort`, `BuyerPort`, `SupervisorPort` | `BUYER` (own orders); `ADMINISTRATOR` and `SUPERVISOR`. |

---

# 1. GenerateInvoiceService

## Description

Generates the unique invoice of a `PAID` order. Invoked by `ValidatePaymentService`; it is not exposed in any role port.

## Implementation

* Class: `nexusmarket.domain.services.invoice.GenerateInvoiceService` (`@Service`, `@RequiredArgsConstructor`).
* Internal service: it is not exposed in any role port.

```java
Invoice generateInvoice(Order order, User performedBy);
```

## Authorized Roles

Internal (executed within `ValidatePaymentService`).

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `order` | `Order` | Paid order. |
| `performedBy` | `User` | User who triggered the payment validation. |

**Output:** `Invoice`. Returns the generated invoice, already attached to the order.

## Business Rules

* An order generates exactly one invoice.
* The total is computed from the order items (`Order.totalAmount()`).
* The invoice is attached to the order (`Order.attachInvoice`); the caller persists the order.

## Validations

* The order must be `PAID`.
* The order must not have an invoice yet.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `InvalidOrderException` | The order is not `PAID` or is already invoiced. |

## Output Ports Used

* `InvoiceRepositoryPort`

## Collaborating Services

* `RegisterAuditLogService`

## Audit

* `AuditAction.INVOICE_GENERATED`

---

# 2. ConsultInvoiceService

## Description

Retrieves the invoice of an order.

## Implementation

* Class: `nexusmarket.domain.services.invoice.ConsultInvoiceService` (`@Service`, `@RequiredArgsConstructor`).

```java
Invoice consultInvoice(User requestingUser, String orderId);
```

## Authorized Roles

`BUYER` (own orders); `ADMINISTRATOR` and `SUPERVISOR`.

Exposed in: `AdministratorPort`, `BuyerPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `orderId` | `String` | Identifier of the invoiced order. |

**Output:** `Invoice`. Returns the invoice.

## Business Rules

* Read-only operation.

## Validations

* RG-01 and allowed role.
* A buyer only consults invoices of its own orders (RG-03).

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Role not allowed or order of another buyer. |
| `EntityNotFoundException` | The order or its invoice does not exist. |

## Output Ports Used

* `OrderRepositoryPort`
* `InvoiceRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `ValidateBuyerOwnershipService`

## Audit

Not audited (read-only or authentication check).

---
