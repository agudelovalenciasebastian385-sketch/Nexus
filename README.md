# NexusMarket

Plataforma de marketplace digital que actúa como intermediaria comercial entre compradores y vendedores. Cubre el catálogo, el inventario distribuido por bodegas, el carrito, los pedidos, el pago simulado, la facturación, los envíos, las devoluciones con reembolso, los reportes y una auditoría completa de las operaciones.

Es una API REST de **108 endpoints** en **Java 17 y Spring Boot 4.1**, con **arquitectura hexagonal**, desarrollada a partir de la especificación del [`SDD/`](SDD/). No incluye interfaz gráfica.

> **Para ponerla en marcha paso a paso, ve a [`SETUP.md`](SETUP.md).** Con Docker Desktop basta un comando: `docker compose up -d --build`.

## Roles

| Rol | Qué puede hacer | Cómo se crea |
| --- | --- | --- |
| **Comprador** (`BUYER`) | Administra su perfil y su carrito, confirma y paga pedidos, consulta sus facturas y solicita devoluciones | Se registra solo |
| **Vendedor** (`SELLER`) | Administra sus bodegas, productos e inventario, y consulta su reporte de desempeño | Lo registra el administrador, con su primera bodega |
| **Operador logístico** (`LOGISTICS_OPERATOR`) | Registra y ajusta inventario, y crea, empaca, despacha y entrega envíos | Lo registra el administrador |
| **Administrador** (`ADMINISTRATOR`) | Administra usuarios, vendedores y bodegas del marketplace, aprueba devoluciones y procesa reembolsos | El inicial se crea al arrancar; los demás usuarios internos los registra un administrador |
| **Supervisor** (`SUPERVISOR`) | Consulta toda la información, reportes y auditoría, sin poder modificar nada de negocio | Lo registra el administrador |

Además, cualquier visitante puede consultar el catálogo público sin iniciar sesión.

## Stack tecnológico

| Capa | Tecnología |
| --- | --- |
| Lenguaje y compilación | Java 17, Maven (wrapper `mvnw.cmd`) |
| Framework | Spring Boot 4.1, Spring MVC, Spring Security (JWT sin estado, BCrypt) |
| Datos de negocio | Spring Data JPA con Hibernate sobre MySQL 8 |
| Auditoría | Spring Data MongoDB sobre MongoDB 6 (colección `audit_logs`) |
| JWT | JJWT 0.12.6 con HS256 |
| Validación | Jakarta Bean Validation |
| Salud | Spring Boot Actuator (solo `/actuator/health`) |
| Documentación del API | springdoc-openapi 3.1.1: Swagger UI y especificación OpenAPI |
| Pruebas | JUnit 5, AssertJ, Mockito (solo en pruebas web), MockMvc y JaCoCo |
| Despliegue | Docker y Docker Compose |

## Arquitectura hexagonal

El dominio contiene toda la lógica de negocio y no depende de la web, la persistencia ni la seguridad: no usa HTTP, JSON, JPA, SQL, MongoDB, JWT ni librerías de hash. Sus únicas anotaciones de framework son `@Service` (Spring) y `@RequiredArgsConstructor` (Lombok) en los servicios, para la inyección de dependencias; los modelos, objetos de valor, excepciones y puertos no usan ninguna. Sus pruebas corren sin arrancar Spring y sin bases de datos. Se comunica con el exterior solo a través de puertos, y las dependencias siempre apuntan hacia él.

```text
Petición HTTP → Controlador REST → Puerto por rol → Caso de uso → Servicio de dominio → Puerto de salida → Adaptador → MySQL / MongoDB
```

- **6 puertos de entrada**, uno por rol más el acceso público. Cada rol solo ve las operaciones que le corresponden.
- **60 servicios de dominio**, organizados en 14 subdominios.
- **16 puertos de salida**, implementados por adaptadores de MySQL, MongoDB, JWT, BCrypt y pago simulado.

```text
NexusMarket/
├── application/                   Aplicación Spring Boot
│   ├── Dockerfile
│   └── src/main/java/nexusmarket/
│       ├── App.java
│       ├── domain/
│       │   ├── models/            Entidades y reportes
│       │   ├── valueobjects/      Catálogos, Address, Variant, ReportPeriod
│       │   ├── exceptions/        Excepciones de dominio
│       │   ├── services/          60 servicios en 14 subdominios
│       │   └── ports/             in/ (6 puertos por rol), out/ (16 puertos de salida)
│       ├── adapters/
│       │   ├── rest/              7 controladores, DTO, mappers y manejo de errores
│       │   ├── useCases/          Implementación de los puertos por rol y límite transaccional
│       │   ├── persistence/       jpa/ (MySQL), mongodb/ (auditoría) y memory/ (perfil sin bases de datos)
│       │   ├── security/          SecurityConfig, filtro JWT, JwtProvider, BCrypt
│       │   └── payment/           Pasarela de pago simulada
│       └── infrastructure/seed/   Administrador inicial
├── SDD/                           Especificación del sistema
├── postman/                       Colección y entorno de Postman
├── tools/                         Verificación del SDD y generadores
├── docker-compose.yml
├── SETUP.md
└── README.md
```

## Reglas de negocio principales

- **Acceso:** toda operación la ejecuta un usuario autenticado y activo (RG-01). Cada usuario tiene un solo rol (RG-02) y solo accede a la información que le corresponde (RG-03).
- **Vendedores:** no pueden registrarse solos; los registra el administrador junto con su primera bodega.
- **Productos:** pasan por `DRAFT`, `PUBLISHED`, `SUSPENDED` y `DISCONTINUED`. Solo los publicados se pueden agregar al carrito.
- **Pedidos:** confirmar un pedido convierte el carrito en un pedido `PENDING_PAYMENT` y reserva el inventario de los productos físicos. Solo el inventario `AVAILABLE` se puede reservar.
- **Pago:** un pago aprobado genera la factura. Un pago rechazado también queda registrado en la auditoría.
- **Productos digitales:** se entregan al pagar, sin envíos.
- **Envíos:** se empacan, se despachan y se entregan. Al despachar, las reservas se convierten en salidas de inventario.
- **Devoluciones:** solo aplican a pedidos entregados. Al aprobarse, el stock vuelve al inventario y se crea el reembolso, que luego procesa el administrador.

## Decisiones destacadas

| Decisión | Qué hace | Por qué |
| --- | --- | --- |
| **Transacciones en los casos de uso** | Cada `*UseCaseImpl` tiene `@Transactional`, con `readOnly = true` en las consultas. `validatePayment` usa `noRollbackFor = PaymentRejectedException.class` | Una operación se confirma completa o no se confirma, y el pago rechazado conserva su registro |
| **Bloqueo optimista** | `@Version` en usuario, carrito, inventario y pedido. El conflicto responde 409 | Dos compras simultáneas no pueden vender el mismo stock |
| **Auditoría tras el commit** | MongoDB se escribe en `afterCommit` | Una transacción revertida no deja auditoría falsa, porque la transacción de MySQL no cubre MongoDB |
| **Rutas propias sin id** | `/api/v1/buyer/profile` usa el id del token, no uno de la ruta | Un usuario no puede pedir el recurso de otro cambiando la URL. Los servicios validan además la propiedad |
| **Normalización del correo** | El dominio lo recorta y lo pasa a minúsculas antes de validarlo | `Maria@Gmail.com` y `maria@gmail.com` son el mismo usuario |
| **Autobloqueo del administrador** | Un administrador no puede cambiar su propio estado (403) | Evita que la plataforma quede sin administradores activos por error |
| **Invalidación de tokens** | Cada token lleva la versión del usuario, que cambia con logout, cambio de contraseña o de estado | Un token ya emitido deja de servir de inmediato, y el filtro recarga al usuario en cada petición |
| **Perfil en memoria** | Con `memory`, 13 adaptadores en memoria reemplazan a JPA y MongoDB, sin tocar el dominio ni los casos de uso | Demuestra la arquitectura hexagonal: cambiar la persistencia no toca el negocio. Los mismos 8 escenarios de punta a punta pasan en ambos perfiles |
| **Zona horaria fija** | La aplicación usa siempre `America/Bogota`, en Docker y fuera de él | Las fechas y los reportes no dependen del equipo donde se ejecuta ([detalle](SETUP.md#3-zona-horaria)) |

## Pruebas y cobertura

| Grupo | Pruebas | Comando | Necesita Docker |
| --- | ---: | --- | --- |
| Unitarias de lógica y API: dominio, servicios, adaptadores, endpoints y perfil en memoria | 1508 | `mvnw.cmd test` | No |
| Unitarias de documentación: cada operación de la especificación OpenAPI | 985 | `mvnw.cmd test` | No |
| Integración | 13 | `mvnw.cmd verify` | Sí |

Los 108 endpoints tienen prueba de su camino exitoso, y una prueba guardiana falla si aparece un endpoint sin ella. Otra prueba compara la especificación OpenAPI con esos mismos endpoints.

Cobertura del reporte combinado (`application/target/site/jacoco-merged`):

| Área | Líneas | Ramas |
| --- | ---: | ---: |
| `adapters` | 92,3 % | 64,8 % |
| `domain` | 90,7 % | 78,6 % |
| **Total** | **91,5 %** | **74,4 %** |

La estrategia completa, sus límites y los tres reportes están en [Testing-strategy.md](SDD/Testing/Testing-strategy.md).

## Cómo ejecutarlo

```powershell
docker compose up -d --build
curl.exe -s http://localhost:8080/actuator/health
```

Los prerrequisitos, las pruebas, la prueba de humo, cómo ejecutarlo fuera de Docker y la solución de problemas están en [`SETUP.md`](SETUP.md).

**Sin Docker ni bases de datos**, con datos de ejemplo listos para probar:

```powershell
cd application
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=memory"
```

Los datos se pierden al detenerla y no hay rollback, así que sirve para demostraciones, no para datos reales ([credenciales y limitaciones](SETUP.md#13-ejecutar-sin-docker)).

Con la aplicación corriendo, la documentación interactiva está en [Swagger UI](http://localhost:8080/swagger-ui.html) ([cómo autorizarse](SETUP.md#12-swagger-ui)).

La carpeta [`postman/`](postman/) trae una colección con el flujo completo ejecutable y los 108 endpoints por rol ([cómo usarla](SETUP.md#11-colección-de-postman)).

## Mapa del SDD

| Documento | Contenido |
| --- | --- |
| [Modelo C4](SDD/C4/README.md) | Contexto, contenedores, componentes, código, una ficha por capa y los flujos con diagramas |
| [Software Architecture](SDD/Software%20Architecture/Software%20Architecture.md) | Principios, capas, estructura y configuración |
| [Domain Model](SDD/domain/Domain%20Model.md) | Entidades y sus reglas |
| [Domain Value Objects](SDD/domain/Domain%20Value%20Objects.md) | Catálogos y objetos de valor |
| [Domain Services](SDD/domain/Domain%20Services.md) | Los 60 servicios por subdominio, con detalle en [`services/`](SDD/domain/services/) |
| [Input-ports](SDD/domain/Input-ports.md) y [Output-ports](SDD/domain/Output-ports.md) | Puertos por rol y puertos de salida |
| [Use-cases-adapters](SDD/Adapters/Use-cases-adapters.md) | Casos de uso y transacciones |
| [Api-rest-endpoints](SDD/Adapters/Api-rest-endpoints.md) | Los 108 endpoints y sus contratos |
| [Rest-adapters](SDD/Adapters/Rest-adapters.md), [Rest-validation](SDD/Adapters/Rest-validation.md) y [Global-exception-handler](SDD/Adapters/Global-exception-handler.md) | Controladores, validación y errores |
| [Rest-security-cors](SDD/Adapters/Rest-security-cors.md) y [Security-adapters](SDD/Adapters/Security-adapters.md) | Rutas, JWT, BCrypt y CORS |
| [Persistence-adapters](SDD/Adapters/Persistence-adapters.md) | MySQL, MongoDB y pago simulado |
| [Testing-strategy](SDD/Testing/Testing-strategy.md) | Tipos de pruebas, cobertura y límites |

Las herramientas que verifican y generan estos documentos están en [`tools/`](tools/README.md).

## Nota de seguridad

**Los valores por defecto de este repositorio son públicos y solo son aceptables para uso académico o local:**

- **Clave JWT:** cualquiera que lea el repositorio podría forjar tokens válidos con la clave por defecto, incluso de administrador. Define tu propia clave en `JWT_SECRET`.
- **Administrador inicial:** `admin@nexusmarket.com` / `Admin123*`. Cambia `SEED_ADMIN_EMAIL` y `SEED_ADMIN_PASSWORD` antes del primer arranque, porque solo se aplican con la base vacía.
- **Bases de datos y herramientas:** MySQL usa `root` / `root`, MongoDB no tiene autenticación y sus puertos quedan publicados, igual que phpMyAdmin y Mongo Express.

Para exponer el servicio fuera de tu equipo necesitarías cambiar estos valores, no publicar los puertos de las bases ni las herramientas de desarrollo, y usar HTTPS.

## Licencia

[MIT](LICENSE).
