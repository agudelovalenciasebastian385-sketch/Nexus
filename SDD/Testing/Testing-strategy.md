# Testing Strategy — NexusMarket

## 1. Test types

Tests live in `application/src/test`. The naming suffix decides which Maven plugin runs them.

| Group | Classes | Tests | Needs | What it covers |
| --- | --- | --- | --- | --- |
| Domain | 20 (`domain/models`, `domain/valueobjects`) | 243 | Nothing | Business rules of the models and value objects, without Spring. |
| Services | 14 (`domain/services`) | 315 | Nothing | The domain services over in-memory repositories and fakes (section 3), without Spring. |
| Adapters without Spring | 18 (`adapters`) | 366 | Nothing | JWT, BCrypt, simulated payment gateway, JPA, MongoDB and REST mappers, and the six use cases over the real services (section 7). |
| Web | 11 (`*WebTest`) | 462 | Nothing | The successful path of the 108 endpoints, the endpoint guard, request validation, the exception table and the security chain, with `@WebMvcTest` and mocked role ports. |
| Integration | 3 (`*IT`) | 13 | MySQL and MongoDB containers | Transactions, after-commit audit, optimistic locking, the end-to-end purchase flow and the health endpoint. |

The first four groups are the 1386 unit tests. Every test has a descriptive English name, one verification and an arrange-act-assert structure.

## 2. How to run them

Run from `application/`:

| Command | Runs | Docker |
| --- | --- | --- |
| `./mvnw test` | Surefire: every `*Test` class (1386 tests). | Not needed |
| `./mvnw verify` | Surefire, then Failsafe: every `*IT` class (13 tests), then the three coverage reports. | MySQL and MongoDB containers running |

Start the containers with `docker compose up -d` from the repository root.

`tools/e2e_4c.py` runs the same flow manually against a running application. It is not part of the Maven build, so its requests do not count toward coverage.

## 3. In-memory repositories and their limitation

The service and use case tests use `InMemoryOrderRepository` and its siblings, `FakePasswordService`, `FakeJwtService` and `FakePaymentGateway`, all wired by `ServiceHarness` in `domain/services/support`. The shared test data is in `DomainFixtures`.

**Limitation:** these repositories keep objects in plain maps and return the same instance that was stored. A service that changes a model and forgets to call `update()` still passes its service test, because the next read sees the changed object. Only the `*IT` classes detect a forgotten `update()`, because they read back from a real database.

For the same reason, service tests do not exercise transaction rollback or `@Version` optimistic locking. `TransactionalConsistencyIT` covers those.

## 4. Test profile

Integration tests use `@ActiveProfiles("test")`, which loads `src/test/resources/application-test.yml`. They use the same containers as development, with a separate database named `nexusmarket_test` in both MySQL and MongoDB, so test data never reaches the development databases. The profile also sets `app.payment.simulated.approval-limit` to `1000.00`, so a test can trigger a rejected payment.

## 5. Mockito only in web tests

Mockito is used only by the web tests. `RolePortsWebTestBase` replaces the six role ports with mocks, and `SecuredWebTestBase` and `SecurityConfigWebTest` also mock `UserRepositoryPort`. Everything else uses the real class or an in-memory fake.

| Class | Tests | Spring slice | Verifies |
| --- | --- | --- | --- |
| `RequestValidationWebTest` | 40 | `@WebMvcTest`, filters off | Invalid requests answer 400 and never reach a role port. Malformed dates and JSON answer 400 with `message` and no `errors`. |
| `GlobalExceptionHandlerWebTest` | 63 | `@WebMvcTest`, filters off | The 27 domain exceptions, persistence conflicts, 500 without internal details, 404, 405 and 415. |
| `SecurityConfigWebTest` | 29 | `@WebMvcTest` with the real security chain | Public routes; 401, 403 and 200 per role; outdated, altered and expired tokens; blocked and deleted users; logout. |
| The seven endpoint classes (section 6) | 326 | `@WebMvcTest` with the real security chain | The successful path of every endpoint. |
| `EndpointCoverageGuardWebTest` | 4 | `@WebMvcTest` with the real security chain | Every registered endpoint has a test (section 6.2). |

## 6. Endpoint tests

There is one class per controller, each with a table of `EndpointCase` rows returned by a static `endpoints()` method. `EndpointWebTestBase` runs each row in three tests, and every endpoint appears by method and route in the report:

| Test | Verifies |
| --- | --- |
| `answersTheDocumentedStatus` | The 200, 201 or 204 of [Api-rest-endpoints.md](../Adapters/Api-rest-endpoints.md). |
| `delegatesToThePortWithExactArguments` | The port method, the authenticated user and the arguments: route ids, body fields converted to the domain, query parameters, `ReportPeriod` and fixed catalogs such as `WarehouseType.SELLER`. No other call reaches any role port. |
| `responseHasMainFieldsAndNoPassword` | The main JSON fields with their values, with no password, password hash or token version. A 204 has an empty body. |

| Class | Endpoints | Tests |
| --- | ---: | ---: |
| `AuthRestControllerWebTest` | 3 | 9 |
| `PublicRestControllerWebTest` | 2 | 6 |
| `BuyerRestControllerWebTest` | 17 | 52 |
| `SellerRestControllerWebTest` | 20 | 60 |
| `LogisticsOperatorRestControllerWebTest` | 17 | 52 |
| `AdministratorRestControllerWebTest` | 27 | 81 |
| `SupervisorRestControllerWebTest` | 22 | 66 |
| **Total** | **108** | **326** |

The Buyer and Logistics classes each have one extra test for a second form of a route: removing a cart item without a variant, and an inventory adjustment without a condition.

### 6.1. Authenticated user

Each request uses the `user(...)` post-processor of Spring Security Test with an `AuthenticatedUserPrincipal` that wraps the domain `User` of the correct role. The real security chain runs, so the controller receives the same principal as in production and the role rule of the route also applies. The delegation test verifies that the port receives that same `User` instance.

### 6.2. Endpoint guard

`EndpointCoverageGuardWebTest` reads the endpoints registered in `RequestMappingHandlerMapping` and compares them with the seven tables. It excludes only Spring's own handlers, so a controller in any package of the project counts.

| Test | Fails when |
| --- | --- |
| `theApiHasExactlyTheExpectedNumberOfEndpoints` | The API does not have exactly 108 endpoints. |
| `everyRegisteredEndpointHasATest` | An endpoint has no row. The message names it, for example `Endpoints without a test: [GET /api/v1/seller/orders/{orderId}]`. |
| `everyTestedEndpointIsRegistered` | A row belongs to an endpoint that no longer exists. |
| `noEndpointIsTestedTwice` | Two rows declare the same endpoint. |

**When adding an endpoint:** add its row to the table of its controller and change the expected count in the guard. A new controller also needs its table added to the guard.

The guard compares the tables with the registered routes, not with [Api-rest-endpoints.md](../Adapters/Api-rest-endpoints.md). Keeping that document in sync remains a manual step.

## 7. Use case tests

There is one class per use case in `adapters/useCases`, built by `UseCaseFactory` over the real services and in-memory repositories of a new `ServiceHarness` per test, without Mockito. Each method of the role port has one successful case, which checks the effect in the repositories, the audit trail or the returned value.

| Class | Tests |
| --- | ---: |
| `PublicAccessUseCaseImplTest` | 5 |
| `BuyerUseCaseImplTest` | 17 |
| `SellerUseCaseImplTest` | 20 |
| `LogisticsOperatorUseCaseImplTest` | 17 |
| `AdministratorUseCaseImplTest` | 27 |
| `SupervisorUseCaseImplTest` | 22 |
| `UseCaseTransactionalAnnotationsTest` | 115 |

`UseCaseTransactionalAnnotationsTest` reads the methods of each role port by reflection and checks:

- Each of the six classes has `@Transactional` at class level.
- Every method whose name starts with "consult" is `readOnly = true`, and every other method is not. A new port method is therefore classified by its name.
- `validatePayment` has `noRollbackFor = PaymentRejectedException.class`.

This test proves that the annotations exist, not that transactions behave correctly. `TransactionalConsistencyIT` covers the behavior.

## 8. Coverage

JaCoCo produces three reports. Each one has an `index.html` to browse and a `jacoco.csv` with the raw numbers.

| Report | Folder | Data file | Generated by | Reflects |
| --- | --- | --- | --- | --- |
| Unit | `target/site/jacoco` | `target/jacoco.exec` | `./mvnw test` | The 1386 unit tests. |
| Integration | `target/site/jacoco-it` | `target/jacoco-it.exec` | `./mvnw verify` | The 13 integration tests. |
| Combined | `target/site/jacoco-merged` | `target/jacoco-merged.exec` | `./mvnw verify` | Both, merged. This is the real total. |

Line coverage, with branch coverage in parentheses, measured on 2026-11-03 with `./mvnw verify`:

| Area | Unit | Integration | Combined |
| --- | --- | --- | --- |
| `nexusmarket.adapters.useCases` | 100% (no branches) | 27.2% (no branches) | 100% (no branches) |
| `nexusmarket.adapters.rest.controllers` | 100% (100%) | 41.7% (0%) | 100% (100%) |
| `adapters` | 66.8% (36.2%) | 73.8% (52.1%) | 91.8% (61.0%) |
| `domain` | 90.1% (78.3%) | 63.5% (46.2%) | 90.1% (78.3%) |
| Total | 80.8% (67.5%) | 67.5% (47.8%) | 90.7% (73.9%) |

The first two rows are part of `adapters`. The total covers all of `nexusmarket`, including `infrastructure` and `App`, so it is not an average of the rows above it.

**Measuring again:** the JaCoCo agent appends to existing `.exec` files, so repeated runs accumulate. Delete `target/*.exec` before a `./mvnw verify` whose numbers you plan to report.

**Counting tests:** use the Maven console totals. `target/surefire-reports` can keep XML files from renamed classes until the folder is cleaned.

### What this coverage does not show

- Most endpoints are tested in two separate layers: the web layer with mocked ports, and the use cases over in-memory repositories. Only the integration tests run the whole stack with real databases, so a forgotten `update()` (section 3) in a flow they do not exercise goes undetected.
- Branch coverage of `adapters` is lower than line coverage (61.0%), mainly in `nexusmarket.adapters.persistence`.
