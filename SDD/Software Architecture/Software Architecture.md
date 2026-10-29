# Software Architecture — NexusMarket

## Overview

NexusMarket follows a **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)** principles.

The primary objective of this architecture is to isolate the Marketplace business domain from external technologies, ensuring that business rules remain independent from frameworks, databases, communication protocols, and infrastructure concerns.

This approach promotes maintainability, scalability, testability, and technology independence — which is especially relevant given that the functional specification explicitly excludes graphical interfaces, technical authentication mechanisms, and implementation technologies, leaving those decisions to the adapter and infrastructure layers.

## Implementation Status

| Component | Status |
| --- | --- |
| Domain models, value objects and domain exceptions | Implemented |
| Domain services (60, organized in 14 subdomains) | Implemented |
| Input ports: 6 role ports | Implemented |
| Output ports (16 interfaces) | Implemented |
| Use case adapters (`adapters/useCases`, 6 classes) | Implemented |
| Persistence output adapters (MySQL / JPA: 12 ports; MongoDB: audit log) | Implemented |
| Password hashing (BCrypt), JWT (JJWT) and simulated payment gateway adapters | Implemented |
| Initial Administrator seeder (`infrastructure/seed`) | Implemented |
| JWT authentication filter and `SecurityConfig` | Implemented |
| REST input adapters: `AuthRestController`, `PublicRestController`, `GlobalExceptionHandler` | Implemented (step 4A) |
| REST input adapters: `BuyerRestController`, `SellerRestController` | Implemented (step 4B) |
| REST input adapters: `LogisticsOperatorRestController`, `AdministratorRestController`, `SupervisorRestController` | Implemented (step 4C) |

Every output port has an implementation, so the Spring context starts against the MySQL and MongoDB containers of `docker-compose.yml`. The REST API is specified in [Api-rest-endpoints.md](../Adapters/Api-rest-endpoints.md).

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
├── adapters          (REST input adapters, web security, use case adapters and output adapters)
│
├── domain            (models, value objects, exceptions, services, ports)
│
└── infrastructure    (technical startup tasks: initial data seed)
```

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
    │   ├── rest/
    │   │   ├── controllers/          one controller per role port + AuthRestController
    │   │   ├── dtos/                 requests/ (Jakarta validation), responses/ (ErrorResponseDTO, ...)
    │   │   ├── mappers/              request DTO -> domain values, Domain Model -> response DTO
    │   │   └── exception/            GlobalExceptionHandler
    │   ├── useCases/                 6 role port implementations
    │   ├── persistence/
    │   │   ├── jpa/                  MySQL: entities, mappers, repositories, JpaAggregateLoader, 12 *JpaAdapter
    │   │   └── mongodb/              MongoDB: AuditLogDocument, mapper, repository, AuditLogMongoAdapter
    │   ├── security/                 SecurityConfig, JwtAuthenticationFilter, JwtProvider, BCryptPasswordServiceAdapter, dtos/, mappers/
    │   └── payment/                  SimulatedPaymentGatewayAdapter
    │
    ├── infrastructure/
    │   └── seed/                     DatabaseSeeder (initial Administrator)
    │
    └── domain/
        ├── models/                   entities, authentication result and read-only reports (28 classes)
        ├── valueobjects/             catalogs (enums implementing DomainCatalog), Address, Variant, ReportPeriod
        ├── exceptions/               DomainException and its subclasses (27 classes)
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
        │   ├── audit/
        │   └── reporting/
        └── ports/
            ├── in/                   6 role ports
            └── out/                  16 output ports
```

## Configuration and Local Environment

Configuration lives in `application/src/main/resources/application.yml`:

| Prefix | Content |
| --- | --- |
| `spring.datasource` | MySQL `jdbc:mysql://localhost:3306/nexusmarket?createDatabaseIfNotExist=true&...`, user `root` / `root` (overridable with `MYSQL_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD`). |
| `spring.jpa` | `ddl-auto: update` (Hibernate creates and updates the tables), `open-in-view: false`. |
| `spring.mongodb.uri` | `mongodb://localhost:27017/nexusmarket` (Spring Boot 4 prefix; overridable with `MONGODB_URI`). |
| `app.security.jwt` | JWT secret and expiration (see [Security Adapters](../Adapters/Security-adapters.md)). |
| `app.security.cors` | Allowed CORS origins (see [REST Security and CORS](../Adapters/Rest-security-cors.md)). |
| `spring.web` | `locale: en` with a fixed resolver: API and validation messages in English. |
| `app.payment.simulated` | Approval limit of the simulated payment gateway (see [Persistence Adapters](../Adapters/Persistence-adapters.md#81-paymentgatewayport--simulatedpaymentgatewayadapter)). |
| `app.seed.admin` | Credentials of the initial Administrator. |

The root `docker-compose.yml` starts:

| Service | Image | Port | Purpose |
| --- | --- | --- | --- |
| `mysql-db` | `mysql:8.0` | 3306 | Business data (database `nexusmarket`, `root` / `root`). |
| `mongo-db` | `mongo:6.0` | 27017 | Audit trail (database `nexusmarket`, collection `audit_logs`). |
| `phpmyadmin` | `phpmyadmin:5.2` | 8081 | **Development tool:** web UI to browse MySQL (`http://localhost:8081`, `root` / `root`). |
| `mongo-express` | `mongo-express:1.0.2` | 8082 | **Development tool:** web UI to browse MongoDB (`http://localhost:8082`, `admin` / `admin`). |

phpMyAdmin and Mongo Express are development tools only; the application does not depend on them.

### Test profile

Integration tests (`application/src/test`) run with `@ActiveProfiles("test")`, which loads `src/test/resources/application-test.yml`:

| Setting | Value |
| --- | --- |
| MySQL | same container, database `nexusmarket_test` (`createDatabaseIfNotExist=true`) |
| MongoDB | same container, database `nexusmarket_test` |
| `app.payment.simulated.approval-limit` | `1000.00`, so a test can trigger a rejected payment |

Test data (users and products with the `itest-` prefix) therefore never appears in the development databases or in their reports. `TransactionalConsistencyTest` verifies the transaction boundary, the after-commit audit and the optimistic locking of inventory against these databases (`./mvnw test` with the containers running).

---

# Layer Responsibilities

## App.java

`App.java` is the application's entry point (`@SpringBootApplication`).

### Responsibilities

- Initialize the application.
- Configure dependency injection (component scanning of services and adapters).
- Start the web server (REST controllers of `adapters/rest`).

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
- `AuthenticationResult` (outcome of a login; not persisted)
- Read-only reports, computed when consulted and never persisted: `CommercialReport`, `SellerPerformanceReport`, `InventoryReport` (with the detail rows `WarehouseStockSummary` and `LowStockProduct`), `LogisticsReport`

---

## Value Objects

Package `nexusmarket.domain.valueobjects` (see [Domain Value Objects](../domain/Domain%20Value%20Objects.md)).

- Business catalogs: Java enums implementing the `DomainCatalog` interface (`code`, `name`, `description`, `fromCode`):
  `SystemRole`, `UserStatus`, `CommercialStatus`, `WarehouseType`, `ProductType`, `ProductStatus`, `InventoryCondition`, `MovementType`, `OrderStatus`, `ShipmentStatus`, `ReturnStatus`, `RefundStatus`, `PaymentResult`, `AuditAction`, `AuditEntityType`.
- Compound value objects: `Address`, `Variant`, `ReportPeriod` (inclusive range of days of a report).

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
| `reporting` | `ConsultCommercialReportService`, `ConsultSellerPerformanceReportService`, `ConsultInventoryReportService`, `ConsultLogisticsReportService` |

Total: **60 services** in 14 subdomains (53 exposed in role ports, 7 internal).

---

## Ports

Ports define communication contracts between the domain and external technologies. The domain owns all interfaces.

### Input Ports

Package `nexusmarket.domain.ports.in` (see [Input Ports](../domain/Input-ports.md)). Input ports are organized strictly by role: one role port per system role plus a public port, each one with the operations that role is allowed to execute. There are no per-service interfaces.

| Role port | Role | Operations |
| --- | --- | --- |
| `PublicAccessPort` | public (no specific role) | 5 |
| `BuyerPort` | `BUYER` | 17 |
| `SellerPort` | `SELLER` | 20 |
| `LogisticsOperatorPort` | `LOGISTICS_OPERATOR` | 17 |
| `AdministratorPort` | `ADMINISTRATOR` | 27 |
| `SupervisorPort` | `SUPERVISOR` (read-only) | 22 |

### Output Ports

Package `nexusmarket.domain.ports.out` (see [Output Ports](../domain/Output-ports.md)):

| Output port | Responsibility | Adapter |
| --- | --- | --- |
| `UserRepositoryPort` | Users of any role; email and identification uniqueness | `UserJpaAdapter` (MySQL) |
| `BuyerRepositoryPort` | Buyers | `BuyerJpaAdapter` (MySQL) |
| `SellerRepositoryPort` | Sellers | `SellerJpaAdapter` (MySQL) |
| `WarehouseRepositoryPort` | Warehouses | `WarehouseJpaAdapter` (MySQL) |
| `ProductRepositoryPort` | Physical and digital products | `ProductJpaAdapter` (MySQL) |
| `InventoryRepositoryPort` | Inventory records and movements (also all records, for the inventory report) | `InventoryJpaAdapter` (MySQL) |
| `CartRepositoryPort` | Carts and cart items | `CartJpaAdapter` (MySQL) |
| `OrderRepositoryPort` | Orders and order items; orders created in a date range | `OrderJpaAdapter` (MySQL) |
| `InvoiceRepositoryPort` | Invoices; invoices issued in a date range | `InvoiceJpaAdapter` (MySQL) |
| `ShipmentRepositoryPort` | Shipments; shipments created in a date range | `ShipmentJpaAdapter` (MySQL) |
| `ReturnRequestRepositoryPort` | Return requests | `ReturnRequestJpaAdapter` (MySQL) |
| `RefundRepositoryPort` | Refunds; refunds processed in a date range | `RefundJpaAdapter` (MySQL) |
| `AuditLogRepositoryPort` | Append-only audit records | `AuditLogMongoAdapter` (MongoDB) |
| `PasswordServicePort` | Password encryption and verification | `BCryptPasswordServiceAdapter` (BCrypt) |
| `JwtServicePort` | Token generation and validation, extraction of the user id and token version | `JwtProvider` (JJWT, HS256) |
| `PaymentGatewayPort` | Order payment processing (`PaymentResult`) | `SimulatedPaymentGatewayAdapter` |

Date-range methods (`findBy<Date>Between(LocalDateTime from, LocalDateTime toExclusive)`) use the half-open interval `[from, toExclusive)`: inclusive lower bound, exclusive upper bound (see [Output Ports](../domain/Output-ports.md)).

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

## Input Adapters — REST

Package `nexusmarket.adapters.rest` exposes the role ports to external clients (see [Rest Adapters](../Adapters/Rest-adapters.md) and [API REST Endpoints](../Adapters/Api-rest-endpoints.md)).

- **Controllers:** one per role port (`AuthRestController` and `PublicRestController` for `PublicAccessPort`, `BuyerRestController`, `SellerRestController`, `LogisticsOperatorRestController`, `AdministratorRestController`, `SupervisorRestController`; 108 endpoints in total). Each injects only its role port, receives the `User` loaded by the JWT filter (`@AuthenticationPrincipal`) and never implements business rules.
- **Requests / Responses:** DTOs in `dtos/requests` (Jakarta validation, see [Rest Validation](../Adapters/Rest-validation.md)) and `dtos/responses`; they never enter the domain, and responses never include passwords or hashes.
- **Mappers:** request DTO → domain values, Domain Model → response DTO.
- **Errors:** `GlobalExceptionHandler` maps every exception to the standard error body (see [Global Exception Handler](../Adapters/Global-exception-handler.md)).

## Output Adapters (implemented)

Every output port is implemented:

- **MySQL (Spring Data JPA)** — `adapters/persistence/jpa`: flat entities, one mapper per entity, one Spring Data repository per table and one adapter per repository port; `JpaAggregateLoader` rebuilds cross-table references. See [Persistence Adapters](../Adapters/Persistence-adapters.md).
- **MongoDB (Spring Data MongoDB)** — `adapters/persistence/mongodb`: `AuditLogDocument` in the `audit_logs` collection. See [Persistence Adapters](../Adapters/Persistence-adapters.md).
- **Security** — `adapters/security`: `JwtProvider` (`JwtServicePort`, minimal token `sub`/`jti`/`ver`/`iat`/`exp`) and `BCryptPasswordServiceAdapter` (`PasswordServicePort`). See [Security Adapters](../Adapters/Security-adapters.md).
- **Payment** — `adapters/payment`: `SimulatedPaymentGatewayAdapter` returns `PaymentResult.APPROVED` up to a configurable order total and `PaymentResult.REJECTED` above it.

**Transactions:** the use case adapters (`adapters/useCases`) are the transaction boundary: each use case runs in one transaction (all or nothing), queries and reports are read-only, and a rejected payment commits so its audit record survives (see [Use Cases Adapters](../Adapters/Use-cases-adapters.md), section 3). Domain services declare no transactions; persistence adapters join the transaction of the use case. Audit records are written to MongoDB after the commit, and inventories, orders and carts use optimistic locking (`@Version`), answering 409 on a concurrent modification (see [Persistence Adapters](../Adapters/Persistence-adapters.md), sections 5.6 and 7).

---

# Infrastructure

Technical components without business logic:

- **Seed (implemented)** — `infrastructure/seed/DatabaseSeeder`: creates the initial `Administrator` at startup when none exists (credentials in `app.seed.admin`, password hashed through `PasswordServicePort`).
- **Database:** Hibernate `ddl-auto: update` creates and updates the MySQL schema; the database itself is created by `createDatabaseIfNotExist=true`.
- **Security (implemented, `adapters/security`):** `SecurityConfig` and the JWT authentication filter, aligned with RG-01 (every operation must be executed by an authenticated user). The filter validates the token, extracts the id with `extractUserId`, loads the user with `LoadAuthenticatedUserService`, verifies that it is `ACTIVE` and that `extractTokenVersion` equals its `authTokenVersion` (changed by logout, password change or status change); any failure is rejected with 401. See [Security Adapters](../Adapters/Security-adapters.md#5-jwt-filter-flow) and [REST Security and CORS](../Adapters/Rest-security-cors.md).

---

# Dependency Flow

Dependencies always point toward the domain.

```text
REST Controller (adapters/rest)
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
                                            Output Adapter (MySQL, MongoDB, security, payment)
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
