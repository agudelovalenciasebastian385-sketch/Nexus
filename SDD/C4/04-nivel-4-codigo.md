# C4 Nivel 4 — Código

## Propósito

Mostrar las clases que colaboran en cada recorrido, con sus nombres reales. Son diagramas simplificados: solo incluyen los miembros que explican el recorrido. El inventario completo está en las fichas de cada capa.

## Del controlador al adaptador: pagar un pedido

```mermaid
classDiagram
    class BuyerRestController {
        -BuyerPort buyerPort
        +payOrder(User user, String orderId) ResponseEntity
    }
    class BuyerPort {
        <<interface>>
        +validatePayment(User requestingUser, String orderId) Order
    }
    class BuyerUseCaseImpl {
        -ValidatePaymentService validatePaymentService
        +validatePayment(User requestingUser, String orderId) Order
    }
    class ValidatePaymentService {
        +validatePayment(User requestingUser, String orderId) Order
    }
    class OrderRepositoryPort {
        <<interface>>
        +findById(String id) Optional
        +update(Order order) void
    }
    class PaymentGatewayPort {
        <<interface>>
        +processPayment(Order order) PaymentResult
    }
    class OrderJpaAdapter
    class SimulatedPaymentGatewayAdapter
    class GenerateInvoiceService {
        +generateInvoice(Order order, User performedBy) Invoice
    }
    class RegisterAuditLogService {
        +execute(User performedBy, AuditAction action, AuditEntityType entityType, String entityId, Map details) AuditLog
    }
    BuyerRestController --> BuyerPort
    BuyerUseCaseImpl ..|> BuyerPort
    BuyerUseCaseImpl --> ValidatePaymentService
    ValidatePaymentService --> OrderRepositoryPort
    ValidatePaymentService --> PaymentGatewayPort
    ValidatePaymentService --> GenerateInvoiceService
    ValidatePaymentService --> RegisterAuditLogService
    OrderJpaAdapter ..|> OrderRepositoryPort
    SimulatedPaymentGatewayAdapter ..|> PaymentGatewayPort
```

## Seguridad

```mermaid
classDiagram
    class SecurityConfig {
        +securityFilterChain(HttpSecurity http, JwtServicePort jwtServicePort, LoadAuthenticatedUserService loadAuthenticatedUserService, HandlerExceptionResolver exceptionResolver) SecurityFilterChain
        +passwordEncoder() PasswordEncoder
        +corsConfigurationSource(List allowedOrigins) CorsConfigurationSource
    }
    class JwtAuthenticationFilter {
        #doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) void
    }
    class JwtServicePort {
        <<interface>>
        +generateToken(User user) String
        +validateToken(String token) boolean
        +extractUserId(String token) String
        +extractTokenVersion(String token) Integer
    }
    class JwtProvider
    class JwtClaimsMapper
    class JwtClaimsDTO
    class LoadAuthenticatedUserService {
        +loadById(String userId) User
    }
    class AuthenticatedUserPrincipal {
        -User user
        +getAuthorities() Collection
    }
    SecurityConfig ..> JwtAuthenticationFilter : crea
    JwtAuthenticationFilter --> JwtServicePort
    JwtAuthenticationFilter --> LoadAuthenticatedUserService
    JwtAuthenticationFilter ..> AuthenticatedUserPrincipal : crea
    JwtProvider ..|> JwtServicePort
    JwtProvider --> JwtClaimsMapper
    JwtClaimsMapper ..> JwtClaimsDTO
```

## Jerarquía de usuarios y productos

```mermaid
classDiagram
    class User {
        <<abstract>>
        -String id
        -String email
        -String password
        -SystemRole role
        -UserStatus status
        -Integer authTokenVersion
        +isActive() boolean
        +bumpAuthTokenVersion() void
        +normalizeEmail(String email) String
    }
    class Buyer {
        -CommercialStatus commercialStatus
        +canPurchase() boolean
    }
    class Seller
    class LogisticsOperator
    class Administrator
    class Supervisor
    class Product {
        <<abstract>>
        -String id
        -BigDecimal price
        -ProductStatus status
        +isPublished() boolean
    }
    class PhysicalProduct
    class DigitalProduct
    User <|-- Buyer
    User <|-- Seller
    User <|-- LogisticsOperator
    User <|-- Administrator
    User <|-- Supervisor
    Product <|-- PhysicalProduct
    Product <|-- DigitalProduct
    Seller "1" --> "*" Product : publica
```

## Relaciones del pedido

```mermaid
classDiagram
    class Order {
        -OrderStatus status
        -LocalDateTime createdAt
        +markPaid() void
        +markShipped() void
        +markDelivered() void
        +hasPhysicalItems() boolean
        +totalAmount() BigDecimal
        +isFinalized() boolean
    }
    class OrderItem
    class Invoice
    class Shipment {
        -ShipmentStatus status
        +markInTransit() void
        +markDelivered() void
    }
    class ReturnRequest {
        -ReturnStatus status
        +approve() void
    }
    class Refund {
        -RefundStatus status
        +markProcessed(LocalDateTime processedAt) void
    }
    class Inventory {
        -int quantity
        -int reservedQuantity
        +canReserve(int quantity) boolean
        +registerMovement(MovementType type, int quantity, LocalDateTime date) InventoryMovement
    }
    class InventoryMovement
    class Warehouse
    class Buyer
    class Product
    Buyer "1" --> "*" Order
    Order "1" *-- "1..*" OrderItem
    Order "1" --> "0..1" Invoice
    Order "1" --> "*" Shipment
    OrderItem --> Product
    OrderItem --> "0..1" Warehouse : origen
    Shipment --> Warehouse : origen
    ReturnRequest --> Order
    Refund --> ReturnRequest
    Inventory --> Warehouse
    Inventory "1" *-- "*" InventoryMovement
```
