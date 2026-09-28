# Software Architecture — NexusMarket

## Overview

NexusMarket follows a **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)** principles.

The primary objective of this architecture is to isolate the Marketplace business domain from external technologies, ensuring that business rules remain independent from frameworks, databases, communication protocols, and infrastructure concerns.

This approach promotes maintainability, scalability, testability, and technology independence — which is especially relevant given that the functional specification explicitly excludes graphical interfaces, technical authentication mechanisms, and implementation technologies, leaving those decisions to the infrastructure layer.

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

The application is organized into four major components:

```
Application
│
├── Adapters
│
├── Domain
│
└── Infrastructure
```

Each component has a clearly defined responsibility.

---

# Package Structure

```text
src/
└── main/
    └── java/
        └── nexusmarket/
            │
            ├── App.java
            │
            ├── adapters/
            │   │
            │   ├── in/
            │   │   └── rest/
            │   │       ├── controllers/
            │   │       ├── requests/
            │   │       ├── responses/
            │   │       └── mappers/
            │   │
            │   └── out/
            │       └── persistence/
            │           └── mysql/
            │               ├── adapters/
            │               ├── entities/
            │               ├── repositories/
            │               └── mappers/
            │
            ├── domain/
            │   ├── models/
            │   ├── valueobjects/
            │   ├── enums/
            │   ├── services/
            │   ├── exceptions/
            │   └── ports/
            │       ├── in/
            │       └── out/
            │
            └── infrastructure/
                ├── config/
                ├── database/
                └── security/
```

---

# Layer Responsibilities

## Application

The `application` package (represented by `nexusmarket` above) is the root of the project.

It contains the application entry point and organizes all architectural components.

### Responsibilities

- Application bootstrap.
- Component organization.
- Dependency composition.

---

## App.java

### Description

`App.java` is the application's entry point.

### Responsibilities

- Initialize the application.
- Load the infrastructure.
- Configure dependency injection.
- Start the REST server.

---

# Adapters

Adapters connect external technologies with the NexusMarket business domain.

Adapters translate external requests into domain operations and transform domain objects into technology-specific representations.

The domain never communicates directly with external systems.

---

## Input Adapters

Input adapters expose the application to external clients (buyers, sellers, logistics operators, administrators, and supervisors).

Current implementation:

```
adapters/in/rest
```

### Responsibilities

- Receive HTTP requests.
- Validate incoming data.
- Convert Request DTOs into Domain Models.
- Execute application use cases.
- Convert domain results into Response DTOs.

---

### Controllers

Controllers expose REST endpoints organized by business domain (users, sellers, buyers, warehouses, catalog, inventory, cart, orders, invoicing, shipments, returns, and refunds).

Responsibilities:

- Receive HTTP requests.
- Delegate execution to the domain.
- Return HTTP responses.

Controllers must never implement business rules.

---

### Requests

Request DTOs represent incoming HTTP payloads.

Responsibilities:

- Receive client data.
- Validate input.
- Transport data into the application.

These objects must not contain business logic.

---

### Responses

Response DTOs represent outgoing HTTP responses.

Responsibilities:

- Return processed information.
- Hide internal domain implementation.
- Standardize API responses.

---

### Mappers

Responsible for converting between:

- Request DTO ↔ Domain Model
- Domain Model ↔ Response DTO

This prevents the domain from depending on transport objects.

---

# Output Adapters

Output adapters connect the domain with external resources.

Examples:

- Relational database (MySQL)
- Notification services (order confirmation, shipment status)
- Payment gateways

Current implementation:

```
Persistence
└── MySQL
```

NexusMarket uses a single relational store for all persistence needs, including transactional data (users, sellers, buyers, warehouses, catalog, cart, orders, invoicing) and traceability data (inventory movements, order status history). A single persistence technology was chosen to keep the initial implementation simple; the Output Ports abstraction still allows a different or additional adapter (e.g. a document store for large-scale audit logs) to be introduced later without changing the domain.

---

## MySQL Adapter

Responsible for all relational persistence.

### Components

#### Entities

Represent relational database tables.

#### Repositories

Implement persistence operations.

#### Mappers

Convert Domain Models into database entities.

#### Adapters

Implement Domain Output Ports.

---

# Domain

The Domain layer is the core of the application.

It contains all business rules described in the NexusMarket Business Functional Specification and must remain independent from any external technology.

No class inside the domain may depend on:

- Spring (or any web framework)
- JPA / ORM
- HTTP
- REST
- JSON
- SQL

---

## Models

Contain the business entities.

Examples:

- User, Buyer, Seller, LogisticsOperator, Administrator, Supervisor
- Warehouse
- Product, PhysicalProduct, DigitalProduct
- Inventory, InventoryMovement
- ShoppingCart, CartItem
- Order, OrderItem
- Invoice
- Shipment
- Return, Refund

These objects represent the Marketplace business.

---

## Value Objects

Represent immutable business concepts.

Examples:

- UserRole, UserStatus, CommercialStatus
- WarehouseType
- ProductStatus
- InventoryMovementType
- OrderStatus, ShipmentStatus, ReturnStatus, RefundStatus
- Address, Variant

Value Objects are compared by value instead of identity.

---

## Enums

Contain technical enumerations that do not require business behavior.

---

## Services

Contain business logic that does not naturally belong to a single entity.

Examples organized by subdomain:

- **Users:** RegisterUserService, ChangeUserStatusService
- **Sellers:** OnboardSellerService, QuerySellerService
- **Buyers:** RegisterBuyerService, UpdateBuyerService
- **Warehouses:** RegisterWarehouseService, QueryWarehouseService
- **Catalog:** PublishProductService, UpdateCatalogService
- **Inventory:** RegisterInventoryMovementService, QueryInventoryService
- **Cart:** AddCartItemService, ConfirmCartService
- **Orders:** CreateOrderService, ValidatePaymentService, ShipOrderService, FinalizeOrderService
- **Invoicing:** GenerateInvoiceService
- **Shipments:** GenerateShipmentService, UpdateShipmentStatusService
- **Returns and Refunds:** RequestReturnService, ApproveReturnService, ProcessRefundService
- **Reports:** ConsolidateAdministrativeReportService

Services coordinate business operations while preserving domain integrity.

---

## Ports

Ports define communication contracts between the domain and external technologies.

The domain owns all interfaces.

---

### Input Ports

Represent application use cases.

Examples:

- CreateOrderUseCase
- PublishProductUseCase
- RegisterInventoryMovementUseCase
- ApproveReturnUseCase

Input ports define what the system can do.

---

### Output Ports

Represent dependencies required by the domain.

Examples:

- UserRepositoryPort
- ProductRepositoryPort
- InventoryRepositoryPort
- OrderRepositoryPort
- InvoiceRepositoryPort
- NotificationPort

Output ports define what the domain needs from external systems. All current implementations of these ports are backed by MySQL, but the ports themselves make no assumption about the persistence technology.

---

## Exceptions

Contains business exceptions.

Examples:

- InsufficientInventoryException
- FinalizedOrderCannotBeModifiedException
- DuplicateEmailException
- UnauthorizedSellerOperationException

Business exceptions belong exclusively to the domain.

---

# Infrastructure

Infrastructure contains technical configuration required by the application.

It does not contain business logic.

---

## Config

Responsible for application configuration.

Examples:

- REST configuration
- Serialization
- Environment configuration

---

## Database

Contains MySQL initialization and connection configuration.

Examples:

- Connection pool settings
- Schema migration configuration

---

## Security

Contains authentication and authorization configuration, aligned with the RG-01 constraint (every operation must be executed by an authenticated user).

Examples:

- Token configuration
- Password encoder
- Authentication filters

---

# Dependency Flow

Dependencies always point toward the domain.

```
REST Controller
        │
        ▼
Input Port
        │
        ▼
Domain Service
        │
        ▼
Output Port
        │
        ▼
MySQL Adapter
        │
        ▼
MySQL Database
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
10. The Domain must be fully testable without requiring infrastructure components, including without a running MySQL instance.
11. Each participant (Buyer, Seller, Logistics Operator, Administrator, Supervisor) may only interact with the information corresponding to their role — a rule that must be reflected in the Security layer and enforced at the Input Ports.