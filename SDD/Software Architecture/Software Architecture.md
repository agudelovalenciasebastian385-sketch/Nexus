# Software Architecture — NexusMarket

## Overview

NexusMarket follows a **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)** principles.

The primary objective of this architecture is to isolate the Marketplace business domain from external technologies, ensuring that business rules remain independent from frameworks, databases, communication protocols, and infrastructure concerns.

This approach promotes maintainability, scalability, testability, and technology independence — which is especially relevant given that the functional specification explicitly excludes graphical interfaces, technical authentication mechanisms, and implementation technologies, leaving those decisions to the adapter and infrastructure layers.

## Implementation Status

| Component | Status |
| --- | --- |
| Domain models, value objects and domain exceptions | Implemented |
| Domain services (54, organized in 13 subdomains) | Implemented |
| Input ports: 49 use case interfaces and 6 role ports | Implemented |
| Output ports (16 interfaces) | Implemented (interfaces only) |
| Use case adapters (`adapters/useCases`, 6 classes) | Implemented |
| REST input adapters (controllers, DTOs, mappers) | **Next phase** |
| Persistence output adapters (MySQL / JPA, MongoDB) | **Next phase** |
| Password hashing, JWT and payment gateway adapters | **Next phase** |
| Security configuration (authentication filters, token validation) | **Next phase** |

Until the output adapters exist, the application compiles but the Spring context cannot start, because no bean implements the output ports.

---

# Architectural Principles

The architecture is based on the following principles:

- Domain-first design.
- Separation of concerns.
- Dependency inversion.
- Technology independence.
- High cohesion.
- Low coupling.
- Explicit boundaries between layers.

The domain contains all business rules and never depends on external technologies.

---

# Architecture Layers

The application is organized into the following components:

```text
nexusmarket
│
├── App.java          (entry point)
│
├── adapters          (use case adapters; REST and output adapters in the next phase)
│
└── domain            (models, value objects, exceptions, services, ports)
```

An `infrastructure` component (technical configuration) is planned for the next phase; it does not exist yet.

---

# Package Structure

## Current Structure

```text
application/src/main/java/
└── nexusmarket/
    │
    ├── App.java
    │
    ├── adapters/
    │   └── useCases/                 6 role port implementations
    │
    └── domain/
        ├── models/                   entities (21 classes)
        ├── valueobjects/             catalogs (enums implementing DomainCatalog), Address, Variant
        ├── exceptions/               DomainException and its subclasses (25 classes)
        ├── services/
        │   ├── user/
        │   ├── seller/
        │   ├── buyer/
        │   ├── warehouse/
        │   ├── product/
        │   ├── inventory/
        │   ├── cart/
        │   ├── order/
        │   ├── invoice/
        │   ├── shipment/
        │   ├── returns/
        │   ├── authorization/
        │   └── audit/
        └── ports/
            ├── in/                   6 role ports
            └── out/                  16 output ports
```

Configuration lives in `application/src/main/resources/application.yml` (MySQL datasource, MongoDB connection, JWT properties). The root `docker-compose.yml` starts MySQL 8 (port 3306, database `nexusmarket`) and MongoDB 6 (port 27017).

## Planned Structure (Next Phase)

```text
nexusmarket/
├── adapters/
│   ├── in/
│   │   └── rest/
│   │       ├── controllers/
│   │       ├── requests/
│   │       ├── responses/
│   │       └── mappers/
│   └── out/
│       ├── persistence/
│       │   ├── mysql/                entities, repositories, mappers, adapters
│       │   └── mongodb/              audit log documents, repositories, mappers, adapters
│       ├── security/                 PasswordServicePort and JwtServicePort implementations
│       └── payment/                  simulated PaymentGatewayPort implementation
│
└── infrastructure/
    ├── config/
    ├── database/
    └── security/
```

---

# Layer Responsibilities

## App.java

`App.java` is the application's entry point (`@SpringBootApplication`).

### Responsibilities

- Initialize the application.
- Configure dependency injection (component scanning of services and adapters).
- Start the web server (REST controllers will be added in the next phase).

---

# Domain

The Domain layer is the core of the application. It contains all business rules described in the NexusMarket Business Functional Specification and must remain independent from any external technology.

No class inside the domain may depend on:

- JPA / ORM
- HTTP
- REST
- JSON
- SQL
- MongoDB
- JWT or password hashing libraries

The only framework elements used in the domain are the Spring `@Service` stereotype and Lombok `@RequiredArgsConstructor` on domain services, used for dependency injection. Entities and value objects do not use any framework.

---

## Models

Package `nexusmarket.domain.models`. Contains the business entities (see [Domain Model](../domain/Domain%20Model.md)):

- `User` (abstract), `Buyer`, `Seller`, `LogisticsOperator`, `Administrator`, `Supervisor`
- `Warehouse`
- `Product` (abstract), `PhysicalProduct`, `DigitalProduct`
- `Inventory`, `InventoryMovement`
- `Cart`, `CartItem`
- `Order`, `OrderItem`
- `Invoice`
- `Shipment`
- `ReturnRequest`, `Refund`
- `AuditLog`

---

## Value Objects

Package `nexusmarket.domain.valueobjects` (see [Domain Value Objects](../domain/Domain%20Value%20Objects.md)).

- Business catalogs: Java enums implementing the `DomainCatalog` interface (`code`, `name`, `description`, `fromCode`):
  `SystemRole`, `UserStatus`, `CommercialStatus`, `WarehouseType`, `ProductType`, `ProductStatus`, `InventoryCondition`, `MovementType`, `OrderStatus`, `ShipmentStatus`, `ReturnStatus`, `RefundStatus`, `PaymentResult`, `AuditAction`, `AuditEntityType`.
- Compound value objects: `Address`, `Variant`.

Value objects are immutable and compared by value. There is no separate `enums` package: every bounded value of the domain is a catalog value object.

---

## Services

Package `nexusmarket.domain.services.<subdomain>`. Each service implements exactly one business capability (see [Domain Services](../domain/Domain%20Services.md)).

- Class `<Name>Service`, annotated with `@Service` and `@RequiredArgsConstructor`.
- Concrete class that implements no interface: the use case adapters inject it directly and expose its operations through the role ports. The 7 internal services (`LoadAuthenticatedUserService`, `GenerateInvoiceService`, `ValidateUserStatusService`, `ValidateRoleAuthorizationService`, `ValidateSellerOwnershipService`, `ValidateBuyerOwnershipService`, `RegisterAuditLogService`) are not exposed in any role port.
- Receives the requesting `User` as first parameter, validates RG-01/RG-02/RG-03 through the authorization services, reads and stores exclusively through output ports, delegates rules to the entities and registers audit records.

| Subdomain | Services |
| --- | --- |
| `user` | `RegisterBuyerUserService`, `RegisterStaffUserService`, `LoginService`, `LogoutService`, `LoadAuthenticatedUserService`, `ConsultUserService`, `ChangeUserStatusService`, `ChangeUserPasswordService` |
| `seller` | `RegisterSellerService`, `ConsultSellerService`, `UpdateSellerService` |
| `buyer` | `ConsultBuyerService`, `UpdateBuyerProfileService`, `ChangeBuyerCommercialStatusService` |
| `warehouse` | `RegisterWarehouseService`, `ConsultWarehouseService`, `UpdateWarehouseService` |
| `product` | `RegisterProductService`, `UpdateProductService`, `PublishProductService`, `SuspendProductService`, `DiscontinueProductService`, `ConsultCatalogService`, `ConsultSellerProductsService` |
| `inventory` | `RegisterInitialStockService`, `RegisterInventoryInboundService`, `RegisterInventoryAdjustmentService`, `ReserveInventoryService`, `ReleaseInventoryReservationService`, `ConsultInventoryService` |
| `cart` | `AddItemToCartService`, `RemoveItemFromCartService`, `UpdateCartItemQuantityService`, `ConsultCartService`, `ClearCartService` |
| `order` | `ConfirmOrderService`, `ValidatePaymentService`, `ConsultOrderService`, `ConsultBuyerOrdersService` |
| `invoice` | `GenerateInvoiceService`, `ConsultInvoiceService` |
| `shipment` | `CreateShipmentsService`, `PackShipmentService`, `DispatchShipmentService`, `ConfirmDeliveryService` |
| `returns` | `RequestReturnService`, `ApproveReturnService`, `RejectReturnService`, `ProcessRefundService`, `ConsultReturnService` |
| `authorization` | `ValidateUserStatusService`, `ValidateRoleAuthorizationService`, `ValidateSellerOwnershipService`, `ValidateBuyerOwnershipService` |
| `audit` | `RegisterAuditLogService`, `ConsultAuditLogsService` |

Total: **56 services** (49 exposed in role ports, 7 internal).

---

## Ports

Ports define communication contracts between the domain and external technologies. The domain owns all interfaces.

### Input Ports

Package `nexusmarket.domain.ports.in` (see [Input Ports](../domain/Input-ports.md)). Input ports are organized strictly by role: one role port per system role plus a public port, each one with the operations that role is allowed to execute. There are no per-service interfaces.

| Role port | Role | Operations |
| --- | --- | --- |
| `PublicAccessPort` | public (no specific role) | 5 |
| `BuyerPort` | `BUYER` | 17 |
| `SellerPort` | `SELLER` | 19 |
| `LogisticsOperatorPort` | `LOGISTICS_OPERATOR` | 16 |
| `AdministratorPort` | `ADMINISTRATOR` | 23 |
| `SupervisorPort` | `SUPERVISOR` (read-only) | 18 |

### Output Ports

Package `nexusmarket.domain.ports.out` (see [Output Ports](../domain/Output-ports.md)). Interfaces only in this phase:

| Output port | Responsibility | Planned adapter (next phase) |
| --- | --- | --- |
| `UserRepositoryPort` | Users of any role; email and identification uniqueness | MySQL |
| `BuyerRepositoryPort` | Buyers | MySQL |
| `SellerRepositoryPort` | Sellers | MySQL |
| `WarehouseRepositoryPort` | Warehouses | MySQL |
| `ProductRepositoryPort` | Physical and digital products | MySQL |
| `InventoryRepositoryPort` | Inventory records and movements | MySQL |
| `CartRepositoryPort` | Carts and cart items | MySQL |
| `OrderRepositoryPort` | Orders and order items | MySQL |
| `InvoiceRepositoryPort` | Invoices | MySQL |
| `ShipmentRepositoryPort` | Shipments | MySQL |
| `ReturnRequestRepositoryPort` | Return requests | MySQL |
| `RefundRepositoryPort` | Refunds | MySQL |
| `AuditLogRepositoryPort` | Append-only audit records | MongoDB |
| `PasswordServicePort` | Password encryption and verification | Hashing library |
| `JwtServicePort` | Token generation and validation, extraction of the user id and token version, and `User` reconstruction | JWT library |
| `PaymentGatewayPort` | Order payment processing (`PaymentResult`) | Simulated gateway |

---

## Exceptions

Package `nexusmarket.domain.exceptions`. Every business error extends `DomainException` (unchecked). The complete list and when each one is raised is documented in [Domain Model — Domain Exceptions](../domain/Domain%20Model.md#domain-exceptions). Examples:

- `InsufficientInventoryException`
- `OrderAlreadyFinalizedException`
- `UserAlreadyExistsException`
- `UnauthorizedOperationException`
- `PaymentRejectedException`

Business exceptions belong exclusively to the domain.

---

# Adapters

Adapters connect external technologies with the NexusMarket business domain. The domain never communicates directly with external systems.

## Use Case Adapters (implemented)

Package `nexusmarket.adapters.useCases` (see [Use Cases Adapters](../Adapters/Use-cases-adapters.md)). Each class implements one role port and delegates every call to the domain services; they contain no business rules.

| Role port | Use case adapter |
| --- | --- |
| `PublicAccessPort` | `PublicAccessUseCaseImpl` |
| `BuyerPort` | `BuyerUseCaseImpl` |
| `SellerPort` | `SellerUseCaseImpl` |
| `LogisticsOperatorPort` | `LogisticsOperatorUseCaseImpl` |
| `AdministratorPort` | `AdministratorUseCaseImpl` |
| `SupervisorPort` | `SupervisorUseCaseImpl` |

## Input Adapters — REST (next phase)

Will expose the role ports to external clients (buyers, sellers, logistics operators, administrators and supervisors).

- **Controllers:** receive HTTP requests, obtain the `User` reconstructed from the token and call the corresponding role port. They must never implement business rules.
- **Requests / Responses:** DTOs for incoming and outgoing payloads; they never enter the domain.
- **Mappers:** Request DTO ↔ domain data, domain model ↔ Response DTO.

## Output Adapters (next phase)

Will implement the output ports:

- **MySQL (Spring Data JPA):** persistence entities, repositories, mappers and adapters for every repository port except the audit log. A single relational store keeps the transactional data.
- **MongoDB (Spring Data MongoDB):** documents, repository, mapper and adapter for `AuditLogRepositoryPort`.
- **Security:** implementations of `PasswordServicePort` and `JwtServicePort`.
- **Payment:** simulated implementation of `PaymentGatewayPort` returning `PaymentResult.APPROVED` or `PaymentResult.REJECTED`.

Persistence adapters are also responsible for transactional boundaries: domain services perform several port calls per operation and do not declare transactions themselves.

---

# Infrastructure (next phase)

Will contain technical configuration without business logic:

- **Config:** REST configuration, serialization, environment configuration.
- **Database:** connection pool and schema configuration (today `spring.jpa.hibernate.ddl-auto: update` in `application.yml`).
- **Security:** authentication filters and token validation, aligned with RG-01 (every operation must be executed by an authenticated user). The JWT filter loads the user with `LoadAuthenticatedUserService` and rejects tokens whose version differs from `User.authTokenVersion` (logout, password change or status change).

---

# Dependency Flow

Dependencies always point toward the domain.

```text
REST Controller (next phase)
        │
        ▼
Role Port (domain/ports/in)  ◄── implemented by ── Use Case Adapter (adapters/useCases)
        │                                                   │
        │                                                   ▼ delegates to
        │                                   Domain Service (domain/services)
        │                                                   │
        │                                                   ▼
        └────────────────────────────────── Output Port (domain/ports/out)
                                                            ▲
                                                            │ implemented by
                                            Output Adapter (next phase: MySQL, MongoDB, security, payment)
```

The domain never depends on adapters or infrastructure.

---

# Benefits

This architecture provides:

- Technology independence.
- High maintainability.
- Clear separation of concerns.
- Improved testability.
- Easier scalability.
- Better support for Domain-Driven Design.
- Easy replacement of frameworks or the database engine.
- Reusable business logic.
- Long-term maintainability.

---

# Architectural Constraints

The following rules must always be respected:

1. Business logic belongs exclusively to the Domain layer.
2. Controllers must not contain business rules.
3. DTOs must never enter the Domain layer.
4. Persistence entities must never be exposed through the API.
5. Communication between technologies and the Domain must occur only through Ports.
6. Adapters implement Ports but never define business rules.
7. Infrastructure depends on the Domain, never the opposite.
8. Every dependency must point toward the Domain.
9. Business entities must remain framework-independent.
10. The Domain must be fully testable without requiring infrastructure components, including without a running MySQL or MongoDB instance.
11. Each participant (Buyer, Seller, Logistics Operator, Administrator, Supervisor) may only interact with the information corresponding to their role (RG-03): the role ports expose only the allowed operations and every service validates the role again through `ValidateRoleAuthorizationService`.
