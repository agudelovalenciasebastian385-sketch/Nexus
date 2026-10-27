# Output Ports — NexusMarket

## Introduction

Output ports define the contracts through which the domain communicates with external resources. The domain owns these interfaces (`nexusmarket.domain.ports.out`); their implementations belong to the adapter layer (MySQL, MongoDB, password hashing, JWT and a simulated payment gateway) and are out of the scope of this phase.

Domain services must never depend directly on MySQL, MongoDB, JPA, Spring Data, HTTP, REST, JWT or hashing libraries.

## Parameter Rule

* Methods that store information receive Domain Models (`save(Order order)`, `update(Order order)`).
* Lookups of an aggregate receive its identifier (`findById(String id)`), because NexusMarket entities validate their invariants on construction and cannot be built with only an identifier.
* Queries by relationship receive the related Domain Model (`findByBuyer(Buyer buyer)`).
* Date-range queries (`findBy<Date>Between(LocalDateTime from, LocalDateTime toExclusive)`) use the half-open interval `[from, toExclusive)`: the lower bound is **inclusive** and the upper bound is **exclusive**. Reports obtain both bounds from `ReportPeriod.startDateTime()` and `ReportPeriod.endDateTimeExclusive()` (the first moment of the day after the end date), so the end date is covered completely. Adapters must not translate them into an inclusive SQL BETWEEN (such as a Spring Data derived query ending in Between); they must use `>= from` and `< toExclusive` (Spring Data keywords GreaterThanEqual and LessThan).

## Dependency Flow

```text
Domain Service
      |
      v
Output Port (domain/ports/out)
      |
      v
Output Adapter (adapters/out)
      |
      v
External resource (MySQL, MongoDB, hashing, JWT, payment gateway)
```

---

# 1. UserRepositoryPort

## Responsibility

Persistence and queries of every `User` (any subclass); uniqueness checks of email and identification number.

## Methods

```java
public interface UserRepositoryPort {

    User save(User user);
    void update(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByIdentificationNumber(String identificationNumber);
}
```

## Consumers

* `ChangeUserPasswordService`
* `ChangeUserStatusService`
* `ConsultAuditLogsService`
* `ConsultUserService`
* `LoadAuthenticatedUserService`
* `LoginService`
* `LogoutService`
* `RegisterBuyerUserService`
* `RegisterSellerService`
* `RegisterStaffUserService`
* `ValidateUserStatusService`

---

# 2. BuyerRepositoryPort

## Responsibility

Persistence and queries of `Buyer`.

## Methods

```java
public interface BuyerRepositoryPort {

    Buyer save(Buyer buyer);
    void update(Buyer buyer);
    Optional<Buyer> findById(String id);
}
```

## Consumers

* `AddItemToCartService`
* `ChangeBuyerCommercialStatusService`
* `ClearCartService`
* `ConfirmOrderService`
* `ConsultBuyerOrdersService`
* `ConsultBuyerService`
* `ConsultCartService`
* `RegisterBuyerUserService`
* `RemoveItemFromCartService`
* `UpdateBuyerProfileService`
* `UpdateCartItemQuantityService`

---

# 3. SellerRepositoryPort

## Responsibility

Persistence and queries of `Seller`.

## Methods

```java
public interface SellerRepositoryPort {

    Seller save(Seller seller);
    void update(Seller seller);
    Optional<Seller> findById(String id);
}
```

## Consumers

* `ConsultSellerPerformanceReportService`
* `ConsultSellerProductsService`
* `ConsultSellerService`
* `ConsultWarehouseService`
* `RegisterProductService`
* `RegisterSellerService`
* `RegisterWarehouseService`
* `UpdateSellerService`

---

# 4. WarehouseRepositoryPort

## Responsibility

Persistence and queries of `Warehouse`.

## Methods

```java
public interface WarehouseRepositoryPort {

    Warehouse save(Warehouse warehouse);
    void update(Warehouse warehouse);
    Optional<Warehouse> findById(String id);
    List<Warehouse> findAll();
    List<Warehouse> findBySeller(Seller seller);
}
```

## Consumers

* `ConsultInventoryReportService`
* `ConsultInventoryService`
* `ConsultWarehouseService`
* `RegisterInitialStockService`
* `RegisterInventoryAdjustmentService`
* `RegisterInventoryInboundService`
* `RegisterSellerService`
* `RegisterWarehouseService`
* `ReleaseInventoryReservationService`
* `ReserveInventoryService`
* `UpdateWarehouseService`

---

# 5. ProductRepositoryPort

## Responsibility

Persistence and queries of `Product` (physical and digital).

## Methods

```java
public interface ProductRepositoryPort {

    Product save(Product product);
    void update(Product product);
    Optional<Product> findById(String id);
    List<Product> findByStatus(ProductStatus status);
    List<Product> findBySeller(Seller seller);
}
```

## Consumers

* `AddItemToCartService`
* `ConfirmOrderService`
* `ConsultCatalogService`
* `ConsultInventoryService`
* `ConsultSellerPerformanceReportService`
* `ConsultSellerProductsService`
* `DiscontinueProductService`
* `PublishProductService`
* `RegisterInitialStockService`
* `RegisterInventoryAdjustmentService`
* `RegisterInventoryInboundService`
* `RegisterProductService`
* `ReleaseInventoryReservationService`
* `RemoveItemFromCartService`
* `ReserveInventoryService`
* `SuspendProductService`
* `UpdateCartItemQuantityService`
* `UpdateProductService`

---

# 6. InventoryRepositoryPort

## Responsibility

Persistence and queries of `Inventory` records (identified by product and warehouse), including their movements.

## Methods

```java
public interface InventoryRepositoryPort {

    Inventory save(Inventory inventory);
    void update(Inventory inventory);
    Optional<Inventory> findByProductAndWarehouse(PhysicalProduct product, Warehouse warehouse);
    List<Inventory> findByProduct(PhysicalProduct product);
    List<Inventory> findByWarehouse(Warehouse warehouse);
    List<Inventory> findAll();
}
```

## Consumers

* `ApproveReturnService`
* `ConfirmOrderService`
* `ConsultInventoryReportService`
* `ConsultInventoryService`
* `DispatchShipmentService`
* `RegisterInitialStockService`
* `RegisterInventoryAdjustmentService`
* `RegisterInventoryInboundService`
* `ReleaseInventoryReservationService`
* `ReserveInventoryService`

---

# 7. CartRepositoryPort

## Responsibility

Persistence and queries of `Cart` and its items.

## Methods

```java
public interface CartRepositoryPort {

    Cart save(Cart cart);
    void update(Cart cart);
    Optional<Cart> findByBuyer(Buyer buyer);
}
```

## Consumers

* `AddItemToCartService`
* `ClearCartService`
* `ConfirmOrderService`
* `ConsultCartService`
* `RegisterBuyerUserService`
* `RemoveItemFromCartService`
* `UpdateCartItemQuantityService`

---

# 8. OrderRepositoryPort

## Responsibility

Persistence and queries of `Order` and its items.

## Methods

```java
public interface OrderRepositoryPort {

    Order save(Order order);
    void update(Order order);
    Optional<Order> findById(String id);
    List<Order> findByBuyer(Buyer buyer);
    List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime toExclusive);
}
```

## Consumers

* `ConfirmDeliveryService`
* `ConfirmOrderService`
* `ConsultBuyerOrdersService`
* `ConsultCommercialReportService`
* `ConsultInvoiceService`
* `ConsultOrderService`
* `ConsultSellerPerformanceReportService`
* `CreateShipmentsService`
* `DispatchShipmentService`
* `RequestReturnService`
* `ValidatePaymentService`

---

# 9. InvoiceRepositoryPort

## Responsibility

Persistence and queries of `Invoice`.

## Methods

```java
public interface InvoiceRepositoryPort {

    Invoice save(Invoice invoice);
    Optional<Invoice> findByOrder(Order order);
    boolean existsByOrder(Order order);
    List<Invoice> findByIssuedAtBetween(LocalDateTime from, LocalDateTime toExclusive);
}
```

## Consumers

* `ApproveReturnService`
* `ConsultCommercialReportService`
* `ConsultInvoiceService`
* `GenerateInvoiceService`

---

# 10. ShipmentRepositoryPort

## Responsibility

Persistence and queries of `Shipment`.

## Methods

```java
public interface ShipmentRepositoryPort {

    Shipment save(Shipment shipment);
    void update(Shipment shipment);
    Optional<Shipment> findById(String id);
    List<Shipment> findByOrder(Order order);
    List<Shipment> findByCreatedAtBetween(LocalDateTime from, LocalDateTime toExclusive);
}
```

## Consumers

* `ConfirmDeliveryService`
* `ConsultLogisticsReportService`
* `CreateShipmentsService`
* `DispatchShipmentService`
* `PackShipmentService`

---

# 11. ReturnRequestRepositoryPort

## Responsibility

Persistence and queries of `ReturnRequest`.

## Methods

```java
public interface ReturnRequestRepositoryPort {

    ReturnRequest save(ReturnRequest returnRequest);
    void update(ReturnRequest returnRequest);
    Optional<ReturnRequest> findById(String id);
    List<ReturnRequest> findByOrder(Order order);
}
```

## Consumers

* `ApproveReturnService`
* `ConsultReturnService`
* `ConsultSellerPerformanceReportService`
* `RejectReturnService`
* `RequestReturnService`

---

# 12. RefundRepositoryPort

## Responsibility

Persistence and queries of `Refund`.

## Methods

```java
public interface RefundRepositoryPort {

    Refund save(Refund refund);
    void update(Refund refund);
    Optional<Refund> findById(String id);
    Optional<Refund> findByReturnRequest(ReturnRequest returnRequest);
    List<Refund> findByProcessedAtBetween(LocalDateTime from, LocalDateTime toExclusive);
}
```

## Consumers

* `ApproveReturnService`
* `ConsultCommercialReportService`
* `ConsultReturnService`
* `ProcessRefundService`

---

# 13. AuditLogRepositoryPort

## Responsibility

Append-only persistence and queries of `AuditLog` records (intended for MongoDB).

## Methods

```java
public interface AuditLogRepositoryPort {

    AuditLog save(AuditLog auditLog);
    List<AuditLog> findAll();
    List<AuditLog> findByPerformedBy(User user);
    List<AuditLog> findByEntity(AuditEntityType entityType, String entityId);
}
```

## Consumers

* `ConsultAuditLogsService`
* `RegisterAuditLogService`

---

# 14. PasswordServicePort

## Responsibility

Password encryption and verification. The domain never depends on a hashing library.

## Methods

```java
public interface PasswordServicePort {

    String encrypt(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
```

## Consumers

* `ChangeUserPasswordService`
* `LoginService`
* `RegisterBuyerUserService`
* `RegisterSellerService`
* `RegisterStaffUserService`

---

# 15. JwtServicePort

## Responsibility

Token generation and validation. The domain never depends on a JWT library.

## Methods

```java
public interface JwtServicePort {

    String generateToken(User user);
    boolean validateToken(String token);
    String extractUserId(String token);
    Integer extractTokenVersion(String token);
    User reconstructUser(String token);
}
```

## Consumers

* `LoginService`

---

# 16. PaymentGatewayPort

## Responsibility

Processing of order payments through an external payment gateway (simulated by an adapter in this phase).

## Methods

```java
public interface PaymentGatewayPort {

    PaymentResult processPayment(Order order);
}
```

## Consumers

* `ValidatePaymentService`

---
