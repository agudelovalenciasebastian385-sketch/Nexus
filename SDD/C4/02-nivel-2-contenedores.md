# C4 Nivel 2 — Contenedores

## Propósito

Mostrar las piezas que se ejecutan por separado y cómo se conectan. Todas las define `docker-compose.yml`. El desarrollador aparece solo en este nivel, porque es quien usa las herramientas de desarrollo.

## Diagrama

```mermaid
%%{init: {"c4": {"width": 260}}}%%
C4Container
    title Contenedores — NexusMarket

    Person(usuarios, "Público y los 5 roles", "Clientes HTTP de la API")
    Person(desarrollador, "Desarrollador", "Revisa los datos durante el desarrollo")

    System_Boundary(sistema, "NexusMarket") {
        ContainerDb(mysql, "nexusmarket-mysql", "MySQL 8.0", "Base nexusmarket. Datos de negocio. Puerto 3306")
        Container(app, "nexusmarket-app", "Java 17, Spring Boot 4.1", "API REST, lógica de negocio, JWT y pago simulado. Puerto 8080")
        ContainerDb(mongo, "nexusmarket-mongo", "MongoDB 6.0", "Base nexusmarket, colección audit_logs. Puerto 27017")
    }

    System_Boundary(herramientas, "Herramientas de desarrollo") {
        Container(phpmyadmin, "nexusmarket-phpmyadmin", "phpMyAdmin 5.2", "Interfaz web de MySQL. Puerto 8081")
        Container(mongoexpress, "nexusmarket-mongo-express", "Mongo Express 1.0.2", "Interfaz web de MongoDB. Puerto 8082")
    }

    Rel(usuarios, app, "HTTP/JSON, Authorization: Bearer", "8080")
    Rel(app, mysql, "JDBC, JPA e Hibernate", "3306")
    Rel(app, mongo, "Spring Data MongoDB", "27017")
    Rel(desarrollador, phpmyadmin, "Navegador", "8081")
    Rel(desarrollador, mongoexpress, "Navegador", "8082")
    Rel(phpmyadmin, mysql, "Consulta", "3306")
    Rel(mongoexpress, mongo, "Consulta", "27017")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Contenedores

| Servicio | Contenedor | Imagen | Puerto | Healthcheck | Espera a |
| --- | --- | --- | --- | --- | --- |
| `mysql-db` | `nexusmarket-mysql` | `mysql:8.0` | 3306 | `mysqladmin ping` por TCP | — |
| `mongo-db` | `nexusmarket-mongo` | `mongo:6.0` | 27017 | `mongosh` con `ping` | — |
| `nexusmarket-app` | `nexusmarket-app` | `application/Dockerfile` | 8080 | `curl` a `/actuator/health` | `mysql-db` y `mongo-db` sanos |
| `phpmyadmin` | `nexusmarket-phpmyadmin` | `phpmyadmin:5.2` | 8081 | — | `mysql-db` sano |
| `mongo-express` | `nexusmarket-mongo-express` | `mongo-express:1.0.2` | 8082 | — | `mongo-db` iniciado |

phpMyAdmin y Mongo Express son herramientas de desarrollo. La aplicación no depende de ellas.

## Perfil `memory`: un solo proceso

Con `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=memory`, la aplicación es el único proceso: no hay contenedores ni bases de datos, y los datos viven en la memoria de la JVM. Es para demostraciones y pruebas; los datos se pierden al detenerla. Ver [la capa de persistencia](07-capa-adapters-persistence.md#perfil-memory).

```mermaid
%%{init: {"c4": {"width": 280}}}%%
C4Container
    title Contenedores — perfil memory

    Person(usuarios, "Público y los 5 roles", "Clientes HTTP de la API")

    System_Boundary(sistema, "NexusMarket") {
        Container(app, "Aplicación con el perfil memory", "Java 17, Spring Boot 4.1", "API REST, adaptadores en memoria y datos de ejemplo. Puerto 8080")
    }

    Rel(usuarios, app, "HTTP/JSON, Authorization: Bearer", "8080")
```

## Imagen de la aplicación

`application/Dockerfile` tiene dos etapas: Maven con JDK 17 compila el jar sin ejecutar pruebas, y la imagen final solo trae el JRE 17 y el jar, con un usuario sin privilegios de root. Arranca con `-Duser.timezone=America/Bogota`.

## Datos

| Almacén | Volumen | Qué guarda | Consistencia |
| --- | --- | --- | --- |
| MySQL `nexusmarket` | `mysql_data` | Todo el estado de negocio | Transacción de cada caso de uso, con `@Version` para el bloqueo optimista |
| MongoDB `nexusmarket` | `mongo_data` | La colección `audit_logs` | Se escribe tras el commit de MySQL, en `afterCommit` |
| `nexusmarket_test`, en ambos | Los mismos | Datos de las pruebas de integración | Bases separadas, con el perfil `test` |

## Variables de entorno de la aplicación

| Variable | Uso |
| --- | --- |
| `TZ` | Zona horaria del contenedor, `America/Bogota` |
| `MYSQL_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD` | Conexión a MySQL, con el host `mysql-db` |
| `MONGODB_URI` | Conexión a MongoDB, con el host `mongo-db` |
| `JWT_SECRET` | Clave de firma de los tokens |
| `SEED_ADMIN_EMAIL`, `SEED_ADMIN_PASSWORD` | Administrador inicial |

Fuera de Docker, la aplicación usa los valores de `application.yml`, con `localhost`, contra los mismos contenedores de bases de datos. Ver [SETUP.md](../../SETUP.md).
