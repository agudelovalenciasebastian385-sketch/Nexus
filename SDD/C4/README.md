# Documentación C4 — NexusMarket

Modelo C4 en 4 niveles, una ficha por cada capa del proyecto y la matriz de flujos por endpoint. Los diagramas están en **Mermaid**, que se renderiza en GitHub y en VS Code con la extensión de Mermaid.

## Mapa de documentos

| # | Archivo | Contenido |
| --- | --- | --- |
| — | `README.md` | Este índice |
| L1 | [01-nivel-1-contexto.md](01-nivel-1-contexto.md) | Contexto: el público, los 5 roles y las bases de datos |
| L2 | [02-nivel-2-contenedores.md](02-nivel-2-contenedores.md) | Contenedores: aplicación, MySQL, MongoDB y herramientas de desarrollo |
| L3 | [03-nivel-3-componentes.md](03-nivel-3-componentes.md) | Componentes internos y ciclo de vida de una petición |
| L4 | [04-nivel-4-codigo.md](04-nivel-4-codigo.md) | Clases clave y cómo colaboran |
| Capa | [05-capa-adapters-rest.md](05-capa-adapters-rest.md) | Controladores, DTO, mappers y errores |
| Capa | [06-capa-adapters-usecases.md](06-capa-adapters-usecases.md) | Casos de uso y límite transaccional |
| Capa | [07-capa-adapters-persistence.md](07-capa-adapters-persistence.md) | JPA (MySQL), MongoDB (auditoría) y pago simulado |
| Capa | [08-capa-adapters-security.md](08-capa-adapters-security.md) | JWT, filtro, `SecurityConfig`, BCrypt y principal |
| Capa | [09-capa-domain.md](09-capa-domain.md) | Modelos, objetos de valor, servicios, puertos y excepciones |
| Capa | [10-capa-infrastructure.md](10-capa-infrastructure.md) | `DatabaseSeeder`, arranque y despliegue |
| Flujos | [11-endpoints-flujos.md](11-endpoints-flujos.md) | Matriz de los 108 endpoints y secuencias de los flujos principales |

## Correspondencia capas ↔ código

```text
application/src/main/java/nexusmarket/
├── App.java                 → 10 (arranque)
├── adapters/rest/           → 05 (entrada HTTP)
├── adapters/useCases/       → 06 (implementan domain/ports/in)
├── adapters/persistence/    → 07 (implementan domain/ports/out contra MySQL y MongoDB)
├── adapters/payment/        → 07 (implementa PaymentGatewayPort)
├── adapters/security/       → 08 (implementan JwtServicePort y PasswordServicePort, y la cadena de seguridad)
├── domain/                  → 09 (models, valueobjects, exceptions, services, ports/in, ports/out)
└── infrastructure/seed/     → 10 (DatabaseSeeder)
```

Regla transversal: **las dependencias apuntan al dominio**. Una petición recorre `rest → ports/in → useCases → services → ports/out → persistence`, y el dominio no importa JPA, MongoDB, HTTP, JSON ni Spring Security. De Spring y Lombok solo usa `@Service` y `@RequiredArgsConstructor` en los servicios.

## Convenciones

- `C4Context`, `C4Container` y `C4Component` para los niveles 1 a 3. Mermaid los distribuye según el ancho de la pantalla y no enruta las relaciones, así que en los niveles 2 y 3 algunas líneas pasan cerca de las cajas. En el nivel 1, NexusMarket está en el centro para que ninguna línea cruce otra caja, en pantallas de 1024 píxeles de ancho o más.
- `classDiagram` para el nivel 4 y las capas. Son simplificados: muestran las clases y relaciones que participan en cada flujo, no todo el código.
- `sequenceDiagram` para los flujos. Muestran el camino feliz; los errores están en [la tabla final del documento 11](11-endpoints-flujos.md#9-errores).
- **Correspondencia 1:1:** los nombres de clases, métodos y rutas son los del código, sin traducir. El texto va en español, igual que `README.md` y `SETUP.md`.

## Cómo se verifica

| Comprobación | Herramienta | Qué detecta |
| --- | --- | --- |
| Nombres | `tools/check_sdd_names.py` | Clases, métodos o constantes que no existen en el código, también dentro de los bloques Mermaid |
| Rutas | `tools/check_sdd_names.py` | Cualquier ruta o combinación de método y ruta de estos documentos que no esté en los controladores, y endpoints ausentes de la matriz |
| Sintaxis de los diagramas | `minlag/mermaid-cli` | Diagramas que Mermaid no puede compilar |

La matriz del documento 11 se genera desde los controladores y los casos de uso con `tools/gen_c4_matrix.py`, que además la compara con [Api-rest-endpoints.md](../Adapters/Api-rest-endpoints.md). Las herramientas están descritas en [`tools/README.md`](../../tools/README.md).
