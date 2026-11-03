# Flujos por endpoint

La matriz se genera desde los controladores y los casos de uso, y se compara con [Api-rest-endpoints.md](../Adapters/Api-rest-endpoints.md). Los diagramas de secuencia muestran el camino feliz; los errores están en la tabla final.

## 1. Matriz de los 108 endpoints

<!-- MATRIZ-INICIO: la genera tools/gen_c4_matrix.py -->

### Autenticación — `AuthRestController`, `PublicAccessPort`

| Método | Ruta | Método del puerto | Servicio | OK |
| --- | --- | --- | --- | ---: |
| `POST` | `/api/v1/auth/login` | `login` | `LoginService` | 200 |
| `POST` | `/api/v1/auth/logout` | `logout` | `LogoutService` | 204 |
| `POST` | `/api/v1/auth/register/buyers` | `registerBuyerUser` | `RegisterBuyerUserService` | 201 |

### Público — `PublicRestController`, `PublicAccessPort`

| Método | Ruta | Método del puerto | Servicio | OK |
| --- | --- | --- | --- | ---: |
| `GET` | `/api/v1/public/products` | `consultCatalog` | `ConsultCatalogService` | 200 |
| `GET` | `/api/v1/public/products/{productId}` | `consultCatalogProduct` | `ConsultCatalogService` | 200 |

### Comprador — `BuyerRestController`, `BuyerPort`

| Método | Ruta | Método del puerto | Servicio | OK |
| --- | --- | --- | --- | ---: |
| `GET` | `/api/v1/buyer/account` | `consultUser` | `ConsultUserService` | 200 |
| `PUT` | `/api/v1/buyer/account/password` | `changeUserPassword` | `ChangeUserPasswordService` | 204 |
| `GET` | `/api/v1/buyer/profile` | `consultBuyer` | `ConsultBuyerService` | 200 |
| `PUT` | `/api/v1/buyer/profile` | `updateBuyerProfile` | `UpdateBuyerProfileService` | 200 |
| `GET` | `/api/v1/buyer/cart` | `consultCart` | `ConsultCartService` | 200 |
| `POST` | `/api/v1/buyer/cart/items` | `addItemToCart` | `AddItemToCartService` | 200 |
| `PATCH` | `/api/v1/buyer/cart/items/{productId}` | `updateCartItemQuantity` | `UpdateCartItemQuantityService` | 200 |
| `DELETE` | `/api/v1/buyer/cart/items/{productId}` | `removeItemFromCart` | `RemoveItemFromCartService` | 200 |
| `DELETE` | `/api/v1/buyer/cart/items` | `clearCart` | `ClearCartService` | 200 |
| `POST` | `/api/v1/buyer/orders` | `confirmOrder` | `ConfirmOrderService` | 201 |
| `POST` | `/api/v1/buyer/orders/{orderId}/payment` | `validatePayment` | `ValidatePaymentService` | 200 |
| `GET` | `/api/v1/buyer/orders` | `consultBuyerOrders` | `ConsultBuyerOrdersService` | 200 |
| `GET` | `/api/v1/buyer/orders/{orderId}` | `consultOrder` | `ConsultOrderService` | 200 |
| `GET` | `/api/v1/buyer/orders/{orderId}/invoice` | `consultInvoice` | `ConsultInvoiceService` | 200 |
| `POST` | `/api/v1/buyer/orders/{orderId}/returns` | `requestReturn` | `RequestReturnService` | 201 |
| `GET` | `/api/v1/buyer/returns/{returnRequestId}` | `consultReturn` | `ConsultReturnService` | 200 |
| `GET` | `/api/v1/buyer/returns/{returnRequestId}/refund` | `consultRefund` | `ConsultReturnService` | 200 |

### Vendedor — `SellerRestController`, `SellerPort`

| Método | Ruta | Método del puerto | Servicio | OK |
| --- | --- | --- | --- | ---: |
| `GET` | `/api/v1/seller/account` | `consultUser` | `ConsultUserService` | 200 |
| `PUT` | `/api/v1/seller/account/password` | `changeUserPassword` | `ChangeUserPasswordService` | 204 |
| `GET` | `/api/v1/seller/profile` | `consultSeller` | `ConsultSellerService` | 200 |
| `PATCH` | `/api/v1/seller/profile` | `updateSeller` | `UpdateSellerService` | 200 |
| `POST` | `/api/v1/seller/warehouses` | `registerWarehouse` | `RegisterWarehouseService` | 201 |
| `GET` | `/api/v1/seller/warehouses` | `consultWarehouses` | `ConsultWarehouseService` | 200 |
| `GET` | `/api/v1/seller/warehouses/{warehouseId}` | `consultWarehouse` | `ConsultWarehouseService` | 200 |
| `PUT` | `/api/v1/seller/warehouses/{warehouseId}` | `updateWarehouse` | `UpdateWarehouseService` | 200 |
| `POST` | `/api/v1/seller/products` | `registerProduct` | `RegisterProductService` | 201 |
| `GET` | `/api/v1/seller/products` | `consultSellerProducts` | `ConsultSellerProductsService` | 200 |
| `PUT` | `/api/v1/seller/products/{productId}` | `updateProduct` | `UpdateProductService` | 200 |
| `PATCH` | `/api/v1/seller/products/{productId}/publish` | `publishProduct` | `PublishProductService` | 200 |
| `PATCH` | `/api/v1/seller/products/{productId}/suspend` | `suspendProduct` | `SuspendProductService` | 200 |
| `PATCH` | `/api/v1/seller/products/{productId}/discontinue` | `discontinueProduct` | `DiscontinueProductService` | 200 |
| `POST` | `/api/v1/seller/inventories` | `registerInitialStock` | `RegisterInitialStockService` | 201 |
| `POST` | `/api/v1/seller/inventories/{productId}/{warehouseId}/inbounds` | `registerInventoryInbound` | `RegisterInventoryInboundService` | 201 |
| `GET` | `/api/v1/seller/products/{productId}/inventories` | `consultProductInventory` | `ConsultInventoryService` | 200 |
| `GET` | `/api/v1/seller/warehouses/{warehouseId}/inventories` | `consultWarehouseInventory` | `ConsultInventoryService` | 200 |
| `GET` | `/api/v1/seller/orders/{orderId}` | `consultOrder` | `ConsultOrderService` | 200 |
| `GET` | `/api/v1/seller/reports/performance` | `consultSellerPerformanceReport` | `ConsultSellerPerformanceReportService` | 200 |

### Operador logístico — `LogisticsOperatorRestController`, `LogisticsOperatorPort`

| Método | Ruta | Método del puerto | Servicio | OK |
| --- | --- | --- | --- | ---: |
| `GET` | `/api/v1/logistics/account` | `consultUser` | `ConsultUserService` | 200 |
| `PUT` | `/api/v1/logistics/account/password` | `changeUserPassword` | `ChangeUserPasswordService` | 204 |
| `GET` | `/api/v1/logistics/warehouses` | `consultWarehouses` | `ConsultWarehouseService` | 200 |
| `GET` | `/api/v1/logistics/warehouses/{warehouseId}` | `consultWarehouse` | `ConsultWarehouseService` | 200 |
| `POST` | `/api/v1/logistics/inventories` | `registerInitialStock` | `RegisterInitialStockService` | 201 |
| `POST` | `/api/v1/logistics/inventories/{productId}/{warehouseId}/inbounds` | `registerInventoryInbound` | `RegisterInventoryInboundService` | 201 |
| `POST` | `/api/v1/logistics/inventories/{productId}/{warehouseId}/adjustments` | `registerInventoryAdjustment` | `RegisterInventoryAdjustmentService` | 201 |
| `POST` | `/api/v1/logistics/inventories/{productId}/{warehouseId}/reservations` | `reserveInventory` | `ReserveInventoryService` | 201 |
| `POST` | `/api/v1/logistics/inventories/{productId}/{warehouseId}/reservation-releases` | `releaseInventoryReservation` | `ReleaseInventoryReservationService` | 201 |
| `GET` | `/api/v1/logistics/products/{productId}/inventories` | `consultProductInventory` | `ConsultInventoryService` | 200 |
| `GET` | `/api/v1/logistics/warehouses/{warehouseId}/inventories` | `consultWarehouseInventory` | `ConsultInventoryService` | 200 |
| `GET` | `/api/v1/logistics/orders/{orderId}` | `consultOrder` | `ConsultOrderService` | 200 |
| `POST` | `/api/v1/logistics/orders/{orderId}/shipments` | `createShipments` | `CreateShipmentsService` | 201 |
| `PATCH` | `/api/v1/logistics/shipments/{shipmentId}/pack` | `packShipment` | `PackShipmentService` | 200 |
| `PATCH` | `/api/v1/logistics/shipments/{shipmentId}/dispatch` | `dispatchShipment` | `DispatchShipmentService` | 200 |
| `PATCH` | `/api/v1/logistics/shipments/{shipmentId}/deliver` | `confirmDelivery` | `ConfirmDeliveryService` | 200 |
| `GET` | `/api/v1/logistics/reports/inventory` | `consultInventoryReport` | `ConsultInventoryReportService` | 200 |

### Administrador — `AdministratorRestController`, `AdministratorPort`

| Método | Ruta | Método del puerto | Servicio | OK |
| --- | --- | --- | --- | ---: |
| `PUT` | `/api/v1/admin/account/password` | `changeUserPassword` | `ChangeUserPasswordService` | 204 |
| `POST` | `/api/v1/admin/staff-users` | `registerStaffUser` | `RegisterStaffUserService` | 201 |
| `GET` | `/api/v1/admin/users/{userId}` | `consultUser` | `ConsultUserService` | 200 |
| `PATCH` | `/api/v1/admin/users/{userId}/status` | `changeUserStatus` | `ChangeUserStatusService` | 200 |
| `POST` | `/api/v1/admin/sellers` | `registerSeller` | `RegisterSellerService` | 201 |
| `GET` | `/api/v1/admin/sellers/{sellerId}` | `consultSeller` | `ConsultSellerService` | 200 |
| `PATCH` | `/api/v1/admin/sellers/{sellerId}` | `updateSeller` | `UpdateSellerService` | 200 |
| `GET` | `/api/v1/admin/sellers/{sellerId}/products` | `consultSellerProducts` | `ConsultSellerProductsService` | 200 |
| `GET` | `/api/v1/admin/buyers/{buyerId}` | `consultBuyer` | `ConsultBuyerService` | 200 |
| `PATCH` | `/api/v1/admin/buyers/{buyerId}/commercial-status` | `changeBuyerCommercialStatus` | `ChangeBuyerCommercialStatusService` | 200 |
| `POST` | `/api/v1/admin/warehouses` | `registerWarehouse` | `RegisterWarehouseService` | 201 |
| `GET` | `/api/v1/admin/warehouses` | `consultWarehouses` | `ConsultWarehouseService` | 200 |
| `GET` | `/api/v1/admin/warehouses/{warehouseId}` | `consultWarehouse` | `ConsultWarehouseService` | 200 |
| `PUT` | `/api/v1/admin/warehouses/{warehouseId}` | `updateWarehouse` | `UpdateWarehouseService` | 200 |
| `GET` | `/api/v1/admin/orders/{orderId}/invoice` | `consultInvoice` | `ConsultInvoiceService` | 200 |
| `GET` | `/api/v1/admin/returns/{returnRequestId}` | `consultReturn` | `ConsultReturnService` | 200 |
| `PATCH` | `/api/v1/admin/returns/{returnRequestId}/approve` | `approveReturn` | `ApproveReturnService` | 200 |
| `PATCH` | `/api/v1/admin/returns/{returnRequestId}/reject` | `rejectReturn` | `RejectReturnService` | 200 |
| `GET` | `/api/v1/admin/returns/{returnRequestId}/refund` | `consultRefund` | `ConsultReturnService` | 200 |
| `PATCH` | `/api/v1/admin/refunds/{refundId}/process` | `processRefund` | `ProcessRefundService` | 200 |
| `GET` | `/api/v1/admin/audit-logs` | `consultAuditLogs` | `ConsultAuditLogsService` | 200 |
| `GET` | `/api/v1/admin/audit-logs/users/{userId}` | `consultAuditLogsByUser` | `ConsultAuditLogsService` | 200 |
| `GET` | `/api/v1/admin/audit-logs/entities/{entityType}/{entityId}` | `consultAuditLogsByEntity` | `ConsultAuditLogsService` | 200 |
| `GET` | `/api/v1/admin/reports/commercial` | `consultCommercialReport` | `ConsultCommercialReportService` | 200 |
| `GET` | `/api/v1/admin/reports/sellers/{sellerId}/performance` | `consultSellerPerformanceReport` | `ConsultSellerPerformanceReportService` | 200 |
| `GET` | `/api/v1/admin/reports/inventory` | `consultInventoryReport` | `ConsultInventoryReportService` | 200 |
| `GET` | `/api/v1/admin/reports/logistics` | `consultLogisticsReport` | `ConsultLogisticsReportService` | 200 |

### Supervisor — `SupervisorRestController`, `SupervisorPort`

| Método | Ruta | Método del puerto | Servicio | OK |
| --- | --- | --- | --- | ---: |
| `PUT` | `/api/v1/supervisor/account/password` | `changeUserPassword` | `ChangeUserPasswordService` | 204 |
| `GET` | `/api/v1/supervisor/users/{userId}` | `consultUser` | `ConsultUserService` | 200 |
| `GET` | `/api/v1/supervisor/sellers/{sellerId}` | `consultSeller` | `ConsultSellerService` | 200 |
| `GET` | `/api/v1/supervisor/buyers/{buyerId}` | `consultBuyer` | `ConsultBuyerService` | 200 |
| `GET` | `/api/v1/supervisor/warehouses` | `consultWarehouses` | `ConsultWarehouseService` | 200 |
| `GET` | `/api/v1/supervisor/warehouses/{warehouseId}` | `consultWarehouse` | `ConsultWarehouseService` | 200 |
| `GET` | `/api/v1/supervisor/sellers/{sellerId}/products` | `consultSellerProducts` | `ConsultSellerProductsService` | 200 |
| `GET` | `/api/v1/supervisor/products/{productId}/inventories` | `consultProductInventory` | `ConsultInventoryService` | 200 |
| `GET` | `/api/v1/supervisor/warehouses/{warehouseId}/inventories` | `consultWarehouseInventory` | `ConsultInventoryService` | 200 |
| `GET` | `/api/v1/supervisor/buyers/{buyerId}/cart` | `consultCart` | `ConsultCartService` | 200 |
| `GET` | `/api/v1/supervisor/buyers/{buyerId}/orders` | `consultBuyerOrders` | `ConsultBuyerOrdersService` | 200 |
| `GET` | `/api/v1/supervisor/orders/{orderId}` | `consultOrder` | `ConsultOrderService` | 200 |
| `GET` | `/api/v1/supervisor/orders/{orderId}/invoice` | `consultInvoice` | `ConsultInvoiceService` | 200 |
| `GET` | `/api/v1/supervisor/returns/{returnRequestId}` | `consultReturn` | `ConsultReturnService` | 200 |
| `GET` | `/api/v1/supervisor/returns/{returnRequestId}/refund` | `consultRefund` | `ConsultReturnService` | 200 |
| `GET` | `/api/v1/supervisor/audit-logs` | `consultAuditLogs` | `ConsultAuditLogsService` | 200 |
| `GET` | `/api/v1/supervisor/audit-logs/users/{userId}` | `consultAuditLogsByUser` | `ConsultAuditLogsService` | 200 |
| `GET` | `/api/v1/supervisor/audit-logs/entities/{entityType}/{entityId}` | `consultAuditLogsByEntity` | `ConsultAuditLogsService` | 200 |
| `GET` | `/api/v1/supervisor/reports/commercial` | `consultCommercialReport` | `ConsultCommercialReportService` | 200 |
| `GET` | `/api/v1/supervisor/reports/sellers/{sellerId}/performance` | `consultSellerPerformanceReport` | `ConsultSellerPerformanceReportService` | 200 |
| `GET` | `/api/v1/supervisor/reports/inventory` | `consultInventoryReport` | `ConsultInventoryReportService` | 200 |
| `GET` | `/api/v1/supervisor/reports/logistics` | `consultLogisticsReport` | `ConsultLogisticsReportService` | 200 |

<!-- MATRIZ-FIN -->

## 2. Login

Normaliza el correo, compara la contraseña y emite el token. La misma respuesta `401` cubre el correo inexistente y la contraseña incorrecta, para no revelar qué correos están registrados.

```mermaid
sequenceDiagram
    actor C as Cliente
    participant R as AuthRestController
    participant P as PublicAccessPort
    participant S as LoginService
    participant U as UserRepositoryPort
    participant H as PasswordServicePort
    participant J as JwtServicePort
    participant A as RegisterAuditLogService
    C->>R: POST /api/v1/auth/login con LoginRequestDTO
    R->>P: login(email, password)
    P->>S: login(email, rawPassword)
    S->>U: findByEmail(normalizeEmail(email))
    U-->>S: User
    S->>H: matches(rawPassword, password)
    H-->>S: true
    S->>S: Comprueba isActive
    S->>J: generateToken(user)
    J-->>S: Token con sub, jti, ver, iat y exp
    S->>A: execute con USER_LOGGED_IN
    S-->>R: AuthenticationResult
    R-->>C: 200 LoginResponseDTO
```

## 3. Confirmar pedido

Convierte el carrito en un pedido `PENDING_PAYMENT`, reserva el stock de los productos físicos y vacía el carrito, todo en la misma transacción.

```mermaid
sequenceDiagram
    actor C as Comprador
    participant R as BuyerRestController
    participant U as BuyerUseCaseImpl
    participant S as ConfirmOrderService
    participant V as ValidateRoleAuthorizationService
    participant K as CartRepositoryPort
    participant I as InventoryRepositoryPort
    participant T as ReserveInventoryService
    participant O as OrderRepositoryPort
    participant A as RegisterAuditLogService
    C->>R: POST /api/v1/buyer/orders
    R->>U: confirmOrder(user)
    U->>S: confirmOrder(requestingUser)
    S->>V: execute(requestingUser, BUYER)
    S->>S: Comprueba canPurchase
    S->>K: findByBuyer(buyer)
    K-->>S: Cart con sus líneas
    loop Cada línea física
        S->>I: findByProduct(product)
        I-->>S: Inventory con stock suficiente
    end
    loop Cada inventario elegido
        S->>T: execute(inventory, quantity, buyer)
        T->>I: update(inventory) con RESERVATION
    end
    S->>O: save(order)
    S->>K: update(cart) ya vacío
    S->>A: execute con ORDER_CONFIRMED
    S-->>R: Order
    R-->>C: 201 OrderResponseDTO
```

## 4. Pagar

Un pago aprobado genera la factura. Si el pedido solo tiene productos digitales, queda entregado de inmediato. Un pago rechazado queda en la auditoría, gracias a `noRollbackFor`, y responde 422.

```mermaid
sequenceDiagram
    actor C as Comprador
    participant R as BuyerRestController
    participant S as ValidatePaymentService
    participant O as OrderRepositoryPort
    participant G as PaymentGatewayPort
    participant N as GenerateInvoiceService
    participant A as RegisterAuditLogService
    C->>R: POST /api/v1/buyer/orders/{orderId}/payment
    R->>S: validatePayment(user, orderId)
    S->>O: findById(orderId)
    O-->>S: Order
    S->>S: Valida propiedad y PENDING_PAYMENT
    S->>G: processPayment(order)
    alt Aprobado
        G-->>S: PaymentResult.APPROVED
        S->>S: markPaid
        S->>N: generateInvoice(order, requester)
        S->>A: execute con PAYMENT_APPROVED
        opt Sin productos físicos
            S->>S: markDelivered
            S->>A: execute con ORDER_DELIVERED
        end
        S->>O: update(order)
        S-->>R: Order
        R-->>C: 200 OrderResponseDTO
    else Rechazado
        G-->>S: PaymentResult.REJECTED
        S->>A: execute con PAYMENT_REJECTED
        S-->>R: PaymentRejectedException
        R-->>C: 422 ErrorResponseDTO
    end
```

## 5. Despachar

Solo el operador asignado puede despachar el envío. Las reservas se convierten en salidas de inventario, y el pedido pasa a `SHIPPED`.

```mermaid
sequenceDiagram
    actor C as Operador logístico
    participant R as LogisticsOperatorRestController
    participant S as DispatchShipmentService
    participant E as ShipmentRepositoryPort
    participant O as OrderRepositoryPort
    participant I as InventoryRepositoryPort
    participant A as RegisterAuditLogService
    C->>R: PATCH /api/v1/logistics/shipments/{shipmentId}/dispatch
    R->>S: dispatchShipment(user, shipmentId)
    S->>E: findById(shipmentId)
    E-->>S: Shipment
    S->>S: Comprueba el operador asignado
    S->>O: findById(orderId)
    O-->>S: Order
    S->>I: findByProductAndWarehouse(product, warehouse)
    S->>S: markInTransit y markShipped
    S->>S: registerMovement con SALE_OUTBOUND
    S->>I: update(inventory)
    S->>E: update(shipment)
    S->>O: update(order)
    S->>A: execute con SHIPMENT_DISPATCHED
    S-->>R: Shipment
    R-->>C: 200 ShipmentResponseDTO
```

## 6. Devolución

Tiene tres pasos: el comprador la solicita sobre un pedido entregado, el administrador la aprueba, con lo que el stock vuelve y se crea el reembolso, y por último procesa ese reembolso.

```mermaid
sequenceDiagram
    actor B as Comprador
    actor D as Administrador
    participant Q as RequestReturnService
    participant V as ApproveReturnService
    participant F as ProcessRefundService
    participant T as ReturnRequestRepositoryPort
    participant I as InventoryRepositoryPort
    participant N as InvoiceRepositoryPort
    participant E as RefundRepositoryPort
    B->>Q: POST /api/v1/buyer/orders/{orderId}/returns
    Q->>Q: Comprueba propiedad y que esté entregado
    Q->>T: save(returnRequest) en REQUESTED
    Q-->>B: 201 ReturnRequestResponseDTO
    D->>V: PATCH /api/v1/admin/returns/{returnRequestId}/approve
    V->>T: findById(returnRequestId)
    V->>N: findByOrder(order)
    V->>V: approve
    V->>I: update(inventory) con RETURN
    V->>T: update(returnRequest)
    V->>E: save(refund) en PENDING
    V-->>D: 200 ReturnRequestResponseDTO
    D->>F: PATCH /api/v1/admin/refunds/{refundId}/process
    F->>E: findById(refundId)
    F->>F: markProcessed
    F->>E: update(refund)
    F-->>D: 200 RefundResponseDTO
```

El reembolso es por el total de la factura. El administrador obtiene su id con `GET /api/v1/admin/returns/{returnRequestId}/refund`.

## 7. Logout e invalidación del token

El logout cambia `authTokenVersion`. El token sigue teniendo una firma válida y no ha vencido, pero su `ver` ya no coincide, así que la siguiente petición responde 401.

```mermaid
sequenceDiagram
    actor C as Cliente
    participant F as JwtAuthenticationFilter
    participant R as AuthRestController
    participant S as LogoutService
    participant U as UserRepositoryPort
    participant J as JwtServicePort
    participant L as LoadAuthenticatedUserService
    participant G as GlobalExceptionHandler
    C->>F: POST /api/v1/auth/logout con Bearer
    F->>R: Petición autenticada
    R->>S: logout(user)
    S->>U: findById(id)
    S->>S: bumpAuthTokenVersion
    S->>U: update(stored)
    R-->>C: 204
    C->>F: GET /api/v1/buyer/profile con el mismo token
    F->>J: validateToken(token)
    J-->>F: true
    F->>J: extractTokenVersion(token)
    J-->>F: Versión anterior
    F->>L: loadById(userId)
    L-->>F: User con la versión nueva
    F->>F: Las versiones no coinciden y hace clearContext
    F->>G: Sin autenticación, llega como AuthenticationException
    G-->>C: 401 ErrorResponseDTO
```

Cambiar la contraseña o el estado del usuario invalida los tokens del mismo modo.

## 8. Compra de principio a fin

La carpeta "Flujo completo" de la colección de Postman recorre estos flujos en orden, con el inventario de cada etapa. Ver [SETUP.md](../../SETUP.md).

## 9. Errores

| Código | Cuándo | Ejemplo |
| --- | --- | --- |
| 400 | Validación, JSON o parámetro inválido, regla de datos | Carrito vacío al confirmar, fecha mal formada |
| 401 | Sin token, token inválido o vencido, versión desactualizada, usuario no activo, credenciales incorrectas | Token tras el logout |
| 403 | Rol no permitido o recurso de otro usuario | Comprador en una ruta de administrador, login de un usuario bloqueado |
| 404 | Entidad o ruta inexistente | Pedido desconocido |
| 405, 415 | Método o tipo de contenido no admitido | `PUT` sobre el catálogo |
| 409 | Duplicado, transición inválida, stock insuficiente o bloqueo optimista | Vendedor duplicado, empacar un envío ya entregado |
| 422 | Pago rechazado | Total por encima del límite de aprobación |
| 500 | Error inesperado, sin detalles internos | — |

La tabla completa está en [Global-exception-handler.md](../Adapters/Global-exception-handler.md).
