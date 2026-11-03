# C4 Nivel 3 — Componentes

## Propósito

Abrir `nexusmarket-app` y mostrar sus componentes: cadena de seguridad, controladores, puertos, casos de uso, servicios de dominio y adaptadores de salida.

## Diagrama

```mermaid
%%{init: {"c4": {"width": 290}}}%%
C4Component
    title Componentes — nexusmarket-app

    Container_Boundary(app, "nexusmarket-app") {
        Component(security, "SecurityConfig", "Spring Security", "Reglas por ruta y rol, CORS y BCrypt")
        Component(filter, "JwtAuthenticationFilter", "OncePerRequestFilter", "Valida el token y carga el usuario en cada petición")
        Component(handler, "GlobalExceptionHandler", "RestControllerAdvice", "Convierte excepciones en ErrorResponseDTO")
        Component(portsin, "6 puertos de entrada", "Interfaces", "PublicAccessPort, BuyerPort, SellerPort, LogisticsOperatorPort, AdministratorPort y SupervisorPort")
        Component(rest, "7 controladores REST", "Spring MVC", "Auth, Public, Buyer, Seller, LogisticsOperator, Administrator y Supervisor")
        Component(seed, "DatabaseSeeder", "CommandLineRunner", "Crea el administrador inicial")
        Component(usecases, "6 casos de uso", "Service, Transactional", "Implementan los puertos y abren la transacción")
        Component(services, "60 servicios de dominio", "Java, Service, RequiredArgsConstructor", "14 subdominios con las reglas de negocio")
        Component(jpa, "12 adaptadores JPA", "Spring Data JPA", "Implementan los repositorios sobre MySQL")
        Component(portsout, "16 puertos de salida", "Interfaces", "Repositorios, JwtServicePort, PasswordServicePort y PaymentGatewayPort")
        Component(audit, "AuditLogMongoAdapter", "Spring Data MongoDB", "Escribe la auditoría tras el commit")
        Component(jwt, "JwtProvider", "JJWT", "Implementa JwtServicePort")
        Component(bcrypt, "BCryptPasswordServiceAdapter", "BCrypt", "Implementa PasswordServicePort")
        Component(payment, "SimulatedPaymentGatewayAdapter", "Java", "Implementa PaymentGatewayPort")
    }

    Rel(security, filter, "Lo agrega a la cadena")
    Rel(filter, rest, "Pasa la petición autenticada")
    Rel(rest, handler, "Propaga excepciones")
    Rel(rest, portsin, "Invoca con el User autenticado")
    Rel(usecases, portsin, "Implementa")
    Rel(usecases, services, "Delega")
    Rel(services, portsout, "Usa")
    Rel(seed, portsout, "Guarda el administrador")
    Rel(jpa, portsout, "Implementa")
    Rel(audit, portsout, "Implementa")
    Rel(jwt, portsout, "Implementa")
    Rel(bcrypt, portsout, "Implementa")
    Rel(payment, portsout, "Implementa")

    UpdateLayoutConfig($c4ShapeInRow="4", $c4BoundaryInRow="1")
```

`JwtAuthenticationFilter` usa `JwtServicePort`, que está entre los puertos de salida. Los 12 adaptadores JPA llegan a MySQL `nexusmarket` y `AuditLogMongoAdapter` a la colección `audit_logs` de MongoDB, como muestra [el nivel 2](02-nivel-2-contenedores.md). Esas líneas se omiten aquí para que el diagrama se pueda leer.

## Ciclo de vida de una petición autenticada

```mermaid
sequenceDiagram
    actor C as Cliente
    participant F as JwtAuthenticationFilter
    participant R as BuyerRestController
    participant P as BuyerPort
    participant U as BuyerUseCaseImpl
    participant S as ConsultOrderService
    participant O as OrderRepositoryPort
    participant A as OrderJpaAdapter
    C->>F: GET /api/v1/buyer/orders/{orderId} con Bearer
    F->>F: Valida el token y carga el User
    F->>R: consultOrder con AuthenticatedUserPrincipal
    R->>P: consultOrder(user, orderId)
    P->>U: Implementación con readOnly = true
    U->>S: consultOrder(requestingUser, orderId)
    S->>O: findById(orderId)
    O->>A: Implementación JPA
    A-->>S: Order del dominio
    S->>S: Valida rol y propiedad
    S-->>R: Order
    R->>R: OrderRestMapper.toResponse
    R-->>C: 200 OrderResponseDTO
```

## Componentes por paquete

| Componente | Paquete | Ficha |
| --- | --- | --- |
| Controladores, DTO, mappers, errores | `nexusmarket.adapters.rest` | [05](05-capa-adapters-rest.md) |
| Casos de uso | `nexusmarket.adapters.useCases` | [06](06-capa-adapters-usecases.md) |
| Adaptadores JPA y MongoDB | `nexusmarket.adapters.persistence` | [07](07-capa-adapters-persistence.md) |
| Pago simulado | `nexusmarket.adapters.payment` | [07](07-capa-adapters-persistence.md) |
| Seguridad | `nexusmarket.adapters.security` | [08](08-capa-adapters-security.md) |
| Dominio | `nexusmarket.domain` | [09](09-capa-domain.md) |
| Semilla | `nexusmarket.infrastructure.seed` | [10](10-capa-infrastructure.md) |
