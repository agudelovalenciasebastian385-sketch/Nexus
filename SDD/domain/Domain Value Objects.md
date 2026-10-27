# Domain Value Objects — NexusMarket

## Introduction

Value Objects represent immutable concepts within the NexusMarket domain.

Unlike Entities, Value Objects have no identity of their own. They are defined entirely by their values and are used to encapsulate controlled business concepts, improve domain expressiveness, and prevent the use of primitive values or scattered string literals throughout the application.

The NexusMarket domain uses Value Objects for:

* **Business catalogs** (roles, statuses, types), implemented as Java `enum`s that implement `DomainCatalog`.
* **Compound concepts** such as `Address`, `Variant` and `ReportPeriod`, implemented as immutable `final` classes compared by value.

All Value Objects live in the package `nexusmarket.domain.valueobjects`.

---

# Value Object Hierarchy

```text
DomainCatalog (Interface)
├── SystemRole
├── UserStatus
├── CommercialStatus
├── WarehouseType
├── ProductType
├── ProductStatus
├── InventoryCondition
├── MovementType
├── OrderStatus
├── ShipmentStatus
├── ReturnStatus
├── RefundStatus
├── PaymentResult
├── AuditAction
└── AuditEntityType

Address
Variant
ReportPeriod
```

---

# DomainCatalog (Interface)

## Description

Represents a generic business catalog used throughout the NexusMarket domain.

`DomainCatalog` provides a consistent structure for controlled business values that require a code, a human-readable name, and a business description.

Because Java enums cannot extend a class, `DomainCatalog` is modeled as an **interface** implemented by every catalog enum. This preserves the inheritance contract while guaranteeing a fixed set of allowed values.

## Attributes (exposed through getters)

| Attribute   | Type   | Description                                           |
| ----------- | ------ | ----------------------------------------------------- |
| code        | String | Unique business identifier of the catalog value.      |
| name        | String | Human-readable name displayed within the application. |
| description | String | Business definition of the catalog value.             |

## Behavior

Every catalog enum provides a static `fromCode(String code)` method that resolves a value from its business code. An unknown code raises `InvalidCatalogCodeException`.

## Characteristics

* Immutable.
* Equality is determined by value rather than object identity.
* Catalog values are controlled by the domain.
* Catalog values must not be represented by arbitrary strings throughout the application.
* Each catalog value has a unique `code`.

---

# SystemRole

## Description

Represents the responsibilities and permissions assigned to a user within NexusMarket.

The role is a characteristic of `User` because it represents what that user means within the system. Each user holds exactly one role, which is fixed by its subclass (`Buyer`, `Seller`, `LogisticsOperator`, `Administrator`, `Supervisor`).

## Implements

`DomainCatalog`

## Allowed Values

| Code               | Name               | Description                                                       |
| ------------------ | ------------------ | ----------------------------------------------------------------- |
| BUYER              | Buyer              | Person who purchases published products.                          |
| SELLER             | Seller             | Responsible for registering and managing their own products.      |
| LOGISTICS_OPERATOR | Logistics Operator | In charge of the physical operation of warehouses and dispatches. |
| ADMINISTRATOR      | Administrator      | Responsible for the administration of sellers and warehouses.     |
| SUPERVISOR         | Supervisor         | Read-only profile for oversight and operational monitoring.       |

---

# UserStatus

## Description

Represents the operational condition of a user within the platform.

## Implements

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                              |
| -------- | -------- | -------------------------------------------------------- |
| ACTIVE   | Active   | The user can operate normally within the system.         |
| BLOCKED  | Blocked  | The user's access has been suspended.                    |
| INACTIVE | Inactive | The user exists but is not currently enabled to operate. |

---

# CommercialStatus

## Description

Represents a `Buyer`'s condition for making purchases within the Marketplace. Independent from `UserStatus`.

## Implements

`DomainCatalog`

## Allowed Values

| Code       | Name       | Description                                        |
| ---------- | ---------- | -------------------------------------------------- |
| ENABLED    | Enabled    | The buyer can make purchases normally.             |
| RESTRICTED | Restricted | The buyer has temporary limitations on purchasing. |
| SUSPENDED  | Suspended  | The buyer cannot make purchases.                   |

---

# WarehouseType

## Description

Classifies a `Warehouse` according to its owner, distinguishing Marketplace warehouses from Seller warehouses.

## Implements

`DomainCatalog`

## Allowed Values

| Code        | Name        | Description                                         |
| ----------- | ----------- | --------------------------------------------------- |
| MARKETPLACE | Marketplace | Warehouse directly administered by the Marketplace. |
| SELLER      | Seller      | Warehouse owned and administered by a Seller.       |

---

# ProductType

## Description

Classifies a `Product` of the catalog according to how it is delivered. The value is determined by the product specialization: `PhysicalProduct` returns `PHYSICAL` and `DigitalProduct` returns `DIGITAL` (method `Product.getType()`).

## Implements

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description                                                      |
| -------- | -------- | ---------------------------------------------------------------- |
| PHYSICAL | Physical | Tangible good that requires inventory and logistics dispatch.    |
| DIGITAL  | Digital  | Intangible good delivered immediately after payment confirmation. |

---

# ProductStatus

## Description

Represents the publication status of a product within the catalog.

## Implements

`DomainCatalog`

## Allowed Values

| Code         | Name         | Description                                                  |
| ------------ | ------------ | ------------------------------------------------------------ |
| DRAFT        | Draft        | The product is registered but not yet visible in the public catalog. |
| PUBLISHED    | Published    | The product is visible in the public catalog.                |
| SUSPENDED    | Suspended    | The product is temporarily unavailable.                      |
| DISCONTINUED | Discontinued | The product has been permanently withdrawn from the catalog. |

## Allowed Transitions

```text
DRAFT     ──publish()──────> PUBLISHED
SUSPENDED ──publish()──────> PUBLISHED
PUBLISHED ──suspend()──────> SUSPENDED
DRAFT     ──discontinue()──> DISCONTINUED
PUBLISHED ──discontinue()──> DISCONTINUED
SUSPENDED ──discontinue()──> DISCONTINUED
```

A new product starts in `DRAFT`.

`DISCONTINUED` is terminal. Any other transition raises `InvalidProductStatusException`.

---

# InventoryCondition

## Description

Represents the operational condition of an `Inventory` record, that is, how its current stock can be used by the marketplace processes. A new inventory record starts as `AVAILABLE` when no condition is given.

## Implements

`DomainCatalog`

## Allowed Values

| Code         | Name         | Description                                                    |
| ------------ | ------------ | -------------------------------------------------------------- |
| AVAILABLE    | Available    | The stock of the record can be sold and dispatched normally.   |
| RESERVED     | Reserved     | The stock of the record is temporarily held for orders in progress. |
| OUT_OF_STOCK | Out of Stock | The record has no available stock at the moment.               |
| DAMAGED      | Damaged      | The stock of the record is damaged and can never be reserved or sold. |

---

# MovementType

## Description

Classifies the movements that an `Inventory` record can undergo, and determines how each movement affects the stock.

## Implements

`DomainCatalog`

## Allowed Values

| Code          | Name          | Description                                       | Effect on stock          |
| ------------- | ------------- | ------------------------------------------------- | ------------------------ |
| INBOUND       | Inbound       | Stock addition to the warehouse.                  | Increases (quantity > 0) |
| RESERVATION   | Reservation   | Temporary stock hold for an order in progress.    | Available → reserved (quantity > 0) |
| RESERVATION_RELEASE | Reservation Release | Return of reserved stock to the available stock when a reservation is cancelled. | Reserved → available (quantity > 0) |
| SALE_OUTBOUND | Sale Outbound | Stock deduction upon confirming a sale.           | Consumes reserved stock (quantity > 0) |
| ADJUSTMENT    | Adjustment    | Manual correction of stock.                       | Signed, non-zero         |
| RETURN        | Return        | Stock re-entry resulting from an approved return. | Increases (quantity > 0) |

---

# OrderStatus

## Description

Represents the lifecycle stage of an `Order`.

## Implements

`DomainCatalog`

## Allowed Values

| Code            | Name                  | Description                                                          |
| --------------- | --------------------- | -------------------------------------------------------------------- |
| CART            | Cart                  | Provisional product selection, not yet confirmed.                    |
| PENDING_PAYMENT | Pending Payment       | Awaiting financial confirmation.                                     |
| PAID            | Paid                  | Fulfillment process begins after payment validation.                 |
| SHIPPED         | Shipped               | The order has physically left the warehouse.                         |
| DELIVERED       | Delivered / Finalized | Successful completion of the delivery. Cannot be modified afterward. |

## Allowed Transitions

```text
PENDING_PAYMENT ──markPaid()──────> PAID
PAID            ──markShipped()───> SHIPPED
SHIPPED         ──markDelivered()─> DELIVERED (finalized)
PAID            ──markDelivered()─> DELIVERED (finalized; only orders without physical items)
```

Digital products are delivered immediately after payment: an order that only contains digital products goes directly from `PAID` to `DELIVERED`.

Any other transition raises `InvalidOrderStatusTransitionException`. Any modification of a `DELIVERED` order raises `OrderAlreadyFinalizedException`.

---

# ShipmentStatus

## Description

Represents the logistics status of a `Shipment` associated with an order containing physical products.

## Implements

`DomainCatalog`

## Allowed Values

| Code       | Name       | Description                                           |
| ---------- | ---------- | ----------------------------------------------------- |
| PREPARING  | Preparing  | The shipment is being packed at the origin warehouse. |
| PACKED     | Packed     | The shipment is packed and ready to be dispatched.    |
| IN_TRANSIT | In Transit | The shipment is in transport toward its destination.  |
| DELIVERED  | Delivered  | The shipment was successfully delivered.              |

## Allowed Transitions

```text
PREPARING  ──markPacked()─────> PACKED
PACKED     ──markInTransit()──> IN_TRANSIT
IN_TRANSIT ──markDelivered()──> DELIVERED
```

Any other transition raises `InvalidStatusTransitionException`.

---

# ReturnStatus

## Description

Represents the status of a `ReturnRequest` process.

## Implements

`DomainCatalog`

## Allowed Values

| Code      | Name      | Description                                               |
| --------- | --------- | --------------------------------------------------------- |
| REQUESTED | Requested | The return was registered by the buyer.                   |
| APPROVED  | Approved  | The return was validated and triggers inventory re-entry. |
| REJECTED  | Rejected  | The return did not meet the required conditions.         |

## Allowed Transitions

```text
REQUESTED ──approve()──> APPROVED
REQUESTED ──reject()───> REJECTED
```

Any other transition raises `InvalidStatusTransitionException`.

---

# RefundStatus

## Description

Represents the status of a `Refund` process derived from an approved return.

## Implements

`DomainCatalog`

## Allowed Values

| Code      | Name      | Description                                      |
| --------- | --------- | ------------------------------------------------ |
| PENDING   | Pending   | The refund was generated and is being processed. |
| PROCESSED | Processed | The amount was restituted to the buyer.          |
| REJECTED  | Rejected  | The refund did not proceed.                      |

## Allowed Transitions

```text
PENDING ──markProcessed()──> PROCESSED
PENDING ──reject()─────────> REJECTED
```

Any other transition raises `InvalidStatusTransitionException`.

---

# PaymentResult

## Description

Result returned by `PaymentGatewayPort.processPayment(order)` when the payment of an order is processed.

## Implements

`DomainCatalog`

## Allowed Values

| Code     | Name     | Description |
| -------- | -------- | ----------- |
| APPROVED | Approved | The payment gateway approved the payment. |
| REJECTED | Rejected | The payment gateway rejected the payment. |

---

# AuditAction

## Description

Classifies the business action recorded by an `AuditLog`. Every service that changes information registers one or more of these actions.

## Implements

`DomainCatalog`

## Allowed Values

| Code                               | Name                               | Description |
| ---------------------------------- | ---------------------------------- | ----------- |
| USER_REGISTERED                    | User Registered                    | A buyer self-registered in the Marketplace. |
| STAFF_USER_REGISTERED              | Staff User Registered              | An administrator registered a logistics operator or a supervisor. |
| USER_LOGGED_IN                     | User Logged In                     | A user authenticated successfully. |
| USER_LOGGED_OUT                    | User Logged Out                    | A user logged out and its previously issued tokens were invalidated. |
| USER_STATUS_CHANGED                | User Status Changed                | The operational status of a user changed. |
| USER_PASSWORD_CHANGED              | User Password Changed              | A user changed its password. |
| SELLER_REGISTERED                  | Seller Registered                  | An administrator onboarded a seller. |
| SELLER_UPDATED                     | Seller Updated                     | The information of a seller changed. |
| BUYER_PROFILE_UPDATED              | Buyer Profile Updated              | A buyer updated its profile or addresses. |
| BUYER_COMMERCIAL_STATUS_CHANGED    | Buyer Commercial Status Changed    | The commercial status of a buyer changed. |
| WAREHOUSE_REGISTERED               | Warehouse Registered               | A warehouse was registered. |
| WAREHOUSE_UPDATED                  | Warehouse Updated                  | The information of a warehouse changed. |
| PRODUCT_REGISTERED                 | Product Registered                 | A seller registered a product. |
| PRODUCT_UPDATED                    | Product Updated                    | The commercial information of a product changed. |
| PRODUCT_PUBLISHED                  | Product Published                  | A product became visible in the public catalog. |
| PRODUCT_SUSPENDED                  | Product Suspended                  | A product was temporarily removed from the catalog. |
| PRODUCT_DISCONTINUED               | Product Discontinued               | A product was permanently withdrawn from the catalog. |
| INVENTORY_INITIAL_STOCK_REGISTERED | Inventory Initial Stock Registered | An inventory record was created with its initial stock. |
| INVENTORY_INBOUND_REGISTERED       | Inventory Inbound Registered       | Stock was added to an inventory record. |
| INVENTORY_ADJUSTED                 | Inventory Adjusted                 | The stock or condition of an inventory record was manually corrected. |
| INVENTORY_RESERVED                 | Inventory Reserved                 | Stock was reserved for an order in progress. |
| INVENTORY_RESERVATION_RELEASED     | Inventory Reservation Released     | Reserved stock returned to the available stock. |
| CART_ITEM_ADDED                    | Cart Item Added                    | A product was added to a cart. |
| CART_ITEM_REMOVED                  | Cart Item Removed                  | A product was removed from a cart. |
| CART_ITEM_QUANTITY_UPDATED         | Cart Item Quantity Updated         | The quantity of a cart line changed. |
| CART_CLEARED                       | Cart Cleared                       | Every item of a cart was removed. |
| ORDER_CONFIRMED                    | Order Confirmed                    | A cart was converted into an order. |
| PAYMENT_APPROVED                   | Payment Approved                   | The payment of an order was approved. |
| PAYMENT_REJECTED                   | Payment Rejected                   | The payment of an order was rejected. |
| ORDER_DELIVERED                    | Order Delivered                    | An order was delivered and finalized. |
| INVOICE_GENERATED                  | Invoice Generated                  | The invoice of an order was generated. |
| SHIPMENT_CREATED                   | Shipment Created                   | A shipment of an order was created. |
| SHIPMENT_PACKED                    | Shipment Packed                    | A shipment was packed. |
| SHIPMENT_DISPATCHED                | Shipment Dispatched                | A shipment left its origin warehouse. |
| SHIPMENT_DELIVERED                 | Shipment Delivered                 | A shipment was delivered. |
| RETURN_REQUESTED                   | Return Requested                   | A buyer requested the return of an order. |
| RETURN_APPROVED                    | Return Approved                    | A return request was approved. |
| RETURN_REJECTED                    | Return Rejected                    | A return request was rejected. |
| REFUND_CREATED                     | Refund Created                     | A refund was generated from an approved return. |
| REFUND_PROCESSED                   | Refund Processed                   | The amount of a refund was restituted to the buyer. |
| REPORT_CONSULTED                   | Report Consulted                   | An administrative report was consulted. |

---

# AuditEntityType

## Description

Classifies the type of entity affected by an audited operation (`AuditLog.entityType`).

## Implements

`DomainCatalog`

## Allowed Values

| Code           | Name           | Description |
| -------------- | -------------- | ----------- |
| USER           | User           | A user of any role. |
| SELLER         | Seller         | A seller. |
| BUYER          | Buyer          | A buyer. |
| WAREHOUSE      | Warehouse      | A warehouse. |
| PRODUCT        | Product        | A catalog product. |
| INVENTORY      | Inventory      | An inventory record (identified by product and warehouse). |
| CART           | Cart           | A shopping cart. |
| ORDER          | Order          | An order. |
| INVOICE        | Invoice        | An invoice. |
| SHIPMENT       | Shipment       | A shipment. |
| RETURN_REQUEST | Return Request | A return request. |
| REFUND         | Refund         | A refund. |
| REPORT         | Report         | An administrative report (identified by its report type). |

---

# Address

## Description

Represents a physical location used for order delivery or warehouse siting. It has no identity of its own; two addresses with the same values are considered equivalent.

Used by `Buyer.primaryAddress`, `Buyer.additionalAddresses`, `Warehouse.location` and `Shipment.deliveryAddress`.

## Attributes

| Attribute | Type   | Description                                              |
| --------- | ------ | -------------------------------------------------------- |
| line1     | String | Primary address line (street, number). Required.         |
| line2     | String | Complementary information. Optional (`null` if blank).   |
| city      | String | City of the address. Required.                           |
| state     | String | State, department, or region. Required.                  |
| country   | String | Country of the address. Required.                        |

## Validations

* `line1`, `city`, `state` and `country` cannot be null or blank → `InvalidAddressException`.

## Characteristics

* Immutable (`final` class, `final` fields, no setters).
* Compared by value (`equals` / `hashCode` over all attributes), not by identity.

---

# Variant

## Description

Represents a specific difference of a product, such as color, size, or model.

Used by `Product.variants`, `CartItem.variant` and `OrderItem.variant`.

## Attributes

| Attribute | Type   | Description                                                 |
| --------- | ------ | ----------------------------------------------------------- |
| type      | String | Category of the variant (e.g. Color, Size, Model). Required. |
| value     | String | Specific value of the variant (e.g. "Red", "M"). Required.   |

## Validations

* `type` and `value` cannot be null or blank → `InvalidVariantException`.

## Characteristics

* Immutable (`final` class, `final` fields, no setters).
* Compared by value (`equals` / `hashCode` over `type` and `value`), not by identity.

---

# ReportPeriod

## Description

Range of calendar days covered by a report. Both dates are **inclusive**: a period from `2026-10-01` to `2026-10-31` covers every moment of October 31.

Used by `CommercialReport`, `SellerPerformanceReport` and `LogisticsReport`, and received by `ConsultCommercialReportService`, `ConsultSellerPerformanceReportService` and `ConsultLogisticsReportService`.

## Attributes

| Attribute | Type      | Description                         |
| --------- | --------- | ----------------------------------- |
| startDate | LocalDate | First day of the period. Required.  |
| endDate   | LocalDate | Last day of the period. Required.   |

## Behavior

| Method                   | Description                                                                         |
| ------------------------ | ----------------------------------------------------------------------------------- |
| `startDateTime()`        | Inclusive lower bound: `startDate` at `00:00`.                                      |
| `endDateTimeExclusive()` | Exclusive upper bound: the day after `endDate` at `00:00`.                          |
| `contains(LocalDateTime moment)` | `true` when `startDateTime() <= moment < endDateTimeExclusive()`.           |

Every report converts the period with these two methods, and the `findBy<Date>Between(from, toExclusive)` methods of the output ports use the same half-open interval `[from, toExclusive)` (see [Output Ports](Output-ports.md)).

## Validations

* `startDate` and `endDate` are required → `InvalidReportPeriodException`.
* `startDate` cannot be after `endDate` → `InvalidReportPeriodException` (a single-day period, `startDate == endDate`, is valid).

## Characteristics

* Immutable (`final` class, `final` fields, no setters).
* Compared by value (`equals` / `hashCode` over `startDate` and `endDate`), not by identity.
