# User Management Services

## Introduction

This document specifies the services of the **User Management** subdomain of NexusMarket.

Registration of buyers and staff users, authentication, user consultation, user status and password management.

All services live in the package `nexusmarket.domain.services.user`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Use case (input port) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `RegisterBuyerUserService` | `RegisterBuyerUserUseCase` | Public (no authentication required). |
| 2 | `RegisterStaffUserService` | `RegisterStaffUserUseCase` | `ADMINISTRATOR`. |
| 3 | `LoginService` | `LoginUseCase` | Public (no authentication required). |
| 4 | `ConsultUserService` | `ConsultUserUseCase` | Any active user over its own information; `ADMINISTRATOR` and `SUPERVISOR` over any user. |
| 5 | `ChangeUserStatusService` | `ChangeUserStatusUseCase` | `ADMINISTRATOR`. |
| 6 | `ChangeUserPasswordService` | `ChangeUserPasswordUseCase` | Any active user over its own password. |

---

# 1. RegisterBuyerUser

## Description

Registers a new buyer who self-registers in the Marketplace, together with its unique shopping cart.

## Implementation

* Class: `nexusmarket.domain.services.user.RegisterBuyerUserService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.RegisterBuyerUserUseCase`.

```java
Buyer registerBuyerUser(String identificationNumber, String fullName, String email, String rawPassword, Address primaryAddress);
```

## Authorized Roles

Public (no authentication required).

Exposed in: `PublicAccessPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `identificationNumber` | `String` | Identity document number. |
| `fullName` | `String` | Official name. |
| `email` | `String` | Access and communication email. |
| `rawPassword` | `String` | Plain password; it is encrypted before being stored. |
| `primaryAddress` | `Address` | Usual delivery address. |

**Output:** `Buyer`. Returns the registered buyer.

## Business Rules

* The buyer is created with `UserStatus.ACTIVE` and `CommercialStatus.ENABLED`.
* The password is encrypted through `PasswordServicePort`; the plain password is never stored.
* A new `Cart` is created and assigned to the buyer (a buyer owns exactly one cart).
* The role is always `BUYER` (RG-02: a single role per user).

## Validations

* `identificationNumber` must not be registered by another user.
* `email` must not be registered by another user.
* `rawPassword` cannot be empty.
* The remaining data is validated by the `Buyer` entity.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UserAlreadyExistsException` | The identification number or the email is already registered. |
| `InvalidUserException` | Empty password or invalid buyer data. |

## Output Ports Used

* `UserRepositoryPort`
* `BuyerRepositoryPort`
* `CartRepositoryPort`
* `PasswordServicePort`

## Collaborating Services

* `RegisterAuditLogService`

## Audit

* `AuditAction.USER_REGISTERED`

---

# 2. RegisterStaffUser

## Description

Registers a staff user: a logistics operator or a supervisor.

## Implementation

* Class: `nexusmarket.domain.services.user.RegisterStaffUserService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.RegisterStaffUserUseCase`.

```java
User registerStaffUser(User requestingUser, SystemRole role, String identificationNumber, String fullName, String email, String rawPassword);
```

## Authorized Roles

`ADMINISTRATOR`.

Exposed in: `AdministratorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `role` | `SystemRole` | `LOGISTICS_OPERATOR` or `SUPERVISOR`. |
| `identificationNumber` | `String` | Identity document number. |
| `fullName` | `String` | Official name. |
| `email` | `String` | Access and communication email. |
| `rawPassword` | `String` | Initial plain password; it is encrypted before being stored. |

**Output:** `User`. Returns the registered `LogisticsOperator` or `Supervisor`.

## Business Rules

* The user is created with `UserStatus.ACTIVE` as a `LogisticsOperator` or a `Supervisor` according to `role`.
* Sellers are not registered here: they are onboarded with `RegisterSeller`.
* The password is encrypted through `PasswordServicePort`.

## Validations

* RG-01 and role `ADMINISTRATOR`.
* `role` must be `LOGISTICS_OPERATOR` or `SUPERVISOR`.
* `identificationNumber` and `email` must be unique.
* `rawPassword` cannot be empty.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active administrator. |
| `UserAlreadyExistsException` | The identification number or the email is already registered. |
| `InvalidUserException` | Invalid staff role, empty password or invalid user data. |

## Output Ports Used

* `UserRepositoryPort`
* `PasswordServicePort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.STAFF_USER_REGISTERED`

---

# 3. Login

## Description

Authenticates a user with its email and password and returns a token.

## Implementation

* Class: `nexusmarket.domain.services.user.LoginService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.LoginUseCase`.

```java
String login(String email, String rawPassword);
```

## Authorized Roles

Public (no authentication required).

Exposed in: `PublicAccessPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `email` | `String` | Registered email. |
| `rawPassword` | `String` | Plain password. |

**Output:** `String`. Returns the token generated by `JwtServicePort`.

## Business Rules

* The user is searched by email; the password is verified through `PasswordServicePort`.
* Only `ACTIVE` users can log in.
* The token is generated through `JwtServicePort` and contains the data needed to rebuild the `User`.

## Validations

* Email and password must be provided and must match a registered user.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `InvalidCredentialsException` | Unknown email or wrong password. |
| `UnauthorizedOperationException` | The user is `BLOCKED` or `INACTIVE`. |

## Output Ports Used

* `UserRepositoryPort`
* `PasswordServicePort`
* `JwtServicePort`

## Collaborating Services

* `RegisterAuditLogService`

## Audit

* `AuditAction.USER_LOGGED_IN`

---

# 4. ConsultUser

## Description

Retrieves the information of a user.

## Implementation

* Class: `nexusmarket.domain.services.user.ConsultUserService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.ConsultUserUseCase`.

```java
User consultUser(User requestingUser, String userId);
```

## Authorized Roles

Any active user over its own information; `ADMINISTRATOR` and `SUPERVISOR` over any user.

Exposed in: `AdministratorPort`, `BuyerPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `userId` | `String` | Identifier of the user to consult. |

**Output:** `User`. Returns the user.

## Business Rules

* Read-only operation: it does not generate audit records.

## Validations

* RG-01.
* RG-03: users other than administrators and supervisors can only consult themselves.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Inactive requesting user or access to another user's information. |
| `EntityNotFoundException` | The user does not exist. |

## Output Ports Used

* `UserRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`

## Audit

Not audited (read-only or authentication check).

---

# 5. ChangeUserStatus

## Description

Changes the operational status of a user (`ACTIVE`, `BLOCKED`, `INACTIVE`).

## Implementation

* Class: `nexusmarket.domain.services.user.ChangeUserStatusService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.ChangeUserStatusUseCase`.

```java
User changeUserStatus(User requestingUser, String userId, UserStatus newStatus);
```

## Authorized Roles

`ADMINISTRATOR`.

Exposed in: `AdministratorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `userId` | `String` | Identifier of the target user. |
| `newStatus` | `UserStatus` | New status. |

**Output:** `User`. Returns the updated user.

## Business Rules

* The change is applied by the `User` entity (`changeStatus`).

## Validations

* RG-01 and role `ADMINISTRATOR`.
* The target user must exist.
* `newStatus` is required.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | The requesting user is not an active administrator. |
| `EntityNotFoundException` | The target user does not exist. |
| `InvalidUserException` | `newStatus` is not provided. |

## Output Ports Used

* `UserRepositoryPort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.USER_STATUS_CHANGED`

---

# 6. ChangeUserPassword

## Description

Changes the password of the requesting user.

## Implementation

* Class: `nexusmarket.domain.services.user.ChangeUserPasswordService` (`@Service`, `@RequiredArgsConstructor`).
* Implements: `nexusmarket.domain.ports.in.ChangeUserPasswordUseCase`.

```java
void changeUserPassword(User requestingUser, String currentPassword, String newPassword);
```

## Authorized Roles

Any active user over its own password.

Exposed in: `AdministratorPort`, `BuyerPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `requestingUser` | `User` | Authenticated user performing the operation (reconstructed from the token). |
| `currentPassword` | `String` | Current plain password. |
| `newPassword` | `String` | New plain password. |

**Output:** No result.

## Business Rules

* A user can only change its own password; changing one's own credential is not a modification of business information, so it is also available to the supervisor.
* The new password is encrypted through `PasswordServicePort`.

## Validations

* RG-01.
* `currentPassword` must match the stored password.
* `newPassword` cannot be empty.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | Inactive requesting user. |
| `InvalidCredentialsException` | The current password does not match. |
| `InvalidUserException` | Empty new password. |

## Output Ports Used

* `UserRepositoryPort`
* `PasswordServicePort`

## Collaborating Services

* `ValidateRoleAuthorizationService`
* `RegisterAuditLogService`

## Audit

* `AuditAction.USER_PASSWORD_CHANGED`

---
