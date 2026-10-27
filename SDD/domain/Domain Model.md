# Domain Model — NexusMarket

## Introduction

This document defines the core business entities of **NexusMarket**, a digital marketplace platform that acts as a commercial intermediary between buyers and sellers. These entities capture the business rules, data, relationships, and lifecycle concepts described in the NexusMarket Business Functional Specification.

The model follows Object-Oriented Design and Domain-Driven Design (DDD) principles. Inheritance is used to represent genuine domain specialization, and explicit object relationships are preferred over generic identifier fields.

Every class described here exists with the same name in the package `nexusmarket.domain.models`. The domain services that orchestrate these entities are described in [Domain Services](Domain%20Services.md). Value Objects (catalogs, `Address`, `Variant`) are described in [Domain Value Objects](Domain%20Value%20Objects.md) and live in `nexusmarket.domain.valueobjects`. Domain exceptions live in `nexusmarket.domain.exceptions`.

The model distinguishes between:

* **Users**, people authorized to interact with the system, each holding a single role.
* **Sellers**, responsible for commercializing products and managing their own warehouses.
* **Buyers**, users who make purchases within the Marketplace.
* **Warehouses**, physical locations where inventory is managed, owned either by the Marketplace or by a Seller.
* **Products**, physical or digital goods offered in the catalog.
* **Inventory**, distributed stock linked to a specific physical product and warehouse.
* **Carts**, the provisional selection of products made by a buyer.
* **Orders**, the formal commercial commitment derived from a cart; its lifecycle is the central process of the system.
* **Invoicing, Shipments, Returns and Refunds**, processes that follow order confirmation.

An order may generate one invoice, one or more shipments, and, eventually, associated return requests and refunds.

---

# General Conventions

* **Identity:** entities with an `id` attribute compare equality by `id`. `Inventory` has no `id`: its identity is the pair (`product`, `warehouse`).
* **Collections** are never exposed for direct modification: getters return unmodifiable views and changes go through domain methods (for example `Cart.addItem`, `Cart.removeItem`, `Seller.addWarehouse`).
* **Rehydration constructors:** entities with a lifecycle provide a second constructor that receives the current status (and collections) to rebuild a persisted object. Users are rehydrated with their stored `authTokenVersion`.
* **Validation:** every rule violation raises a subclass of `DomainException` (see [Domain Exceptions](#domain-exceptions)). Required references that are `null` and required texts that are blank are rejected.

---

# Domain Class Hierarchy

```text
User (Abstract)
├── Buyer
├── Seller
├── LogisticsOperator
├── Administrator
└── Supervisor

Product (Abstract)
├── PhysicalProduct
└── DigitalProduct

Warehouse
Inventory
InventoryMovement

Cart
CartItem

Order
OrderItem

Invoice
Shipment
ReturnRequest
Refund

AuditLog

AuthenticationResult
```

---

# Domain Relationships

```text
User
   │
   ├── Buyer ──────owns────────────> Cart (exactly one)
   │      │
   │      └──places────────────────> Order
   │
   ├── Seller ───manages───────────> Warehouse (type SELLER)
   │      │
   │      └──publishes─────────────> Product
   │
   ├── LogisticsOperator ──handles──> Shipment
   │
   ├── Administrator ──onboards─────> Seller
   │      └──registers─────────────> Warehouse (type MARKETPLACE)
   │
   └── Supervisor ──reviews─────────> (whole system, read-only)

Warehouse
   │
   └── holds ───────────────────────> Inventory

PhysicalProduct
   │
   └── available in ────────────────> Inventory
                                         │
                                         └── records ─> InventoryMovement

Cart
   ├── belongs to ───────────────────> Buyer
   └── contains ─────────────────────> CartItem ──references──> Product

Order
   ├── contains ─────────────────────> OrderItem ──references──> Product
   ├── placed by ────────────────────> Buyer
   ├── generates ────────────────────> Invoice
   ├── generates ────────────────────> Shipment (one per origin warehouse)
   └── may generate ─────────────────> ReturnRequest ──generates──> Refund

Shipment
   ├── dispatched from ──────────────> Warehouse
   └── handled by ───────────────────> LogisticsOperator

AuditLog
   └── performed by ─────────────────> User
```

---

# Entities

---

# User (Abstract)

## Description

Represents any person authorized to interact with NexusMarket. Centralizes the identity and contact information shared by all system participants.

Each participant holds a single role within the system and may only interact with the information corresponding to their responsibilities.

This class cannot be instantiated directly.

## Attributes

| Attribute            | Type       | Description                                                                          |
| -------------------- | ---------- | ------------------------------------------------------------------------------------ |
| id                   | String     | Uniquely identifies the user. Cannot be empty.                                       |
| identificationNumber | String     | Identity document number. Cannot be empty. Unique across the platform.               |
| fullName             | String     | Official name of the user. Cannot be empty.                                          |
| email                | String     | Primary access and communication channel. Must have a valid format. Unique across the platform. |
| password             | String     | Encrypted password (never the plain text). Cannot be empty.                          |
| role                 | SystemRole | Defines the user's responsibilities and permissions. Fixed by the subclass.          |
| status               | UserStatus | Operational condition of the user (Active, Blocked, Inactive).                       |
| authTokenVersion     | Integer    | Session version embedded in every issued token. Starts at `1`; always `>= 1`. Advancing it invalidates every previously issued token. |

## Constructors

| Constructor | Description |
| --- | --- |
| New user | Receives the identity, contact, password and status data; `authTokenVersion` starts at `1`. |
| Rehydration | Same data plus the stored `Integer authTokenVersion`, to rebuild a persisted user. A `null` value or a value lower than `1` → `InvalidUserException`. |

Every subclass exposes both constructors and fixes its own `role`:

| Subclass | New user | Rehydration |
| --- | --- | --- |
| `Buyer` | `(id, identificationNumber, fullName, email, password, status, primaryAddress, commercialStatus)` | `(id, identificationNumber, fullName, email, password, status, primaryAddress, additionalAddresses, commercialStatus, orders, authTokenVersion)` |
| `Seller` | `(id, identificationNumber, fullName, email, password, status)` | `(id, identificationNumber, fullName, email, password, status, authTokenVersion)` |
| `LogisticsOperator` | `(id, identificationNumber, fullName, email, password, status)` | `(id, identificationNumber, fullName, email, password, status, authTokenVersion)` |
| `Administrator` | `(id, identificationNumber, fullName, email, password, status)` | `(id, identificationNumber, fullName, email, password, status, authTokenVersion)` |
| `Supervisor` | `(id, identificationNumber, fullName, email, password, status)` | `(id, identificationNumber, fullName, email, password, status, authTokenVersion)` |

The new-user constructor delegates to the rehydration constructor with `authTokenVersion = 1`.

## Behavior

| Method                                  | Description                                                   |
| --------------------------------------- | ------------------------------------------------------------- |
| `changeStatus(UserStatus newStatus)`    | Changes the operational condition of the user and calls `bumpAuthTokenVersion()`. |
| `changePassword(String encodedPassword)` | Replaces the encrypted password and calls `bumpAuthTokenVersion()`. |
| `bumpAuthTokenVersion()`                | Advances `authTokenVersion` by one, invalidating every previously issued token. Used by logout, status change and password change. |
| `updateFullName(String fullName)`       | Replaces the full name.                                       |
| `isActive()`                            | `true` when `status` is `ACTIVE`.                             |

## Business Rules

* A user holds exactly one `role`; it is assigned by the subclass constructor and cannot change.
* `id`, `identificationNumber`, `fullName` and `password` cannot be empty; `email` must match `local@domain.tld` → `InvalidUserException`.
* `password` always holds the value produced by `PasswordServicePort.encrypt`; `identificationNumber` and `email` are immutable.
* The uniqueness of `identificationNumber` and `email` across the platform is enforced at domain service level (it requires querying other users).
* `authTokenVersion` is never supplied by the caller of a registration: new users always start at `1`. Every token carries the version it was issued with; a token whose version differs from the stored `authTokenVersion` is rejected (logout, blocking a user or changing its password invalidates the previous tokens).

## Relationships

* A `User` is specialized as `Buyer`, `Seller`, `LogisticsOperator`, `Administrator`, or `Supervisor`.

---

# Buyer

## Description

Represents a user who makes purchases within the Marketplace. Holds the information required to participate in commercial processes.

**Key constraint:** a Buyer never manages information belonging to other buyers or to inventory.

## Inherits From

`User` (role always `BUYER`)

## Attributes

| Attribute           | Type             | Description                                                   |
| ------------------- | ---------------- | ------------------------------------------------------------- |
| primaryAddress      | Address          | Usual delivery location. Required.                            |
| additionalAddresses | List\<Address\>  | Secondary delivery locations. Empty by default. No duplicates. |
| commercialStatus    | CommercialStatus | Buyer's condition for making purchases. Required.             |
| cart                | Cart             | Buyer's active shopping cart.                                 |
| orders              | List\<Order\>    | Buyer's historical and active orders. Empty by default.       |

## Behavior

| Method                                   | Description                                                              |
| ---------------------------------------- | ------------------------------------------------------------------------ |
| `setPrimaryAddress(Address)`             | Replaces the usual delivery location.                                    |
| `addAdditionalAddress(Address)`          | Registers a secondary delivery location (duplicates rejected).           |
| `removeAdditionalAddress(Address)`       | Removes a registered secondary delivery location.                        |
| `setCommercialStatus(CommercialStatus)`  | Changes the buyer's purchasing condition.                                |
| `assignCart(Cart)`                       | Assigns the buyer's unique cart.                                         |
| `addOrder(Order)`                        | Registers an order placed by this buyer.                                 |
| `canPurchase()`                          | `true` when the user is `ACTIVE` and `commercialStatus` is `ENABLED`.    |

## Business Rules

* A buyer owns exactly one `Cart`: `assignCart` rejects a cart of another buyer or a second cart → `InvalidCartException`.
* `addOrder` rejects an order placed by another buyer → `InvalidOrderException`.
* `orders` is not populated by default; it is loaded on demand by the corresponding query service.

## Relationships

* A buyer owns exactly one `Cart`.
* A buyer places zero or more `Order` instances.

---

# Seller

## Description

Represents the entity responsible for commercializing products within the Marketplace.

**Business rule:** sellers cannot self-register; they are onboarded exclusively by the Administrator, who registers the seller together with its first warehouse (enforced at domain service level).

## Inherits From

`User` (role always `SELLER`)

## Attributes

| Attribute  | Type              | Description                                                              |
| ---------- | ----------------- | ------------------------------------------------------------------------ |
| warehouses | List\<Warehouse\> | Own warehouses associated with the seller. At least one upon onboarding. |
| products   | List\<Product\>   | Products published by the seller. Empty by default.                      |

## Behavior

| Method                     | Description                                                                    |
| -------------------------- | ------------------------------------------------------------------------------ |
| `addWarehouse(Warehouse)`  | Associates an own warehouse; it must belong to this seller → `InvalidWarehouseException`. |
| `addProduct(Product)`      | Registers a published product; it must belong to this seller → `InvalidProductException`. |

## Relationships

* A seller manages one or more `Warehouse` instances of type `SELLER`.
* A seller publishes zero or more `Product` instances.
* A seller is onboarded by an `Administrator`.

---

# LogisticsOperator

## Description

Represents the user responsible for the physical operation of warehouses and dispatches.

## Inherits From

`User` (role always `LOGISTICS_OPERATOR`)

## Attributes

No additional attributes beyond those inherited from `User`.

## Relationships

* Handles the operational lifecycle of one or more `Shipment` instances.
* Participates in managing the `Inventory` of the warehouses it operates.

---

# Administrator

## Description

Responsible for the administration of sellers and warehouses within the Marketplace.

## Inherits From

`User` (role always `ADMINISTRATOR`)

## Attributes

No additional attributes beyond those inherited from `User`.

## Relationships

* Registers and onboards `Seller` instances.
* Registers `Warehouse` instances of type `MARKETPLACE`.

---

# Supervisor

## Description

Read-only profile for oversight and operational monitoring. Has no ability to modify system information.

## Inherits From

`User` (role always `SUPERVISOR`)

## Attributes

No additional attributes beyond those inherited from `User`.

## Relationships

* Reviews consolidated information across all business domains, without modification capability.

---

# Warehouse

## Description

Represents a physical location where inventory is managed. Warehouses are classified as Marketplace warehouses or Seller warehouses.

## Attributes

| Attribute   | Type              | Description                                                         |
| ----------- | ----------------- | ------------------------------------------------------------------- |
| id          | String            | Uniquely identifies the warehouse. Cannot be empty.                 |
| name        | String            | Descriptive name of the warehouse. Cannot be empty.                 |
| location    | Address           | Physical location of the warehouse. Required.                       |
| type        | WarehouseType     | Classifies the warehouse as `MARKETPLACE` or `SELLER`. Required.    |
| seller      | Seller            | Owning seller; only when `type` is `SELLER`, `null` otherwise.      |
| inventories | List\<Inventory\> | Inventory records held in this warehouse. Empty by default.         |

## Behavior

| Method                     | Description                                                           |
| -------------------------- | --------------------------------------------------------------------- |
| `setName(String)`          | Renames the warehouse.                                                |
| `setLocation(Address)`     | Changes the physical location.                                        |
| `addInventory(Inventory)`  | Holds a new inventory record; it must belong to this warehouse → `InvalidInventoryException`. |
| `isMarketplace()`          | `true` when `type` is `MARKETPLACE`.                                  |
| `isSellerOwned()`          | `true` when `type` is `SELLER`.                                       |

## Business Rules

* A `SELLER` warehouse must have exactly one `seller`; a `MARKETPLACE` warehouse cannot have one → `InvalidWarehouseException`.

## Relationships

* A `SELLER`-type warehouse belongs to exactly one `Seller`.
* A `MARKETPLACE`-type warehouse is registered and managed by an `Administrator`.
* A warehouse holds zero or more `Inventory` instances.

---

# Product (Abstract)

## Description

Represents a good offered in the Marketplace catalog. The catalog distinguishes between physical products (requiring inventory and dispatch) and digital products (immediate delivery after payment).

This class cannot be instantiated directly.

## Attributes

| Attribute   | Type            | Description                                                        |
| ----------- | --------------- | ------------------------------------------------------------------ |
| id          | String          | Uniquely identifies the product. Cannot be empty.                  |
| name        | String          | Commercial name of the product. Cannot be empty.                   |
| description | String          | Functional description of the product. Cannot be empty.            |
| price       | BigDecimal      | Current unit price. Zero or positive. Copied to `CartItem.unitPrice` when added to a cart. |
| seller      | Seller          | Seller responsible for the product. Required.                      |
| variants    | List\<Variant\> | Differences such as color, size, model. Empty by default. No duplicates. |
| status      | ProductStatus   | `DRAFT`, `PUBLISHED`, `SUSPENDED`, or `DISCONTINUED`. A new product starts in `DRAFT`. |

The product **type** is not stored: it is derived from the specialization through `getType()` (`ProductType.PHYSICAL` or `ProductType.DIGITAL`).

## Behavior

| Method                                                  | Description                                                    |
| ------------------------------------------------------- | -------------------------------------------------------------- |
| `getType()`                                             | Abstract; returns the `ProductType` of the specialization.     |
| `updateInfo(String name, String description, BigDecimal price, List<Variant> variants)` | Updates the commercial information (replaces variants). |
| `addVariant(Variant)`                                   | Adds a variant (duplicates rejected).                          |
| `hasVariant(Variant)`                                   | `true` when the product offers the variant.                    |
| `publish()`                                             | `DRAFT` / `SUSPENDED` → `PUBLISHED`.                           |
| `suspend()`                                             | `PUBLISHED` → `SUSPENDED`.                                     |
| `discontinue()`                                         | `DRAFT` / `PUBLISHED` / `SUSPENDED` → `DISCONTINUED` (terminal). |
| `isPublished()`                                         | `true` when `status` is `PUBLISHED`.                           |

## Business Rules

* Invalid data or duplicated variants → `InvalidProductException`.
* Status transitions not listed above → `InvalidProductStatusException`.

## Relationships

* A product is published by exactly one `Seller`.
* A product may have zero or more `Variant` values.

---

# PhysicalProduct

## Description

Represents a tangible good that requires inventory and logistics dispatch for delivery.

## Inherits From

`Product` (`getType()` returns `PHYSICAL`)

## Attributes

| Attribute   | Type              | Description                                                     |
| ----------- | ----------------- | --------------------------------------------------------------- |
| inventories | List\<Inventory\> | Distributed stock of the product per warehouse. Empty by default. |

## Behavior

| Method                     | Description                                                             |
| -------------------------- | ----------------------------------------------------------------------- |
| `addInventory(Inventory)`  | Links an inventory record; it must belong to this product → `InvalidInventoryException`. |
| `totalStock()`             | Sum of the quantities of all its inventory records.                     |

## Relationships

* A physical product is linked to one or more `Inventory` instances.

---

# DigitalProduct

## Description

Represents an intangible good delivered immediately after payment confirmation, requiring no physical inventory or logistics process.

## Inherits From

`Product` (`getType()` returns `DIGITAL`)

## Attributes

No additional attributes beyond those inherited from `Product`.

## Relationships

* Holds no relationship with `Warehouse` or `Inventory`.
* Never generates `Shipment` instances.

---

# Inventory

## Description

Represents the available stock of a physical product in a specific warehouse. Inventory is distributed and must be mandatorily linked to a product and a warehouse.

**Constraint:** negative stock is never allowed under any circumstance.

## Attributes

| Attribute | Type                      | Description                                                   |
| --------- | ------------------------- | ------------------------------------------------------------- |
| product   | PhysicalProduct           | Product the inventory record belongs to. Required.            |
| warehouse | Warehouse                 | Warehouse where the stock is held. Required.                  |
| quantity  | int                       | Available stock (can be reserved). Can never be negative.     |
| reservedQuantity | int                | Stock held for orders in progress. Can never be negative.     |
| condition | InventoryCondition        | Operational condition of the record. `AVAILABLE` by default.  |
| movements | List\<InventoryMovement\> | History of movements affecting this inventory record.         |

## Behavior

| Method                                                           | Description                                                    |
| ---------------------------------------------------------------- | -------------------------------------------------------------- |
| `registerMovement(MovementType type, int quantity, LocalDateTime date)` | Records a movement, applies its effect to `quantity` / `reservedQuantity` and returns the new `InventoryMovement`. |
| `setCondition(InventoryCondition)`                               | Changes the operational condition of the record.               |
| `canReserve(int quantity)`                                       | `true` when the record is `AVAILABLE` and has enough available stock. |

## Business Rules

* The initial quantities cannot be negative → `InvalidInventoryException`.
* Effect of each movement type:
  * `INBOUND`, `RETURN`: `quantity + n`.
  * `RESERVATION`: `quantity - n`, `reservedQuantity + n`.
  * `RESERVATION_RELEASE`: `reservedQuantity - n`, `quantity + n`.
  * `SALE_OUTBOUND`: `reservedQuantity - n` (a sale always consumes a previous reservation).
  * `ADJUSTMENT`: `quantity + n` (signed, non-zero).
* A movement that would leave the available stock below zero is rejected → `InsufficientInventoryException`; one that would leave the reserved stock below zero → `InvalidInventoryException`.
* A record in condition `DAMAGED` can never be reserved → `InvalidInventoryException`.
* Identity is the pair (`product`, `warehouse`).

## Relationships

* An inventory record belongs to exactly one `PhysicalProduct` and exactly one `Warehouse`.
* An inventory record logs zero or more `InventoryMovement` instances.

---

# InventoryMovement

## Description

Represents a change to the stock of an `Inventory` record. Immutable once recorded.

## Attributes

| Attribute | Type          | Description                                                        |
| --------- | ------------- | ------------------------------------------------------------------ |
| type      | MovementType  | `INBOUND`, `RESERVATION`, `SALE_OUTBOUND`, `ADJUSTMENT`, or `RETURN`. |
| quantity  | int           | Quantity affected. Positive, except `ADJUSTMENT` (signed, non-zero). |
| date      | LocalDateTime | Moment the movement was recorded.                                  |

## Business Rules

* Invalid type, date or quantity → `InvalidInventoryException`.

## Relationships

* An inventory movement belongs to exactly one `Inventory` record.

---

# Cart

## Description

Represents the provisional selection of products made by a buyer before confirming an order.

## Attributes

| Attribute | Type             | Description                             |
| --------- | ---------------- | --------------------------------------- |
| id        | String           | Uniquely identifies the cart. Cannot be empty. |
| buyer     | Buyer            | Owning buyer of the cart. Required.     |
| items     | List\<CartItem\> | Selected products. Empty by default.    |

## Behavior

| Method                                                                   | Description                                                             |
| ------------------------------------------------------------------------ | ----------------------------------------------------------------------- |
| `addItem(Product product, Variant variant, int quantity, BigDecimal unitPrice)` | Adds a product. If the same product and variant are already in the cart, the quantities are accumulated and the unit price is updated. |
| `removeItem(Product product, Variant variant)`                           | Removes the line of that product and variant.                           |
| `changeItemQuantity(Product product, Variant variant, int quantity)`     | Replaces the quantity of that line.                                     |
| `totalAmount()`                                                          | Sum of all item subtotals.                                              |
| `clear()`                                                                | Removes every item (e.g. after it gives rise to an order).              |
| `isEmpty()`                                                              | `true` when the cart has no items.                                      |

## Business Rules

* Lines are identified by the pair (`product`, `variant`); there are no duplicated lines.
* Removing or changing a product that is not in the cart → `InvalidCartException`.
* Item-level rules are described in `CartItem`.

## Relationships

* A cart belongs to exactly one `Buyer`.
* A cart contains zero or more `CartItem` instances.
* Upon confirmation, the cart gives rise to an `Order`.

---

# CartItem

## Description

Represents a product selected within a cart, along with the chosen variant, quantity and the unit price captured when it was added.

## Attributes

| Attribute | Type       | Description                                          |
| --------- | ---------- | ---------------------------------------------------- |
| product   | Product    | Selected product. Required.                          |
| variant   | Variant    | Chosen variant, if applicable (`null` otherwise).    |
| quantity  | int        | Selected quantity. Always positive.                  |
| unitPrice | BigDecimal | Unit price captured when the product was added. Zero or positive. |

## Behavior

| Method                                    | Description                                              |
| ----------------------------------------- | -------------------------------------------------------- |
| `changeQuantity(int quantity)`            | Replaces the selected quantity.                          |
| `subtotal()`                              | `unitPrice` × `quantity`.                                |
| `matches(Product product, Variant variant)` | `true` when the line corresponds to that product and variant. |

## Business Rules

* Only products in `PUBLISHED` status can be added → `InvalidProductStatusException`.
* The chosen variant must be offered by the product → `InvalidCartException`.
* `quantity` must be positive and `unitPrice` zero or positive → `InvalidCartException`.

## Relationships

* A `CartItem` references exactly one `Product`.

---

# Order

## Description

Represents the formal commercial commitment derived from confirming a cart. Its lifecycle is the central process of the system.

**Constraint:** a finalized order (`DELIVERED`) can never be modified under any circumstance.

## Attributes

| Attribute | Type              | Description                                                         |
| --------- | ----------------- | ------------------------------------------------------------------- |
| id        | String            | Uniquely identifies the order. Cannot be empty.                     |
| buyer     | Buyer             | Buyer who placed the order. Required.                               |
| items     | List\<OrderItem\> | Products included in the order. One or more.                        |
| status    | OrderStatus       | Lifecycle stage. A new order starts at `PENDING_PAYMENT`.           |
| createdAt | LocalDateTime     | Moment the order was confirmed (current time if not given).         |
| invoice   | Invoice           | Associated invoice, generated once payment is validated (`null` before). |
| shipments | List\<Shipment\>  | Shipments generated for the order's physical products.              |

## Behavior

| Method                      | Description                                                          |
| --------------------------- | -------------------------------------------------------------------- |
| `markPaid()`                | `PENDING_PAYMENT` → `PAID`.                                          |
| `markShipped()`             | `PAID` → `SHIPPED`.                                                  |
| `markDelivered()`           | `SHIPPED` → `DELIVERED` (finalized). An order without physical items goes `PAID` → `DELIVERED`. |
| `hasPhysicalItems()`        | `true` when at least one item is a physical product.                 |
| `attachInvoice(Invoice)`    | Attaches the unique invoice of the order.                            |
| `addShipment(Shipment)`     | Registers a shipment of the order.                                   |
| `totalAmount()`             | Sum of all order item subtotals.                                     |
| `isFinalized()`             | `true` when `status` is `DELIVERED`.                                 |

## Business Rules

* An order contains at least one item → `InvalidOrderException`.
* Transitions not listed above → `InvalidOrderStatusTransitionException`. `PAID` → `DELIVERED` is only allowed when the order has no physical items (digital products are delivered immediately after payment).
* Any modification of a `DELIVERED` order → `OrderAlreadyFinalizedException`.
* An order generates exactly one invoice: a second invoice → `InvalidOrderException`; an invoice of another order → `InvalidInvoiceException`.
* A shipment of another order → `InvalidShipmentException`.

## Relationships

* An order belongs to exactly one `Buyer`.
* An order contains one or more `OrderItem` instances.
* An order generates exactly one `Invoice` once payment is validated.
* An order generates zero or more `Shipment` instances (one per origin warehouse involved, only for physical products).
* An order may give rise to one or more `ReturnRequest` instances.

---

# OrderItem

## Description

Represents a confirmed product within an order, along with its quantity and price at the time of purchase. Immutable.

## Attributes

| Attribute | Type       | Description                                          |
| --------- | ---------- | ---------------------------------------------------- |
| product   | Product    | Purchased product. Required.                         |
| variant   | Variant    | Purchased variant, if applicable (`null` otherwise). |
| quantity  | int        | Purchased quantity. Always positive.                 |
| unitPrice | BigDecimal | Unit price in effect at the time of purchase. Zero or positive. |
| originWarehouse | Warehouse | Warehouse where the stock of a physical item was reserved. Required for physical products; `null` for digital products. |

## Behavior

| Method       | Description                |
| ------------ | -------------------------- |
| `subtotal()` | `unitPrice` × `quantity`.  |
| `isPhysical()` | `true` when the product is a `PhysicalProduct`. |

## Business Rules

* Invalid product, quantity or unit price → `InvalidOrderException`.
* A physical product requires an `originWarehouse`; a digital product cannot have one → `InvalidOrderException`.

## Relationships

* An `OrderItem` references exactly one `Product`.

---

# Invoice

## Description

Represents the commercial information associated with the sale of an order, generated once payment is validated. Immutable.

## Attributes

| Attribute   | Type          | Description                                                     |
| ----------- | ------------- | --------------------------------------------------------------- |
| id          | String        | Uniquely identifies the invoice. Cannot be empty.               |
| order       | Order         | Invoiced order. Required.                                       |
| totalAmount | BigDecimal    | Total invoiced value. Zero or positive; by default computed from `Order.totalAmount()`. |
| issuedAt    | LocalDateTime | Invoice issuance moment. Required.                              |

## Business Rules

* Invalid data → `InvalidInvoiceException`.

## Relationships

* An invoice belongs to exactly one `Order`.

---

# Shipment

## Description

Represents the logistics process of packing, dispatching, and transporting the physical products of an order.

## Attributes

| Attribute         | Type              | Description                                          |
| ----------------- | ----------------- | ---------------------------------------------------- |
| id                | String            | Uniquely identifies the shipment. Cannot be empty.   |
| order             | Order             | Order the shipment belongs to. Required.             |
| originWarehouse   | Warehouse         | Warehouse from which the shipment is dispatched. Required. |
| deliveryAddress   | Address           | Delivery address, taken from the order. Required.    |
| logisticsOperator | LogisticsOperator | Operator responsible for executing the shipment. Required. |
| status            | ShipmentStatus    | Logistics status. A new shipment starts at `PREPARING`. |

## Behavior

| Method            | Description                  |
| ----------------- | ---------------------------- |
| `markPacked()`    | `PREPARING` → `PACKED`.      |
| `markInTransit()` | `PACKED` → `IN_TRANSIT`.     |
| `markDelivered()` | `IN_TRANSIT` → `DELIVERED`.  |
| `isDelivered()`   | `true` when `status` is `DELIVERED`. |

## Business Rules

* Invalid data → `InvalidShipmentException`.
* Transitions not listed above → `InvalidStatusTransitionException`.

## Relationships

* A shipment belongs to exactly one `Order` and exactly one origin `Warehouse`.
* A shipment is handled by exactly one `LogisticsOperator`.

---

# ReturnRequest

## Description

Represents a request to return one or more products from an already delivered order.

## Attributes

| Attribute | Type         | Description                                            |
| --------- | ------------ | ------------------------------------------------------ |
| id        | String       | Uniquely identifies the return request. Cannot be empty. |
| order     | Order        | Order the return applies to. Required.                 |
| reason    | String       | Justification for the return. Cannot be empty.         |
| status    | ReturnStatus | Status of the return process. Starts at `REQUESTED`.   |

## Behavior

| Method      | Description                  |
| ----------- | ---------------------------- |
| `approve()` | `REQUESTED` → `APPROVED`.    |
| `reject()`  | `REQUESTED` → `REJECTED`.    |

## Business Rules

* Invalid data → `InvalidReturnRequestException`.
* Transitions not listed above → `InvalidStatusTransitionException`.

## Relationships

* A return request belongs to exactly one `Order`.
* A return request may give rise to a `Refund`.
* Once approved, it generates an `InventoryMovement` of type `RETURN` on the corresponding inventory record (physical products only; coordinated by a domain service).

---

# Refund

## Description

Represents the financial restitution associated with an approved return.

## Attributes

| Attribute     | Type          | Description                                          |
| ------------- | ------------- | ---------------------------------------------------- |
| id            | String        | Uniquely identifies the refund. Cannot be empty.     |
| returnRequest | ReturnRequest | Return request that gives rise to the refund. Required. |
| amount        | BigDecimal    | Refunded value. Must be positive.                    |
| status        | RefundStatus  | Status of the refund process. Starts at `PENDING`.   |

## Behavior

| Method            | Description                 |
| ----------------- | --------------------------- |
| `markProcessed()` | `PENDING` → `PROCESSED`.    |
| `reject()`        | `PENDING` → `REJECTED`.     |

## Business Rules

* Invalid data → `InvalidRefundException`.
* Transitions not listed above → `InvalidStatusTransitionException`.

## Relationships

* A refund belongs to exactly one `ReturnRequest`.

---

# AuditLog

## Description

Represents an immutable record of a significant business operation. Audit records are append-only (stored through `AuditLogRepositoryPort`, intended for MongoDB) and are never modified or deleted.

## Attributes

| Attribute   | Type                   | Description                                                   |
| ----------- | ---------------------- | ------------------------------------------------------------- |
| id          | String                 | Uniquely identifies the record. Cannot be empty.              |
| action      | AuditAction            | Audited business action. Required.                            |
| performedBy | User                   | User who performed the operation. Required.                   |
| role        | SystemRole             | Role of `performedBy` when the operation was performed (taken from the user). |
| entityType  | AuditEntityType        | Type of the affected entity. Required.                        |
| entityId    | String                 | Identifier of the affected entity. Cannot be empty.           |
| occurredAt  | LocalDateTime          | Moment of the operation. Required.                            |
| details     | Map\<String, String\> | Operation-specific details. Empty by default; unmodifiable.   |

## Business Rules

* Invalid or missing data → `InvalidAuditLogException`.
* All attributes are final; there are no setters.

## Relationships

* An audit record is performed by exactly one `User`.

---

# AuthenticationResult

## Description

Outcome of a successful login (`LoginService.login`). Carries the authenticated user together with the freshly issued token. It is not persisted.

## Attributes

| Attribute         | Type   | Description                                                                 |
| ----------------- | ------ | --------------------------------------------------------------------------- |
| authenticatedUser | User   | Stored user that authenticated successfully. Required.                      |
| token             | String | Token issued by `JwtServicePort`; it carries the user's current `authTokenVersion`. Cannot be empty. |

## Business Rules

* Missing user or empty token → `InvalidUserException`.
* All attributes are final; there are no setters.

## Relationships

* An authentication result references exactly one `User`.

---

# Domain Exceptions

All business exceptions live in `nexusmarket.domain.exceptions` and extend `DomainException` (unchecked, extends `RuntimeException`). They belong exclusively to the domain; adapters translate them into technology-specific responses.

```text
DomainException
├── InvalidUserException
├── InvalidWarehouseException
├── InvalidProductException
├── InvalidProductStatusException
├── InvalidInventoryException
├── InsufficientInventoryException
├── InvalidCartException
├── InvalidOrderException
├── InvalidOrderStatusTransitionException
├── OrderAlreadyFinalizedException
├── InvalidInvoiceException
├── InvalidShipmentException
├── InvalidReturnRequestException
├── InvalidRefundException
├── InvalidStatusTransitionException
├── InvalidAddressException
├── InvalidVariantException
├── InvalidCatalogCodeException
├── InvalidAuditLogException
├── UnauthorizedOperationException
├── EntityNotFoundException
├── UserAlreadyExistsException
├── InvalidCredentialsException
└── PaymentRejectedException
```

| Exception                               | Raised when                                                                  | Raised by |
| --------------------------------------- | ---------------------------------------------------------------------------- | --------- |
| `InvalidUserException`                  | User data is invalid (empty id, identification number, name; invalid email; missing role, status, address or commercial status). | `User`, `Buyer` |
| `InvalidWarehouseException`             | Warehouse data is invalid, the type/seller combination is inconsistent, or a warehouse of another seller is associated. | `Warehouse`, `Seller` |
| `InvalidProductException`               | Product data is invalid, a variant is duplicated, or a product of another seller is associated. | `Product`, `Seller` |
| `InvalidProductStatusException`         | A product status transition is not allowed, or a non-published product is added to a cart. | `Product`, `CartItem` |
| `InvalidInventoryException`             | Inventory or movement data is invalid, or an inventory record is linked to the wrong product/warehouse. | `Inventory`, `InventoryMovement`, `PhysicalProduct`, `Warehouse` |
| `InsufficientInventoryException`        | A movement would leave the stock below zero.                                 | `Inventory` |
| `InvalidCartException`                  | Cart or cart item data is invalid, the product/variant is not in the cart, or the buyer's cart assignment is invalid. | `Cart`, `CartItem`, `Buyer` |
| `InvalidOrderException`                 | Order or order item data is invalid, a second invoice is attached, or an order of another buyer is registered. | `Order`, `OrderItem`, `Buyer` |
| `InvalidOrderStatusTransitionException` | An order status transition is not allowed.                                   | `Order` |
| `OrderAlreadyFinalizedException`        | A `DELIVERED` order is modified.                                             | `Order` |
| `InvalidInvoiceException`               | Invoice data is invalid, or an invoice of another order is attached.         | `Invoice`, `Order` |
| `InvalidShipmentException`              | Shipment data is invalid, or a shipment of another order is registered.      | `Shipment`, `Order` |
| `InvalidReturnRequestException`         | Return request data is invalid.                                              | `ReturnRequest` |
| `InvalidRefundException`                | Refund data is invalid.                                                      | `Refund` |
| `InvalidStatusTransitionException`      | A shipment, return request or refund status transition is not allowed.       | `Shipment`, `ReturnRequest`, `Refund` |
| `InvalidAddressException`               | An `Address` is built with missing required values.                          | `Address` |
| `InvalidVariantException`               | A `Variant` is built with missing values.                                    | `Variant` |
| `InvalidCatalogCodeException`           | A business code does not correspond to any value of a catalog.               | All catalog enums (`fromCode`) |
| `InvalidAuditLogException`              | An audit record is missing required data.                                    | `AuditLog` |
| `UnauthorizedOperationException`        | RG-01/RG-02/RG-03 violation: unauthenticated or inactive user, role not allowed or information outside the user's responsibilities. | Domain services |
| `EntityNotFoundException`               | A referenced entity does not exist.                                          | Domain services |
| `UserAlreadyExistsException`            | The identification number or the email is already registered.               | Domain services |
| `InvalidCredentialsException`           | Unknown email or wrong password.                                             | Domain services |
| `PaymentRejectedException`              | The payment gateway rejected the payment of an order.                        | Domain services |
