import os
import sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from nexus_spec import *  # noqa

ROOT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "SDD")
BASE_PKG = "nexusmarket.domain"


def svc_class(s):
    return s["name"] + "Service"


def exposed_in(s):
    return sorted({p for m in s["methods"] for p in m["ports"]})


def is_internal(s):
    return not exposed_in(s)


def exposed_cell(s):
    return ", ".join(f"`{p}`" for p in exposed_in(s)) or "— (internal)"


def sig(m):
    ps = ", ".join(f"{t} {n}" for t, n, _ in m["params"])
    return f"{m['ret']} {m['name']}({ps})"


def by_sub(sub):
    return [s for s in SERVICES if s["sub"] == sub]


def port_methods(port):
    out = []
    for s in SERVICES:
        for m in s["methods"]:
            if port in m["ports"]:
                out.append((s, m))
    return out


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(text.rstrip() + "\n")


# ------------------------------------------------------------ Domain Services.md
def domain_services():
    L = ["# Domain Services — NexusMarket", "",
         "## Introduction", "",
         "This document provides the high-level catalog of the domain services of **NexusMarket**. "
         "Each service implements exactly one business capability and lives in "
         "`nexusmarket.domain.services.<subdomain>`.", "",
         "The detailed specification of every service (authorized roles, input, output, business rules, "
         "validations, exceptions and output ports) is maintained in one document per subdomain inside "
         "[`services/`](services/).", "",
         "## Service Design Principles", "",
         "* **One service per use case.** Each service is a concrete class `<Name>Service` annotated with `@Service` "
         "and `@RequiredArgsConstructor`; it implements no interface. Its operations are exposed through the role "
         "ports of `nexusmarket.domain.ports.in`, whose use case adapters inject the service class directly. "
         "Internal services (authorization, audit registration and loading of the authenticated user) are not "
         "exposed in any role port.",
         "* **Requesting user first.** Every protected operation receives the authenticated `User` "
         "(loaded from the token by the JWT filter) as its first parameter.",
         "* **Authorization first.** Services validate RG-01 (authenticated and `ACTIVE` user), RG-02 (single role) "
         "and RG-03 (no information outside the role) through the authorization services before doing anything else.",
         "* **Output ports only.** Services read and store information exclusively through the output ports of "
         "`nexusmarket.domain.ports.out`; they never depend on databases, JPA, MongoDB, HTTP, JWT or hashing libraries.",
         "* **Rules in the entities.** Services orchestrate; invariants and state transitions are enforced by the "
         "domain entities, which raise domain exceptions.",
         "* **Identifiers and data.** Existing aggregates are referenced by their identifier (`String`); new "
         "information is received as primitives and Value Objects, because the entities are always valid and "
         "cannot be built partially. New identifiers are generated with `UUID.randomUUID()`.",
         "* **Audit.** Every operation that changes information registers an immutable `AuditLog` through "
         "`RegisterAuditLogService`. Read-only operations are not audited.",
         "* **Errors.** Every failure is a subclass of `DomainException` (`nexusmarket.domain.exceptions`).", "",
         "## General Restrictions", "",
         "| Code | Restriction | Enforced by |", "| --- | --- | --- |",
         "| RG-01 | Every operation must be executed by an authenticated user. | `ValidateUserStatusService` |",
         "| RG-02 | Each user has a single role in the system. | `User.role` (fixed by subclass) and `ValidateRoleAuthorizationService` |",
         "| RG-03 | No participant can manage information outside its role. | `ValidateRoleAuthorizationService`, `ValidateSellerOwnershipService`, `ValidateBuyerOwnershipService` |",
         "", "---", ""]
    for sub, title, fname, desc in SUBDOMAINS:
        L += [f"# {title}", "", desc, "",
              f"Package: `{BASE_PKG}.services.{sub}` — detail: [services/{fname}](services/{fname})", "",
              "| Service | Exposed in | Purpose |", "| --- | --- | --- |"]
        for s in by_sub(sub):
            L.append(f"| `{svc_class(s)}` | {exposed_cell(s)} | {s['purpose']} |")
        L += ["", "---", ""]
    internal = [s for s in SERVICES if is_internal(s)]
    L += ["# Service Count", "",
          f"* Domain services: **{len(SERVICES)}**.",
          f"* Services exposed in role ports: **{len(SERVICES) - len(internal)}**.",
          f"* Internal services: **{len(internal)}** ("
          + ", ".join(f"`{svc_class(s)}`" for s in internal) + ")."]
    write(os.path.join(ROOT, "domain", "Domain Services.md"), "\n".join(L))


# ------------------------------------------------------------ services/<sub>-services.md
def subdomain_doc(sub, title, fname, desc):
    ss = by_sub(sub)
    L = [f"# {title} Services", "", "## Introduction", "",
         f"This document specifies the services of the **{title}** subdomain of NexusMarket.", "", desc, "",
         f"All services live in the package `{BASE_PKG}.services.{sub}`. They operate exclusively with Domain "
         "Models and Value Objects and reach external resources only through output ports "
         "(see [Output Ports](../Output-ports.md)).", "",
         "## Services", "", "| # | Service | Exposed in (role ports) | Authorized roles |", "| --- | --- | --- | --- |"]
    for i, s in enumerate(ss, 1):
        L.append(f"| {i} | `{svc_class(s)}` | {exposed_cell(s)} | {s['roles']} |")
    L += ["", "---", ""]
    for i, s in enumerate(ss, 1):
        L += [f"# {i}. {svc_class(s)}", "", "## Description", "", s["purpose"], "", "## Implementation", ""]
        L.append(f"* Class: `{BASE_PKG}.services.{sub}.{svc_class(s)}` (`@Service`, `@RequiredArgsConstructor`).")
        if is_internal(s):
            L.append("* Internal service: it is not exposed in any role port.")
        L += ["", "```java"]
        for m in s["methods"]:
            L.append(sig(m) + ";")
        L += ["```", "", "## Authorized Roles", "", s["roles"], ""]
        exposed = exposed_in(s)
        if exposed:
            L += ["Exposed in: " + ", ".join(f"`{p}`" for p in exposed) + ".", ""]
        for m in s["methods"]:
            head = f"## Input — `{m['name']}`" if len(s["methods"]) > 1 else "## Input"
            L += [head, ""]
            if m["params"]:
                L += ["| Parameter | Type | Description |", "| --- | --- | --- |"]
                L += [f"| `{n}` | `{t}` | {d} |" for t, n, d in m["params"]]
            else:
                L.append("No parameters.")
            L += ["", "**Output:** " + (f"`{m['ret']}`. " if m["ret"] != "void" else "") + m["doc"], ""]
        L += ["## Business Rules", ""] + [f"* {r}" for r in s["rules"]] + [""]
        L += ["## Validations", ""] + [f"* {v}" for v in s["validations"]] + [""]
        L += ["## Exceptions", "", "| Exception | Raised when |", "| --- | --- |"]
        L += [f"| `{e}` | {w} |" for e, w in s["exceptions"]] + [""]
        L += ["## Output Ports Used", ""]
        L += ([f"* `{p}`" for p in s["outports"]] or ["None."]) + [""]
        if s["uses"]:
            L += ["## Collaborating Services", ""] + [f"* `{u}`" for u in s["uses"]] + [""]
        L += ["## Audit", ""]
        L += ([f"* `AuditAction.{a}`" for a in s["audit"]] or ["Not audited (read-only or authentication check)."]
              if s["sub"] not in ("audit",) else ["Not applicable."])
        L += ["", "---", ""]
    write(os.path.join(ROOT, "domain", "services", fname), "\n".join(L))


# ------------------------------------------------------------ Input-ports.md
def input_ports():
    L = ["# Input Ports (Role-Based Use Case Interfaces) — NexusMarket", "", "## 1. Overview", "",
         "Input ports define the entry contracts through which external delivery mechanisms (REST controllers in a "
         "later phase) interact with the core domain.", "",
         "In this architecture, **input ports are organized strictly by system roles (`SystemRole`)**, plus a public "
         "access port for operations that do not require a specific role. This ensures that:", "",
         "* Every role exposes a dedicated, cohesive interface with the exact operations allowed for that role.",
         "* REST controllers and security filters evaluate role permissions directly against the targeted role port.",
         "* Methods operate exclusively on Domain Models and Value Objects; they never receive DTOs.", "",
         "There are no per-service use case interfaces: domain services are concrete classes and each role port is "
         "implemented by a use case adapter that injects them directly (see "
         "[Use Cases Adapters](../Adapters/Use-cases-adapters.md)).", "",
         "---", "", "# 2. General Principles", "", "### 2.1 Role Isolation", "",
         "Each system role interacts with the application through its role port in `nexusmarket.domain.ports.in`:", ""]
    L += [f"* `{port}` " + (f"(`{role}`)" if role else "(Public)") for port, impl, role, desc in ROLE_PORTS]
    L += ["", "A role port only contains the operations allowed to its role (RG-03); the service validates the "
          "role again.", "", "### 2.2 Parameters", "",
          "* Protected methods receive the authenticated `User` (loaded from the token by the JWT filter) as `requestingUser`.",
          "* Existing aggregates are referenced by identifier (`String`), because NexusMarket entities validate their "
          "invariants on construction and cannot be built with only an identifier.",
          "* New information is received as primitives and Value Objects.", "",
          "---", "", "# 3. Input Port Definitions", ""]
    for i, (port, impl, role, desc) in enumerate(ROLE_PORTS, 1):
        L += [f"## 3.{i} {port}" + (f" (`{role}`)" if role else " (Public)"), "", desc, "", "```java",
              f"public interface {port} {{"]
        for s, m in port_methods(port):
            L.append(f"    {sig(m)};")
        L += ["}", "```", ""]
        L += ["| Method | Delegates to |", "| --- | --- |"]
        L += [f"| `{m['name']}` | `{svc_class(s)}` |" for s, m in port_methods(port)] + ["", "---", ""]
    write(os.path.join(ROOT, "domain", "Input-ports.md"), "\n".join(L))


# ------------------------------------------------------------ Output-ports.md
def output_ports():
    L = ["# Output Ports — NexusMarket", "", "## Introduction", "",
         "Output ports define the contracts through which the domain communicates with external resources. The "
         "domain owns these interfaces (`nexusmarket.domain.ports.out`); their implementations belong to the "
         "adapter layer (MySQL, MongoDB, password hashing, JWT and a simulated payment gateway); see "
         "[Adapters](#adapters) below.", "",
         "Domain services must never depend directly on MySQL, MongoDB, JPA, Spring Data, HTTP, REST, JWT or hashing "
         "libraries.", "",
         "## Parameter Rule", "",
         "* Methods that store information receive Domain Models (`save(Order order)`, `update(Order order)`).",
         "* Lookups of an aggregate receive its identifier (`findById(String id)`), because NexusMarket entities "
         "validate their invariants on construction and cannot be built with only an identifier.",
         "* Queries by relationship receive the related Domain Model (`findByBuyer(Buyer buyer)`).",
         "* Date-range queries (`findBy<Date>Between(LocalDateTime from, LocalDateTime toExclusive)`) use the "
         "half-open interval `[from, toExclusive)`: the lower bound is **inclusive** and the upper bound is "
         "**exclusive**. Reports obtain both bounds from `ReportPeriod.startDateTime()` and "
         "`ReportPeriod.endDateTimeExclusive()` (the first moment of the day after the end date), so the end date is "
         "covered completely. Adapters must not translate them into an inclusive SQL BETWEEN (such as a Spring Data "
         "derived query ending in Between); they must use `>= from` and `< toExclusive` (Spring Data keywords "
         "GreaterThanEqual and LessThan).", "",
         "## Dependency Flow", "", "```text", "Domain Service", "      |", "      v", "Output Port (domain/ports/out)",
         "      |", "      v", "Output Adapter (adapters/persistence, adapters/security, adapters/payment)", "      |", "      v",
         "External resource (MySQL, MongoDB, hashing, JWT, payment gateway)", "```", "",
         "## Adapters", "",
         "Every output port is implemented (see [Persistence Adapters](../Adapters/Persistence-adapters.md) and "
         "[Security Adapters](../Adapters/Security-adapters.md)):", "",
         "| Output port | Adapter | Technology |", "| --- | --- | --- |"]
    L += [f"| `{port}` | `{adapter}` | {tech} |" for port, adapter, tech in OUTPUT_ADAPTERS]
    L += ["", "---", ""]
    for i, (name, resp, methods) in enumerate(OUTPUT_PORTS, 1):
        consumers = sorted({svc_class(s) for s in SERVICES if name in s["outports"]})
        L += [f"# {i}. {name}", "", "## Responsibility", "", resp, "", "## Methods", "", "```java",
              f"public interface {name} {{", ""]
        for ret, mname, params in methods:
            L.append(f"    {ret} {mname}({params});")
        L += ["}", "```", "", "## Consumers", ""] + [f"* `{c}`" for c in consumers] + ["", "---", ""]
    write(os.path.join(ROOT, "domain", "Output-ports.md"), "\n".join(L))


# ------------------------------------------------------------ Use-cases-adapters.md
def injected(port):
    seen = []
    for s, m in port_methods(port):
        if svc_class(s) not in seen:
            seen.append(svc_class(s))
    return seen


def use_case_adapters():
    L = ["# Use Cases Adapters — NexusMarket", "", "## 1. Overview", "",
         "Use case adapters live in `nexusmarket.adapters.useCases`. Each one is the concrete implementation of a "
         "role port of `nexusmarket.domain.ports.in` and bridges the incoming calls of the REST controllers "
         "(`nexusmarket.adapters.rest.controllers`, see [Rest-adapters.md](Rest-adapters.md)) to the domain "
         "services of `nexusmarket.domain.services`. Requests reach the controllers only after `SecurityConfig` and "
         "`JwtAuthenticationFilter` have authenticated the user and checked the route role "
         "(see [Rest-security-cors.md](Rest-security-cors.md)).", "",
         "## 2. Responsibilities", "",
         "* Implement exactly one role port.",
         "* Inject the concrete domain service classes (`@Service`, constructor injection with `@RequiredArgsConstructor`).",
         "* Delegate every call directly to the corresponding service; they contain no business rules.", "",
         "```text", "REST Controller (adapters/rest/controllers)", "      |", "      v  calls",
         "Role Port (domain/ports/in)", "      ^", "      |  implemented by", "Use Case Adapter (adapters/useCases)",
         "      |", "      v  injects & delegates to", "Domain Service (domain/services)",
         "      |", "      v  calls", "Output Port (domain/ports/out)", "```", "",
         "## 3. Transaction Management", "",
         "Each use case adapter is the **transaction boundary** of its use cases: every operation runs in one "
         "Spring transaction (all or nothing). Domain services declare no transactions; the internal services they "
         "call (`ReserveInventoryService`, `GenerateInvoiceService`, ...) and the output adapters join the "
         "transaction of the use case, and no use case adapter calls another one.", "",
         "| Annotation | Applies to |", "| --- | --- |",
         "| `@Transactional` (class level) | Every operation that changes state. If any step fails, every MySQL write "
         "of the use case is rolled back. |",
         "| `@Transactional(readOnly = true)` | Queries and reports (methods `consult*`). |",
         "| `@Transactional(noRollbackFor = PaymentRejectedException.class)` | `BuyerUseCaseImpl.validatePayment`: "
         "the only write before the rejection is its `PAYMENT_REJECTED` audit record, so the transaction commits "
         "and the rejection stays audited while the order remains `PENDING_PAYMENT`. |", "",
         "Consequences:", "",
         "* **Audit records** (MongoDB, outside the JPA transaction) are written after the commit; a rolled-back "
         "use case leaves no audit record (see [Persistence-adapters.md](Persistence-adapters.md), section 7).",
         "* **Optimistic locking** of inventories, orders and carts compares the version read at the start of the "
         "use case; a concurrent modification fails the commit and the API answers 409 (see "
         "[Persistence-adapters.md](Persistence-adapters.md), section 5.6).",
         "* The annotations are generated by `tools/gen_role_ports.py` together with the adapters.", "",
         "| Use case adapter | Methods | Read-only | Read-write |", "| --- | --- | --- | --- |"]
    for port, impl, role, desc in ROLE_PORTS:
        names = [m["name"] for _, m in port_methods(port)]
        ro = sum(1 for n in names if n.startswith("consult"))
        L.append(f"| `{impl}` | {len(names)} | {ro} | {len(names) - ro} |")
    L += ["", "## 4. Adapters", "", "| Role port | Use case adapter | Injected domain services |", "| --- | --- | --- |"]
    for port, impl, role, desc in ROLE_PORTS:
        L.append(f"| `{port}` | `{impl}` | " + ", ".join(f"`{c}`" for c in injected(port)) + " |")
    L += ["", "---", ""]
    for port, impl, role, desc in ROLE_PORTS:
        L += [f"## {impl}", "", f"Implements `{port}`. {desc}", "", "| Method | Delegates to |", "| --- | --- |"]
        L += [f"| `{m['name']}` | `{svc_class(s)}.{m['name']}` |" for s, m in port_methods(port)] + [""]
    L += ["---", "", "## 5. Code Pattern", "", "```java", "package nexusmarket.adapters.useCases;", "",
          "@Service", "@Transactional", "@RequiredArgsConstructor",
          "public class AdministratorUseCaseImpl implements AdministratorPort {", "",
          "    private final ChangeUserStatusService changeUserStatusService;", "    // ...", "",
          "    @Override",
          "    public User changeUserStatus(User requestingUser, String userId, UserStatus newStatus) {",
          "        return changeUserStatusService.changeUserStatus(requestingUser, userId, newStatus);", "    }", "",
          "    @Override", "    @Transactional(readOnly = true)",
          "    public User consultUser(User requestingUser, String userId) {",
          "        return consultUserService.consultUser(requestingUser, userId);", "    }",
          "}", "```"]
    write(os.path.join(ROOT, "Adapters", "Use-cases-adapters.md"), "\n".join(L))


if __name__ == "__main__":
    domain_services()
    for sd in SUBDOMAINS:
        subdomain_doc(*sd)
    input_ports()
    output_ports()
    use_case_adapters()
    print("services:", len(SERVICES), "internal:", sum(1 for s in SERVICES if is_internal(s)),
          "output ports:", len(OUTPUT_PORTS))
    for port, *_ in ROLE_PORTS:
        print(port, len(port_methods(port)))
