# User Management Services

## Introduction

This document specifies the services of the **User Management** subdomain of NexusMarket.

Registration of buyers and staff users, authentication and logout, loading of the authenticated user, user consultation, user status and password management.

All services live in the package `nexusmarket.domain.services.user`. They operate exclusively with Domain Models and Value Objects and reach external resources only through output ports (see [Output Ports](../Output-ports.md)).

## Services

| # | Service | Exposed in (role ports) | Authorized roles |
| --- | --- | --- | --- |
| 1 | `RegisterBuyerUserService` | `PublicAccessPort` | Public (no authentication required). |
| 2 | `RegisterStaffUserService` | `AdministratorPort` | `ADMINISTRATOR`. |
| 3 | `LoginService` | `PublicAccessPort` | Public (no authentication required). |
| 4 | `LogoutService` | `PublicAccessPort` | Any authenticated user (exposed in the public port so that every role can log out). |
| 5 | `LoadAuthenticatedUserService` | — (internal) | Internal (used by the JWT filter of the security phase). |
| 6 | `ConsultUserService` | `AdministratorPort`, `BuyerPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` | Any active user over its own information; `ADMINISTRATOR` and `SUPERVISOR` over any user. |
| 7 | `ChangeUserStatusService` | `AdministratorPort` | `ADMINISTRATOR`. |
| 8 | `ChangeUserPasswordService` | `AdministratorPort`, `BuyerPort`, `LogisticsOperatorPort`, `SellerPort`, `SupervisorPort` | Any active user over its own password. |

---

# 1. RegisterBuyerUserService

## Description

Registers a new buyer who self-registers in the Marketplace, together with its unique shopping cart.

## Implementation

* Class: `nexusmarket.domain.services.user.RegisterBuyerUserService` (`@Service`, `@RequiredArgsConstructor`).

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

# 2. RegisterStaffUserService

## Description

Registers a staff user: a logistics operator or a supervisor.

## Implementation

* Class: `nexusmarket.domain.services.user.RegisterStaffUserService` (`@Service`, `@RequiredArgsConstructor`).

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
* Sellers are not registered here: they are onboarded with `RegisterSellerService`.
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

# 3. LoginService

## Description

Authenticates a user with its email and password and returns the authenticated user together with its token.

## Implementation

* Class: `nexusmarket.domain.services.user.LoginService` (`@Service`, `@RequiredArgsConstructor`).

```java
AuthenticationResult login(String email, String rawPassword);
```

## Authorized Roles

Public (no authentication required).

Exposed in: `PublicAccessPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `email` | `String` | Registered email. |
| `rawPassword` | `String` | Plain password. |

**Output:** `AuthenticationResult`. Returns the `AuthenticationResult` with the authenticated user and the token generated by `JwtServicePort`.

## Business Rules

* The user is searched by email; the password is verified through `PasswordServicePort`.
* Only `ACTIVE` users can log in.
* The token is generated through `JwtServicePort`; it carries the user identifier and its current `authTokenVersion`, plus the data needed to rebuild the `User`.

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

# 4. LogoutService

## Description

Logs out the authenticated user by invalidating every token previously issued to it.

## Implementation

* Class: `nexusmarket.domain.services.user.LogoutService` (`@Service`, `@RequiredArgsConstructor`).

```java
void logout(User user);
```

## Authorized Roles

Any authenticated user (exposed in the public port so that every role can log out).

Exposed in: `PublicAccessPort`.

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `user` | `User` | Authenticated user (reconstructed from the token). |

**Output:** No result.

## Business Rules

* The stored user is loaded through `UserRepositoryPort` and its `authTokenVersion` is advanced (`User.bumpAuthTokenVersion`).
* Tokens carrying the previous version are rejected from then on.

## Validations

* The user must be provided and must exist.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | No user is provided. |
| `EntityNotFoundException` | The user does not exist. |

## Output Ports Used

* `UserRepositoryPort`

## Collaborating Services

* `RegisterAuditLogService`

## Audit

* `AuditAction.USER_LOGGED_OUT`

---

# 5. LoadAuthenticatedUserService

## Description

Loads the stored user identified by the token so that the JWT filter can validate its token version and status.

## Implementation

* Class: `nexusmarket.domain.services.user.LoadAuthenticatedUserService` (`@Service`, `@RequiredArgsConstructor`).
* Internal service: it is not exposed in any role port.

```java
User loadById(String userId);
```

## Authorized Roles

Internal (used by the JWT filter of the security phase).

## Input

| Parameter | Type | Description |
| --- | --- | --- |
| `userId` | `String` | Identifier of the user carried in the token. |

**Output:** `User`. Returns the stored user.

## Business Rules

* Read-only operation: it does not generate audit records.

## Validations

* `userId` must be provided and must belong to a registered user.

## Exceptions

| Exception | Raised when |
| --- | --- |
| `UnauthorizedOperationException` | No user identifier is provided. |
| `EntityNotFoundException` | The user does not exist. |

## Output Ports Used

* `UserRepositoryPort`

## Audit

Not audited (read-only or authentication check).

---

# 6. ConsultUserService

## Description

Retrieves the information of a user.

## Implementation

* Class: `nexusmarket.domain.services.user.ConsultUserService` (`@Service`, `@RequiredArgsConstructor`).

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

# 7. ChangeUserStatusService

## Description

Changes the operational status of a user (`ACTIVE`, `BLOCKED`, `INACTIVE`).

## Implementation

* Class: `nexusmarket.domain.services.user.ChangeUserStatusService` (`@Service`, `@RequiredArgsConstructor`).

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
* Changing the status advances the user's `authTokenVersion`, invalidating its previous tokens.

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

# 8. ChangeUserPasswordService

## Description

Changes the password of the requesting user.

## Implementation

* Class: `nexusmarket.domain.services.user.ChangeUserPasswordService` (`@Service`, `@RequiredArgsConstructor`).

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
* Changing the password advances the user's `authTokenVersion`, invalidating its previous tokens.

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
