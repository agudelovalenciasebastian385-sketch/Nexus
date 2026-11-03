# Herramientas del proyecto

Scripts de apoyo para verificar el SDD y generar documentos, código y la colección de Postman. La aplicación no los usa para compilar ni para ejecutarse.

## Requisitos

**Python 3.9 o superior**, sin paquetes adicionales: solo usan la biblioteca estándar. Se probaron con Python 3.9 en Linux y 3.11 en Windows.

Ejecútalos desde la raíz del repositorio. Las rutas se calculan desde la ubicación de cada script, así que no dependen de la carpeta actual ni del equipo.

```bash
python tools/check_sdd_names.py
```

En Windows, si `python` no está disponible, usa `py`.

## Verificación

No modifican ningún archivo. Terminan con código de salida 1 si encuentran un problema, así que también sirven en un proceso automático.

| Herramienta | Qué comprueba | Qué necesita |
| --- | --- | --- |
| `check_sdd_names.py` | Que los nombres citados en `SDD/` existan en el código: clases, métodos, constantes, configuración y servicios de Docker. En `SDD/C4/` revisa también los diagramas Mermaid, las rutas contra los controladores y que la matriz del documento 11 tenga todos los endpoints | Nada |
| `e2e_4c.py` | El flujo completo de la API, de la compra a los reportes, en 68 pasos. Crea sus propios usuarios con un sufijo único, así que se puede repetir. Deja esos datos en la base | La aplicación corriendo en `http://localhost:8080` y el administrador inicial con sus credenciales por defecto |

`check_sdd_names.py` lista cada problema por archivo y termina con `files with issues: 0` cuando todo está bien.

## Generadores de documentos

**Sobrescriben los archivos de destino**, así que un cambio hecho a mano en ellos se pierde al volver a generarlos. Cambia la fuente, no el resultado.

| Herramienta | Fuente | Qué escribe |
| --- | --- | --- |
| `gen_docs.py` | `nexus_spec.py` | `SDD/domain/Domain Services.md`, `SDD/domain/services/`, `SDD/domain/Input-ports.md`, `SDD/domain/Output-ports.md` y `SDD/Adapters/Use-cases-adapters.md` |
| `gen_c4_matrix.py` | Los controladores y los casos de uso | La matriz de endpoints de `SDD/C4/11-endpoints-flujos.md`, solo entre sus marcadores. Falla si una fila no coincide con `SDD/Adapters/Api-rest-endpoints.md` |
| `gen_postman.py` | `SDD/Adapters/Api-rest-endpoints.md` para las carpetas por rol. El flujo completo está escrito en el propio script | `postman/NexusMarket.postman_collection.json` y `postman/NexusMarket.postman_environment.json` |

## Generadores de código

También **sobrescriben los archivos de destino**, en este caso código Java de producción. Revisa el resultado con `git diff` y ejecuta las pruebas después de usarlos.

| Herramienta | Fuente | Qué escribe, dentro de `application/src/main/java/nexusmarket/` |
| --- | --- | --- |
| `gen_role_ports.py` | `nexus_spec.py` | `domain/ports/in/` (6 puertos por rol) y `adapters/useCases/` (6 casos de uso) |
| `gen_out_ports.py` | `nexus_spec.py` | `domain/ports/out/` (16 puertos de salida) |
| `gen_catalogs.py` | `nexus_spec.py` | `AuditAction`, `AuditEntityType` y `PaymentResult` en `domain/valueobjects/`, y las excepciones de `NEW_EXCEPTIONS` en `domain/exceptions/` |

## Módulos de apoyo

No se ejecutan directamente.

| Módulo | Contenido |
| --- | --- |
| `nexus_spec.py` | Especificación única de los servicios de dominio, los puertos por rol, los puertos de salida y los catálogos de auditoría. La usan `gen_docs.py` y los generadores de código |
| `javagen.py` | Funciones comunes para generar fuentes Java |

## Comprobación

Todos los generadores están sincronizados con el repositorio: al ejecutarlos sobre una copia, no cambian ningún archivo, salvo finales de línea según el sistema operativo. Si un generador produce diferencias, significa que el código o el documento se editó a mano o que la fuente cambió.
