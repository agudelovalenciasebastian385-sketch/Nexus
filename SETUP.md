# Guía paso a paso: poner en marcha NexusMarket

Probado en Windows 11 con PowerShell 5.1. Todos los comandos se ejecutan desde la raíz del repositorio, salvo que se indique otra carpeta. Los comandos de `curl.exe`, `docker` y `mvnw.cmd` también funcionan en CMD; los pasos que usan variables o funciones, como la prueba de humo, son solo para PowerShell.

## 0. Prerrequisitos

1. **Docker Desktop** en ejecución. Compruébalo con `docker --version` y `docker compose version`. Si no quieres usar Docker, ve a [Ejecutar sin Docker](#13-ejecutar-sin-docker).
2. **JDK 17 o superior**, solo si vas a correr las pruebas o ejecutar la aplicación fuera de Docker. Maven no hace falta: el proyecto trae `mvnw.cmd`, y dentro de Docker todo se compila en la imagen.
3. Puertos libres en el equipo: **8080** (aplicación), **3306** (MySQL), **27017** (MongoDB), **8081** (phpMyAdmin) y **8082** (Mongo Express). Compruébalo con:
   ```powershell
   Get-NetTCPConnection -LocalPort 8080,3306,27017,8081,8082 -State Listen -ErrorAction SilentlyContinue | Select-Object LocalPort, OwningProcess
   ```
   Si no muestra nada, todos están libres. Si ya tienes los contenedores de este proyecto corriendo, los puertos aparecerán ocupados por ellos, y eso es normal.

## 1. Levantar todo con un comando

```powershell
docker compose up -d --build
```

La primera vez tarda unos minutos, porque descarga las imágenes y las dependencias de Maven. Compose arranca MySQL y MongoDB, espera a que estén sanos y solo entonces inicia la aplicación. Al arrancar con la base vacía, la aplicación crea las tablas y el administrador inicial.

Comprueba el estado de los contenedores:

```powershell
docker ps --format "{{.Names}} | {{.Status}}"
```

`nexusmarket-app`, `nexusmarket-mysql` y `nexusmarket-mongo` deben aparecer como `(healthy)`. La aplicación puede tardar cerca de un minuto en pasar de `(health: starting)` a `(healthy)`.

## 2. Verificar que funciona

Salud de la aplicación, sin token. Debe responder `UP` y solo estará así si MySQL y MongoDB responden:

```powershell
curl.exe -s http://localhost:8080/actuator/health
# {"groups":["liveness","readiness"],"status":"UP"}
```

Login del administrador inicial (`admin@nexusmarket.com` / `Admin123*`). Debe responder **HTTP 200** con un `token`:

```powershell
Set-Content "$env:TEMP\login.json" -Value '{"email":"admin@nexusmarket.com","password":"Admin123*"}' -NoNewline
curl.exe -s -w "`nHTTP:%{http_code}`n" -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d "@$env:TEMP\login.json"
```

> En PowerShell, pasar JSON directamente a `curl.exe` suele dañar las comillas. Por eso los cuerpos van en un archivo, con `-d "@archivo.json"`.

## 3. Zona horaria

La aplicación usa siempre **`America/Bogota`**, así que todas las fechas de pedidos, facturas, envíos y reportes están en hora de Colombia, sin importar la zona del equipo:

| Dónde | Configuración |
| --- | --- |
| Imagen Docker | `-Duser.timezone=America/Bogota` en el `ENTRYPOINT` y `TZ=America/Bogota` en `docker-compose.yml` |
| Fuera de Docker (`mvnw.cmd spring-boot:run`) | `-Duser.timezone=America/Bogota` en el `spring-boot-maven-plugin` de `pom.xml` |
| MySQL | `serverTimezone=America/Bogota` en la URL y `hibernate.jdbc.time_zone` en `application.yml` |
| Pruebas (`mvnw.cmd test` y `verify`) | `-Duser.timezone=America/Bogota` en el `argLine` de surefire y failsafe, en `pom.xml` |

Las columnas de fecha de MySQL guardan la misma hora de Colombia que devuelve la API. MongoDB guarda instantes en UTC, así que en Mongo Express las fechas de auditoría aparecen con 5 horas más (terminan en `Z`). Es el mismo instante, y la API las devuelve en hora de Colombia.

Si ejecutas el jar directamente con `java -jar`, agrega tú mismo `-Duser.timezone=America/Bogota`.

## 4. Compilar y correr las pruebas

Desde la carpeta `application`:

```powershell
cd application
.\mvnw.cmd test
```

Ejecuta las **2493 pruebas unitarias**, sin Docker. Resultado esperado: `BUILD SUCCESS`.

```powershell
.\mvnw.cmd verify
```

Ejecuta las unitarias y las **13 de integración**, y necesita los contenedores de MySQL y MongoDB corriendo. Usan bases separadas (`nexusmarket_test`), así que no tocan los datos de la aplicación, y pueden correr con `nexusmarket-app` encendida.

## 5. Reportes de cobertura

`mvnw.cmd verify` genera tres reportes. Abre el `index.html` de cada carpeta en un navegador:

| Reporte | Carpeta | Refleja |
| --- | --- | --- |
| Unitarias | `application/target/site/jacoco` | Las pruebas unitarias (también lo genera `mvnw.cmd test`) |
| Integración | `application/target/site/jacoco-it` | Las pruebas de integración |
| Combinado | `application/target/site/jacoco-merged` | Ambas. Es la cobertura real del proyecto |

Los datos de cobertura se acumulan entre ejecuciones. Antes de medir, bórralos con `Remove-Item target\*.exec` desde `application`. Si VS Code o Eclipse tienen el proyecto abierto, también compilan sobre `target/`, así que puedes ver `Unresolved compilation problem` o cifras algo distintas: borra `target\classes` y `target\test-classes` y repite. Los detalles están en [Testing-strategy.md](SDD/Testing/Testing-strategy.md).

## 6. Prueba de humo con curl

Recorre el flujo principal con la aplicación en Docker: el administrador registra un vendedor, el vendedor publica un producto con stock y un comprador nuevo lo compra. Copia los bloques en la misma ventana de PowerShell, en orden. Los correos y documentos usan la hora actual, así que se puede repetir sin errores de duplicados.

**Preparación:**

```powershell
$api = "http://localhost:8080/api/v1"
$tmp = "$env:TEMP\nexusmarket-smoke"
New-Item -ItemType Directory -Path $tmp -Force | Out-Null
$id = Get-Date -Format "yyMMddHHmmss"
$address = '{"line1":"Calle 10 # 20-30","city":"Medellin","state":"Antioquia","country":"Colombia"}'

function Call($method, $path, $token, $body) {
    $curlArgs = @("-s", "-w", "`n%{http_code}", "-X", $method, "$api$path")
    if ($token) { $curlArgs += @("-H", "Authorization: Bearer $token") }
    if ($body) {
        Set-Content "$tmp\body.json" -Value $body -NoNewline
        $curlArgs += @("-H", "Content-Type: application/json", "-d", "@$tmp\body.json")
    }
    $lines = curl.exe @curlArgs
    Write-Host "$($lines[-1])  $method $path"
    ($lines[0..($lines.Count - 2)] -join "`n") | ConvertFrom-Json
}
```

**1. El administrador inicia sesión y registra un vendedor con su primera bodega** (esperado: `200` y `201`):

```powershell
$admin = (Call POST "/auth/login" $null '{"email":"admin@nexusmarket.com","password":"Admin123*"}').token
$seller = Call POST "/admin/sellers" $admin "{`"identificationNumber`":`"S$id`",`"fullName`":`"Tech Store`",`"email`":`"seller$id@example.com`",`"password`":`"Seller123*`",`"warehouseName`":`"Bodega Principal`",`"warehouseLocation`":$address}"
```

**2. El vendedor inicia sesión, registra un producto, lo publica y carga stock** (esperado: `200`, `201`, `200` y `201`):

```powershell
$sellerToken = (Call POST "/auth/login" $null "{`"email`":`"seller$id@example.com`",`"password`":`"Seller123*`"}").token
$product = Call POST "/seller/products" $sellerToken '{"type":"PHYSICAL","name":"Mouse inalambrico","description":"Mouse ergonomico","price":100.00,"variants":[]}'
$published = Call PATCH "/seller/products/$($product.id)/publish" $sellerToken $null
$stock = Call POST "/seller/inventories" $sellerToken "{`"productId`":`"$($product.id)`",`"warehouseId`":`"$($seller.firstWarehouse.id)`",`"quantity`":10}"
```

**3. Un comprador se registra e inicia sesión** (esperado: `201` y `200`):

```powershell
$buyer = Call POST "/auth/register/buyers" $null "{`"identificationNumber`":`"B$id`",`"fullName`":`"Maria Gomez`",`"email`":`"buyer$id@example.com`",`"password`":`"Buyer123*`",`"primaryAddress`":$address}"
$buyerToken = (Call POST "/auth/login" $null "{`"email`":`"buyer$id@example.com`",`"password`":`"Buyer123*`"}").token
```

**4. El comprador agrega al carrito, confirma el pedido y paga** (esperado: `200`, `201` y `200`):

```powershell
$cart = Call POST "/buyer/cart/items" $buyerToken "{`"productId`":`"$($product.id)`",`"quantity`":2}"
$order = Call POST "/buyer/orders" $buyerToken $null
$paid = Call POST "/buyer/orders/$($order.id)/payment" $buyerToken $null
"Pedido $($paid.id): estado $($paid.status), total $($paid.totalAmount), creado $($paid.createdAt)"
```

El estado debe ser `PAID`, el total `200.00` y la fecha, la hora actual de Colombia.

**5. Comprueba lo que quedó guardado:**

```powershell
docker exec nexusmarket-mysql mysql -uroot -proot --batch -e "SELECT id, status, created_at FROM nexusmarket.orders ORDER BY created_at DESC LIMIT 1;"
docker exec nexusmarket-mongo mongosh --quiet nexusmarket --eval "db.audit_logs.countDocuments()"
```

MySQL debe mostrar en `created_at` la misma hora que la API, y el conteo de auditoría debe ser mayor que cero.

## 7. phpMyAdmin y Mongo Express

Son herramientas de desarrollo para ver los datos; la aplicación no depende de ellas.

| Herramienta | URL | Usuario / contraseña | Qué mirar |
| --- | --- | --- | --- |
| phpMyAdmin | <http://localhost:8081> | `root` / `root` | Base `nexusmarket`: usuarios, productos, pedidos, inventario |
| Mongo Express | <http://localhost:8082> | `admin` / `admin` | Base `nexusmarket`, colección `audit_logs` |

## 8. Apagar todo

```powershell
docker compose down
```

Detiene y elimina los contenedores, pero **conserva los datos**. Para borrarlos también:

```powershell
docker compose down -v
```

El siguiente arranque empieza con las bases vacías y vuelve a crear el administrador inicial.

## 9. Ejecutar la aplicación fuera de Docker

Levanta solo las bases de datos y detén la aplicación de Docker, para liberar el puerto 8080:

```powershell
docker compose up -d mysql-db mongo-db
docker compose stop nexusmarket-app
cd application
.\mvnw.cmd spring-boot:run
```

Se detiene con `Ctrl+C`. Para volver a la aplicación en Docker: `docker compose up -d nexusmarket-app`.

## 10. Variables de entorno

Todas tienen un valor por defecto. Defínelas antes de `docker compose up` para cambiarlas, por ejemplo `$env:JWT_SECRET = "..."`.

| Variable | Uso |
| --- | --- |
| `HOST_SERVER_PORT` | Puerto del equipo donde se publica la aplicación (por defecto 8080). Solo en Docker |
| `MYSQL_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD` | Conexión a MySQL |
| `MONGODB_URI` | Conexión a MongoDB |
| `JWT_SECRET` | Clave de firma de los tokens. El valor por defecto es público (ver la nota de seguridad en [README.md](README.md)) |
| `SEED_ADMIN_EMAIL`, `SEED_ADMIN_PASSWORD` | Administrador inicial. Solo se aplican si la base no tiene ningún administrador |

## 11. Colección de Postman

La carpeta `postman/` trae la colección y su entorno:

| Carpeta de la colección | Para qué sirve |
| --- | --- |
| **Flujo completo** | 41 peticiones en orden, de principio a fin, con 72 verificaciones. Se ejecuta con el Collection Runner |
| Autenticacion, Publico, Comprador, Vendedor, Operador logistico, Administrador, Supervisor | Los 108 endpoints por rol, para explorar a mano |

El flujo recorre: login del administrador, registro del vendedor, del operador y del supervisor, productos físico y digital, stock, compra y pago, factura, envío, devolución y reembolso, los 4 reportes con la fecha de hoy en Colombia y el logout. Incluye casos negativos: duplicado (409), otro rol (403), sin token (401) y token tras el logout (401). Cada ejecución usa correos y documentos nuevos, así que se puede repetir sin errores de duplicados.

### En Postman

1. Con la aplicación corriendo, abre Postman y usa **Import** para cargar los dos archivos de `postman/`: `NexusMarket.postman_collection.json` y `NexusMarket.postman_environment.json`.
2. Arriba a la derecha, selecciona el entorno **NexusMarket**. Sin él, las variables no se guardan y el flujo falla.
3. En la colección, abre el menú de la carpeta **Flujo completo**, elige **Run folder** y pulsa **Run**. Conserva el orden de las peticiones.
4. Todas las verificaciones deben aparecer en verde.

Las carpetas por rol usan las variables que deja el flujo, así que ejecuta el flujo una vez antes de explorarlas. Cada carpeta de rol empieza con **Iniciar sesion**, que renueva su token. En esas carpetas, un 400, 404 o 409 puede ser correcto según el estado de los datos: por ejemplo, un pedido ya entregado no se puede pagar. El código de éxito de cada endpoint aparece en el nombre de la petición.

Si cambiaste el puerto o las credenciales del administrador, ajusta `baseUrl`, `adminEmail` y `adminPassword` en el entorno.

### Desde la consola, con newman

No hace falta instalar Node: newman corre en un contenedor, conectado a la red de la aplicación. Ejecuta esto desde la raíz del repositorio, con PowerShell:

```powershell
docker run --rm --network container:nexusmarket-app -v "${PWD}\postman:/etc/newman" postman/newman:6-alpine run NexusMarket.postman_collection.json -e NexusMarket.postman_environment.json --folder "Flujo completo"
```

Resultado esperado: `requests 41` y `assertions 72`, con 0 fallos.

## 12. Swagger UI

Documentación interactiva de los 108 endpoints, abierta sin token:

| Qué | Dirección |
| --- | --- |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| Especificación OpenAPI en JSON | <http://localhost:8080/v3/api-docs> |

Los endpoints están agrupados por rol, y cada uno muestra sus parámetros y sus respuestas de error. El candado marca los que necesitan token.

Para probar una ruta protegida con el administrador:

1. Abre **Authentication**, luego `POST /api/v1/auth/login`, y pulsa **Try it out**.
2. Reemplaza el cuerpo de ejemplo por las credenciales del administrador inicial y pulsa **Execute**:
   ```json
   {"email": "admin@nexusmarket.com", "password": "Admin123*"}
   ```
3. Copia el valor de `token` de la respuesta, sin las comillas.
4. Pulsa **Authorize**, arriba a la derecha, pega el token, **sin** escribir `Bearer` delante, y pulsa **Authorize** y luego **Close**.
5. Abre **Administrator**, luego `GET /api/v1/admin/warehouses`, y pulsa **Try it out** y **Execute**. Debe responder **200**.

Los valores de ejemplo de los esquemas, como `user@example.com`, son genéricos: las credenciales reales no se publican en la especificación. El token se conserva al recargar la página, hasta que venza o cierres la sesión con **Authorize** y **Logout**.

## 13. Ejecutar sin Docker

Con el perfil `memory`, la aplicación arranca sin MySQL ni MongoDB y sin Docker. Solo necesitas JDK 17 o superior. Es la forma más rápida de probar el sistema.

Si la aplicación de Docker está corriendo, primero libera el puerto 8080 con `docker compose stop nexusmarket-app`. Luego, desde la carpeta `application`:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=memory"
```

En CMD, el comando es el mismo, sin comillas. Se detiene con `Ctrl+C`. Comprueba que arrancó con `curl.exe -s http://localhost:8080/actuator/health`. Swagger UI y Postman funcionan igual que en Docker.

### Datos de ejemplo

Se crean al arrancar, para poder probar de inmediato:

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Administrador | `admin@nexusmarket.com` | `Admin123*` |
| Vendedor | `vendedor@example.com` | `Seller123*` |
| Operador logístico | `operador@example.com` | `Operator123*` |
| Supervisor | `supervisor@example.com` | `Supervisor123*` |
| Comprador | `comprador@example.com` | `Buyer123*` |

El vendedor tiene una bodega con dos productos físicos publicados con stock (Wireless Mouse y Mechanical Keyboard) y un producto digital publicado (Java Guide).

Para arrancar solo con el administrador, en PowerShell:

```powershell
$env:SEED_DEMO_ENABLED = "false"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=memory"
```

### Colección de Postman contra este perfil

En Postman, no cambia nada: `baseUrl` ya apunta a `http://localhost:8080`. Con newman en un contenedor, `localhost` sería el propio contenedor, así que se usa la dirección del equipo. Desde la raíz del repositorio:

```powershell
docker run --rm -v "${PWD}\postman:/etc/newman" postman/newman:6-alpine run NexusMarket.postman_collection.json -e NexusMarket.postman_environment.json --folder "Flujo completo" --env-var "baseUrl=http://host.docker.internal:8080"
```

### Limitaciones

Este perfil sirve para demostraciones y pruebas, no para datos reales:

- **Los datos se pierden** al detener la aplicación.
- **Atiende las peticiones de una en una**, así que no escala. A cambio, dos compras simultáneas de la última unidad no la venden dos veces: una responde `201` y la otra `409` por stock insuficiente, no por el bloqueo optimista, que solo existe con MySQL.
- **No hay rollback.** Si una operación fallara después de modificar un objeto, ese cambio se quedaría. Los servicios que modifican varios objetos validan antes de cambiar; la revisión está en [Persistence-adapters.md](SDD/Adapters/Persistence-adapters.md#9-memory-adapters-adapterspersistencememory).

## 14. Solución de problemas

| Síntoma | Causa probable | Solución |
| --- | --- | --- |
| `Bind for 0.0.0.0:8080 failed: port is already allocated` o `Port 8080 was already in use` | Otro proceso usa el 8080, a menudo la aplicación corriendo fuera de Docker | Detén ese proceso, o ejecuta `$env:HOST_SERVER_PORT="8090"` antes de `docker compose up -d --build`. Para ver quién lo usa: `Get-NetTCPConnection -LocalPort 8080 -State Listen` |
| `Bind for 0.0.0.0:3306 failed: port is already allocated`, o el login falla en `spring-boot:run` | Un MySQL instalado en Windows ocupa el 3306 | Abre PowerShell como administrador, busca el servicio con `Get-Service *mysql*` y detenlo con `Stop-Service MySQL80`, usando el nombre que aparezca |
| `error during connect` o `failed to connect to the docker API` | Docker Desktop no está iniciado | Abre Docker Desktop, espera a que indique que está corriendo y repite el comando |
| Docker Desktop no arranca o menciona `WSL` | WSL 2 ausente o desactualizado | En PowerShell como administrador: `wsl --install` si no está instalado, o `wsl --update` si ya lo está. Reinicia el equipo y abre Docker Desktop |
| `nexusmarket-app` sigue en `(unhealthy)` o se reinicia | La aplicación no logró arrancar | Mira la causa con `docker compose logs nexusmarket-app` |
| `/actuator/health` responde `DOWN` | MySQL o MongoDB no responden | `docker ps` para ver cuál está caído, y `docker compose up -d`. La aplicación se recupera sola |
| El login del administrador responde `401` | La base ya tenía un administrador con otra contraseña, porque `SEED_ADMIN_PASSWORD` solo se aplica con la base vacía | En desarrollo: `docker compose down -v` y vuelve a levantar. **Borra todos los datos** |
| `400` al usar `curl.exe` con JSON | PowerShell dañó las comillas | Usa un archivo: `-d "@archivo.json"`, como en los pasos 2 y 6 |
| `curl` responde con un error de `Invoke-WebRequest` | En PowerShell 5.1, `curl` es un alias | Escribe `curl.exe` |
| `.\mvnw.cmd verify` falla con `Communications link failure` | MySQL o MongoDB están apagados | `docker compose up -d mysql-db mongo-db` y repite |
| Los pedidos antiguos muestran una hora desplazada 5 horas | Se crearon antes de fijar la zona horaria, cuando MySQL guardaba en UTC | En desarrollo: `docker compose down -v`. **Borra todos los datos** |
