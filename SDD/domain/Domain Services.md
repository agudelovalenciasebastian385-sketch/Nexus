# Domain Services — NexusMarket

## Introduction

This document provides the high-level catalog of the domain services of **NexusMarket**. Each service implements exactly one business capability and lives in `nexusmarket.domain.services.<subdomain>`.

The detailed specification of every service (authorized roles, input, output, business rules, validations, exceptions and output ports) is maintained in one document per subdomain inside [`services/`](services/).

## Service Design Principles

* **One service per use case.** Each service is a concrete class `<Name>Service` annotated with `@Service` and `@RequiredArgsConstructor`; it implements no interface. Its operations are exposed through the role ports of `nexusmarket.domain.ports.in`, whose use case adapters inject the service class directly. Internal services (authorization, audit registration and loading of the authenticated user) are not exposed in any role port.
* **Requesting user first.** Every protected operation receives the authenticated `User` (loaded from the token by the JWT filter) as its first parameter.
* **Authorization first.** Services validate RG-01 (authenticated and `ACTIVE` user), RG-02 (single role) and RG-03 (no information outside the role) through the authorization services before doing anything else.
* **Output ports only.** Services read and store information exclusively through the output ports of `nexusmarket.domain.ports.out`; they never depend on databases, JPA, MongoDB, HTTP, JWT or hashing libraries.
* **Rules in the entities.** Services orchestrate; invariants and state transitions are enforced by the domain entities, which raise domain exceptions.
* **Identifiers and data.** Existing aggregates are referenced by their identifier (`String`); new information is received as primitives and Value Objects, because the entities are always valid and cannot be built partially. New identifiers are generated with `UUID.randomUUID()`.
* **Audit.** Every operation that changes information registers an immutable `AuditLog` through `RegisterAuditLogService`. Read-only operations are not audited.
* **Errors.** Every failure is a subclass of `DomainException` (`nexusmarket.domain.exceptions`).

## General Restrictions

| Code | Restriction | Enforced by |
| --- | --- | --- |
| RG-01 | Every operation must be executed by an authenticated user. | `ValidateUserStatusService` |
| RG-02 | Each user has a single role in the system. | `User.role` (fixed by subclass) and `ValidateRoleAuthorizationService` |
| RG-03 | No participant can manage information outside its role. | `ValidateRoleAuthorizationService`, `ValidateSellerOwnershipService`, `ValidateBuyerOwnershipService` |

---

# User Management

Registration of buyers and staff users, authentication and logout, loading of the authenticated user, user consultation, user status and password management.

Package: `nexusmarket.domain.services.user` — detail: [services/user-services.md](services/user-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `RegisterBuyerUserService` | `PublicAccessPort` | Registers a new buyer who self-registers in the Marketplace, together with its unique shopping cart. |
| `RegisterStaffUserService` | `AdministratorPort` | Registers a staff user: a logistics operator or a supervisor. |
| `LoginService` | `PublicAccessPort` | Authenticates a user with its email and password and returns the authenticated user together with its token. |
| `LogoutService` | `PublicAccessPort` | Logs out the authenticated user by invalidating every token previously issued to it. |
| `LoadAuthenticatedUserService` | — (internal) | Loads the stored user identified by the token so that the JWT filter can validate its token version and status. |
| `ConsultUserService` | `AdministratorPort`, `BuyerPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` | Retrieves the information of a user. |
| `ChangeUserStatusService` | `AdministratorPort` | Changes the operational status of a user (`ACTIVE`, `BLOCKED`, `INACTIVE`). |
| `ChangeUserPasswordService` | `AdministratorPort`, `BuyerPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` | Changes the password of the requesting user. |

---

# Seller Management

Administrative onboarding of sellers together with their first warehouse, and maintenance of seller information.

Package: `nexusmarket.domain.services.seller` — detail: [services/seller-services.md](services/seller-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `RegisterSellerService` | `AdministratorPort` | Onboards a seller together with its first warehouse of type `SELLER` in the same operation. |
| `ConsultSellerService` | `AdministratorPort`, `SellerPort`, `SupervisorPort` | Retrieves the information of a seller. |
| `UpdateSellerService` | `AdministratorPort`, `SellerPort` | Updates the full name of a seller. |

---

# Buyer Management

Consultation and maintenance of the buyer profile, delivery addresses and commercial status.

Package: `nexusmarket.domain.services.buyer` — detail: [services/buyer-services.md](services/buyer-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `ConsultBuyerService` | `AdministratorPort`, `BuyerPort`, `SupervisorPort` | Retrieves the information of a buyer. |
| `UpdateBuyerProfileService` | `BuyerPort` | Updates the profile of the requesting buyer: full name, primary address and additional addresses. |
| `ChangeBuyerCommercialStatusService` | `AdministratorPort` | Changes the commercial status of a buyer (`ENABLED`, `RESTRICTED`, `SUSPENDED`). |

---

# Warehouse Management

Registration, consultation and maintenance of Marketplace and Seller warehouses.

Package: `nexusmarket.domain.services.warehouse` — detail: [services/warehouse-services.md](services/warehouse-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `RegisterWarehouseService` | `AdministratorPort`, `SellerPort` | Registers a warehouse: the administrator registers Marketplace warehouses and a seller registers additional warehouses of its own. |
| `ConsultWarehouseService` | `AdministratorPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` | Retrieves one warehouse or the list of warehouses visible to the requesting user. |
| `UpdateWarehouseService` | `AdministratorPort`, `SellerPort` | Updates the name and location of a warehouse. |

---

# Catalog Management

Registration and maintenance of physical and digital products, their publication lifecycle and the public catalog.

Package: `nexusmarket.domain.services.product` — detail: [services/product-services.md](services/product-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `RegisterProductService` | `SellerPort` | Registers a physical or digital product of the requesting seller in `DRAFT` status. |
| `UpdateProductService` | `SellerPort` | Updates the commercial information of a product of the requesting seller. |
| `PublishProductService` | `SellerPort` | Makes a product visible in the public catalog (`DRAFT` or `SUSPENDED` → `PUBLISHED`). |
| `SuspendProductService` | `SellerPort` | Temporarily removes a product from the catalog (`PUBLISHED` → `SUSPENDED`). |
| `DiscontinueProductService` | `SellerPort` | Permanently withdraws a product from the catalog (any status except `DISCONTINUED` → `DISCONTINUED`). |
| `ConsultCatalogService` | `PublicAccessPort` | Retrieves the public catalog: only products in `PUBLISHED` status. |
| `ConsultSellerProductsService` | `AdministratorPort`, `SellerPort`, `SupervisorPort` | Retrieves all the products of a seller, in any status. |

---

# Inventory Management

Distributed stock per product and warehouse: initial stock, inbounds, adjustments, reservations and consultation.

Package: `nexusmarket.domain.services.inventory` — detail: [services/inventory-services.md](services/inventory-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `RegisterInitialStockService` | `LogisticsOperatorPort`, `SellerPort` | Creates the inventory record of a physical product in a warehouse with its initial stock. |
| `RegisterInventoryInboundService` | `LogisticsOperatorPort`, `SellerPort` | Adds stock to an existing inventory record (`INBOUND` movement). |
| `RegisterInventoryAdjustmentService` | `LogisticsOperatorPort` | Applies a manual correction of stock and/or changes the condition of an inventory record (e.g. marks it `DAMAGED`). |
| `ReserveInventoryService` | `LogisticsOperatorPort` | Holds stock of a physical product for an order in progress (`RESERVATION` movement: available → reserved). |
| `ReleaseInventoryReservationService` | `LogisticsOperatorPort` | Returns reserved stock to the available stock (`RESERVATION_RELEASE` movement). |
| `ConsultInventoryService` | `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` | Retrieves the inventory records of a product or of a warehouse. |

---

# Cart Management

The buyer's provisional selection of published products before confirming an order.

Package: `nexusmarket.domain.services.cart` — detail: [services/cart-services.md](services/cart-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `AddItemToCartService` | `BuyerPort` | Adds a published product to the requesting buyer's cart at its current price. |
| `RemoveItemFromCartService` | `BuyerPort` | Removes a product/variant line from the requesting buyer's cart. |
| `UpdateCartItemQuantityService` | `BuyerPort` | Replaces the quantity of a product/variant line of the requesting buyer's cart. |
| `ConsultCartService` | `BuyerPort`, `SupervisorPort` | Retrieves the cart of a buyer with its items and total. |
| `ClearCartService` | `BuyerPort` | Removes every item from the requesting buyer's cart. |

---

# Order Management

Order confirmation from the cart, payment validation and order consultation.

Package: `nexusmarket.domain.services.order` — detail: [services/order-services.md](services/order-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `ConfirmOrderService` | `BuyerPort` | Converts the requesting buyer's cart into an order in `PENDING_PAYMENT`, reserves the inventory of its physical items and empties the cart. |
| `ValidatePaymentService` | `BuyerPort` | Processes the payment of the buyer's own `PENDING_PAYMENT` order through the payment gateway. |
| `ConsultOrderService` | `BuyerPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` | Retrieves an order. |
| `ConsultBuyerOrdersService` | `BuyerPort`, `SupervisorPort` | Retrieves the historical and active orders of a buyer. |

---

# Invoice Management

Generation of the unique invoice of an order once its payment is validated, and invoice consultation.

Package: `nexusmarket.domain.services.invoice` — detail: [services/invoice-services.md](services/invoice-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `GenerateInvoiceService` | — (internal) | Generates the unique invoice of a `PAID` order. Invoked by `ValidatePaymentService`; it is not exposed in any role port. |
| `ConsultInvoiceService` | `AdministratorPort`, `BuyerPort`, `SupervisorPort` | Retrieves the invoice of an order. |

---

# Shipment Management

Logistics of physical products: shipment creation per origin warehouse, packing, dispatch and delivery confirmation.

Package: `nexusmarket.domain.services.shipment` — detail: [services/shipment-services.md](services/shipment-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `CreateShipmentsService` | `LogisticsOperatorPort` | Creates the shipments of a `PAID` order: one per origin warehouse, only for physical items. |
| `PackShipmentService` | `LogisticsOperatorPort` | Registers that a shipment has been packed (`PREPARING` → `PACKED`). |
| `DispatchShipmentService` | `LogisticsOperatorPort` | Dispatches a packed shipment (`PACKED` → `IN_TRANSIT`), converts its reservations into sale outbounds and moves the order to `SHIPPED`. |
| `ConfirmDeliveryService` | `LogisticsOperatorPort` | Confirms the delivery of a shipment (`IN_TRANSIT` → `DELIVERED`) and finalizes the order when all its shipments are delivered. |

---

# Returns and Refunds Management

Return requests over delivered orders, their approval or rejection, inventory re-entry and refund processing.

Package: `nexusmarket.domain.services.returns` — detail: [services/returns-services.md](services/returns-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `RequestReturnService` | `BuyerPort` | Registers a return request over a delivered order of the requesting buyer. |
| `ApproveReturnService` | `AdministratorPort` | Approves a return request, re-enters the inventory of its physical items and creates the pending refund. |
| `RejectReturnService` | `AdministratorPort` | Rejects a return request that does not meet the required conditions. |
| `ProcessRefundService` | `AdministratorPort` | Processes a pending refund: the amount is restituted to the buyer (`PENDING` → `PROCESSED`). |
| `ConsultReturnService` | `AdministratorPort`, `BuyerPort`, `SupervisorPort` | Retrieves a return request or the refund generated by it. |

---

# Authorization

Cross-cutting validations of the general restrictions RG-01, RG-02 and RG-03 and of ownership over business information.

Package: `nexusmarket.domain.services.authorization` — detail: [services/authorization-services.md](services/authorization-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `ValidateUserStatusService` | — (internal) | RG-01: validates that the operation is executed by an authenticated user that is currently `ACTIVE`. |
| `ValidateRoleAuthorizationService` | — (internal) | RG-02 and RG-03: validates RG-01 and that the single role of the user is one of the roles allowed for the operation. |
| `ValidateSellerOwnershipService` | — (internal) | RG-03 for sellers: validates that a seller only manages its own information. |
| `ValidateBuyerOwnershipService` | — (internal) | RG-03 for buyers: validates that a buyer only accesses its own information. |

---

# Audit

Immutable registration of every significant business operation and consultation of the audit trail.

Package: `nexusmarket.domain.services.audit` — detail: [services/audit-services.md](services/audit-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `RegisterAuditLogService` | — (internal) | Registers an immutable audit record of a significant business operation. |
| `ConsultAuditLogsService` | `AdministratorPort`, `SupervisorPort` | Retrieves audit records: all of them, those performed by a user, or those of an entity. |

---

# Reporting

Consolidation of administrative information (OBJ-12): commercial, seller performance, inventory and logistics reports. Reports are read-only, computed when consulted and never persisted.

Package: `nexusmarket.domain.services.reporting` — detail: [services/reporting-services.md](services/reporting-services.md)

| Service | Exposed in | Purpose |
| --- | --- | --- |
| `ConsultCommercialReportService` | `AdministratorPort`, `SupervisorPort` | Consolidates the commercial figures of a period: orders created per status, total invoiced, total refunded and net revenue. |
| `ConsultSellerPerformanceReportService` | `AdministratorPort`, `SellerPort`, `SupervisorPort` | Consolidates the performance of a seller in a period: products per status, units and amount sold, approved returns and return rate. |
| `ConsultInventoryReportService` | `AdministratorPort`, `LogisticsOperatorPort`, `SupervisorPort` | Consolidates the current stock of every warehouse and lists the physical products with low stock. |
| `ConsultLogisticsReportService` | `AdministratorPort`, `SupervisorPort` | Consolidates the shipments created in a period per status, the shipments pending dispatch and the delivered ones. |

---

# Service Count

* Domain services: **60**.
* Services exposed in role ports: **53**.
* Internal services: **7** (`LoadAuthenticatedUserService`, `GenerateInvoiceService`, `ValidateUserStatusService`, `ValidateRoleAuthorizationService`, `ValidateSellerOwnershipService`, `ValidateBuyerOwnershipService`, `RegisterAuditLogService`).
