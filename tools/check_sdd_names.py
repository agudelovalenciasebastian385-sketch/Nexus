import glob
import os
import re

# Forward slashes, because the paths below are built with "/" and passed to glob.
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__))).replace(os.sep, "/")
JAVA = glob.glob(ROOT + "/application/src/main/java/**/*.java", recursive=True) \
    + glob.glob(ROOT + "/application/src/test/java/**/*.java", recursive=True)
YML = [ROOT + "/application/src/main/resources/application.yml",
       ROOT + "/application/src/test/resources/application-test.yml"]

classes, methods, fields, constants, packages = set(), set(), set(), set(), set()
for f in JAVA:
    s = open(f, encoding="utf-8").read()
    classes.add(os.path.basename(f)[:-5])
    packages.add(re.search(r"^package ([\w.]+);", s, re.M).group(1))
    methods |= set(re.findall(r"^\s+(?:public |protected |private |static |abstract |final )*[\w<>, ?\[\].]+ (\w+)\([^;{]*\)(?:\s+throws [\w, .]+)?\s*[{;]", s, re.M))
    methods |= set(re.findall(r"^\s+(\w+)\([^)]*\);", s, re.M))  # interface methods without modifiers
    fields |= set(re.findall(r"^\s+private (?:final )?[\w<>, ]+ (\w+)(?: =.*)?;", s, re.M))
    constants |= set(re.findall(r"^\s+([A-Z][A-Z0-9_]+)\(\"", s, re.M))
    for sig in re.findall(r"\w+\(([^()]*)\)\s*[{;]", s):
        fields |= set(re.findall(r"(\w+)\s*(?:,|$)", sig))
for p in list(packages):
    parts = p.split(".")
    for i in range(1, len(parts) + 1):
        packages.add(".".join(parts[:i]))


# Persistence names declared in the adapters: tables, columns and collections (@Table, @Column,
# @CollectionTable, @JoinColumn, @OrderColumn, @AttributeOverride, @Document).
db_names = set()
# Library types imported by the project (BCryptPasswordEncoder, CommandLineRunner, JpaRepository...).
imported_types = set()
# Constants of library types referenced by the code (LockModeType.OPTIMISTIC_FORCE_INCREMENT, ...).
library_constants = set()
# Spring profiles activated by the tests (@ActiveProfiles("test")).
profiles = set()
for f in JAVA:
    s = open(f, encoding="utf-8").read()
    db_names |= set(re.findall(r'(?:name|collection) = "(\w+)"', s))
    imported_types |= set(re.findall(r"^import (?:static )?[\w.]+\.([A-Z]\w*);", s, re.M))
    library_constants |= set(re.findall(r"\b[A-Z]\w*\.([A-Z][A-Z0-9_]{2,})\b", s))
    profiles |= set(re.findall(r'@ActiveProfiles\("(\w+)"\)', s))
    # Query parameters of the REST controllers (@RequestParam ... Type name).
    fields |= set(re.findall(r"@RequestParam(?:\([^)]*\))?\s+(?:@\w+(?:\([^)]*\))?\s+)*[\w<>]+\s+(\w+)", s))
# Last segments of the project packages (jpa, mongodb, security, payment, seed...), used in paths.
package_segments = {p.split(".")[-1] for p in packages}

# Configuration keys (dotted paths) and environment variables declared in application.yml.
# Database names of the connection URLs (nexusmarket, nexusmarket_test).
config_keys, env_vars, database_names = set(), set(), set()
for yml in YML:
    stack = []
    for line in open(yml, encoding="utf-8"):
        database_names |= set(re.findall(r"localhost:\d+/(\w+)", line))
        m = re.match(r"^(\s*)([\w-]+):", line)
        if not m or line.lstrip().startswith("#"):
            continue
        depth = len(m.group(1)) // 2
        stack = stack[:depth] + [m.group(2)]
        for i in range(1, len(stack) + 1):
            config_keys.add(".".join(stack[:i]))
        env_vars |= set(re.findall(r"\$\{(\w+):", line))

# Service names, images and credentials of docker-compose.yml (development environment).
compose = open(ROOT + "/docker-compose.yml", encoding="utf-8").read()
compose_names = set(re.findall(r"^  ([\w-]+):\s*$", compose, re.M))
compose_names |= {img.split(":")[0] for img in re.findall(r"image: ([\w./:-]+)", compose)}
compose_names |= set(re.findall(r"(?:PASSWORD|USERNAME): (\w+)", compose))

JAVA_STD = {"String", "int", "void", "boolean", "BigDecimal", "LocalDateTime", "List", "Optional", "Map", "UUID",
            "RuntimeException", "Integer", "Long", "Set", "Object", "IllegalStateException", "Exception"}
FRAMEWORK = {"Service", "RequiredArgsConstructor", "SpringBootApplication", "Transactional", "Entity", "Document",
             "Getter", "Setter", "NoArgsConstructor",
             "AuthenticationEntryPoint", "AccessDeniedHandler",  # Spring Security, configured with lambdas
             "ObjectOptimisticLockingFailureException"}  # Spring ORM translation of an optimistic lock failure
# Classes specified in the SDD for a later step; remove each one once it exists in the code.
PLANNED = set()
known_types = classes | JAVA_STD | FRAMEWORK | imported_types | PLANNED
known_members = methods | fields | db_names | package_segments | database_names | profiles | {"tools"}
constants |= env_vars | library_constants
packages |= config_keys
# Words that are legitimately written in code style but are not Java identifiers of this project.
ALLOW = {"enum", "final", "services", "adapters", "useCases", "application", "src", "resources", "nexusmarket",
         "user", "audit", "authorization", "inventory", "returns", "shipment", "@Service", "@RequiredArgsConstructor", "@SpringBootApplication", "@Entity", "@Document", "@Transactional",
         "application.yml", "docker-compose.yml", "UUID.randomUUID()", "local@domain.tld", "null", "true", "false",
         "services/", "enums", "infrastructure", "adapters/useCases", "requestingUser", "reporting",
         "HALF_UP",  # HALF_UP: java.math.RoundingMode constant
         "IDENTITY",  # jakarta.persistence.GenerationType.IDENTITY
         "sub", "jti", "ver", "iat", "exp",  # JWT claims (JwtClaimsMapper)
         "Between", "GreaterThanEqual", "LessThan",  # Spring Data derived-query keywords
         "http",
         "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS",  # HTTP methods
         "ERROR", "WARN",  # log levels
         "America", "Bogota", "TZ",  # time zone
         "UP", "DOWN", "management",  # actuator health
         "argLine", "pom.xml", "postman", "springdoc", "bearerAuth",  # Maven, the API collection and OpenAPI
         "C4Context", "C4Container", "C4Component", "classDiagram", "sequenceDiagram", "minlag",  # Mermaid
         "README.md", "SETUP.md", "curl", "mongosh", "ping", "localhost", "user.getId",
         "serverTimezone", "noRollbackFor",
         "DATETIME", "ENTRYPOINT", "Dockerfile", "LocalDateTime.now",  # MySQL, Docker and the JDK
         "target", "site", "jacoco", "index.html", "jacoco.csv", "jacoco.exec", ".exec",  # JaCoCo reports and data files
         "json", "Authorization", "hasRole", "ROLE_", "SessionCreationPolicy.STATELESS"} | compose_names


def check_token(tok):
    t = tok.strip()
    if not t or t in ALLOW or t in JAVA_STD:   # JAVA_STD includes primitive types such as int
        return None
    if re.fullmatch(r"-?\d+(\.\d+)?", t):      # numeric literals such as 0.00
        return None
    t = re.sub(r"\(.*\)$", "", t)          # method(...) -> method
    t = t.rstrip(":;,.")
    if t in ALLOW:
        return None
    if re.fullmatch(r"nexusmarket(\.\w+)+", t) and t.split(".")[-1] in classes:
        return None
    if re.fullmatch(r"[\w./ ]+/?", t) and ("/" in tok or t.endswith(".java")):
        return None
    if re.fullmatch(r"[a-z]+(\.[a-zA-Z]+)+", t):          # package names
        return None if t in packages or t.startswith("nexusmarket.adapters.") or t.startswith("nexusmarket.domain.services") else f"package {t}"
    if "." in t and re.fullmatch(r"[\w.]+", t):
        owner, member = t.split(".", 1)
        if "." in member:
            return None
        if owner in classes or owner in known_types:
            if member in known_members or member in constants or member in classes:
                return None
            return f"member {t}"
        return None if owner[0].islower() else f"type {owner} in {t}"
    if re.fullmatch(r"[A-Z][A-Z0-9_]+", t):
        return None if t in constants or re.fullmatch(r"RG-\d+", t) else f"constant {t}"
    if re.fullmatch(r"[A-Z]\w*(<[\w<>, ?]+>)?\??", t):
        base = re.findall(r"[A-Z]\w*", t)
        missing = [b for b in base if b not in known_types]
        return f"type {', '.join(missing)}" if missing else None
    if re.fullmatch(r"[a-z]\w*", t):
        return None if t in known_members or len(t) < 3 else f"member {t}"
    return None


problems = {}
for md in glob.glob(ROOT + "/SDD/**/*.md", recursive=True):
    text = open(md, encoding="utf-8").read()
    text = re.sub(r"```.*?```", "", text, flags=re.S)  # code blocks checked separately
    for tok in re.findall(r"`([^`\n]+)`", text):
        if re.search(r"(^|\s)/([\w{]|\*\*)", tok):  # REST routes such as `POST /api/v1/auth/login` or `/profile`
            continue
        for piece in re.split(r"\s*(?:/|→|->|,| or | \+ )\s*", tok):
            r = check_token(piece)
            if r:
                problems.setdefault(os.path.relpath(md, ROOT), set()).add(r)
# ---------------------------------------------------------------------------- C4: diagrams and routes

# Library names written inside diagrams that the project code does not import by that exact name.
DIAGRAM_ALLOW = {"ResponseEntity", "HttpSecurity", "LocalDateTime", "LocalDate", "BigDecimal",
                 "C4Context", "C4Container", "C4Component", "SystemDb", "ContainerDb", "UpdateLayoutConfig", "UpdateRelStyle",  # Mermaid C4
                 "MySQL", "MongoDB", "NexusMarket"}  # product names
# Mermaid syntax and words that look like a method call in a diagram.
# corsConfigurationSource exists, but its @Value("${...}") parameter hides it from the method pattern.
DIAGRAM_CALL_ALLOW = {"hasRole", "stateDiagram", "corsConfigurationSource"}
HTTP_METHODS = "GET|POST|PUT|PATCH|DELETE"


def controller_routes():
    """(HTTP method, path) of every mapping declared by the REST controllers."""
    routes = set()
    for java in glob.glob(ROOT + "/application/src/main/java/nexusmarket/adapters/rest/controllers/*.java"):
        code = open(java, encoding="utf-8").read()
        base = re.search(r'@RequestMapping\("([^"]+)"\)', code).group(1)
        for verb, path in re.findall(r'@(Get|Post|Put|Patch|Delete)Mapping(?:\("([^"]*)"\))?', code):
            routes.add((verb.upper(), base + path))
    return routes


def check_diagram(block_text):
    found = set()
    for name in re.findall(r"\b[A-Z][a-z0-9]+(?:[A-Z][a-z0-9]*)+\b", block_text):
        if name not in known_types and name not in DIAGRAM_ALLOW:
            found.add(f"diagram type {name}")
    # Method calls in messages and class members. Quoted C4 descriptions are prose, so they are skipped.
    without_strings = re.sub(r'"[^"\n]*"', '""', block_text)
    for name in re.findall(r"\b([a-z][a-zA-Z0-9]+)\(", without_strings):
        if name not in known_members and name not in DIAGRAM_CALL_ALLOW:
            found.add(f"diagram method {name}")
    return found


def check_routes(text, routes):
    found = set()
    paths = {path for _, path in routes}
    prefixes = {re.match(r"(/api/v1/\w+)", path).group(1) for path in paths}
    for verb, path in re.findall(rf"\b({HTTP_METHODS})`?(?: \| `| )(/api/v1/[\w/{{}}.-]*)", text):
        if path.endswith("/"):        # a prefix such as POST /api/v1/auth/register/**
            continue
        if (verb, path) not in routes:
            found.add(f"route {verb} {path}")
    for path in re.findall(r"/api/v1/[\w/{}.-]*", text):
        clean = path.rstrip("/.")
        if clean not in paths and clean not in prefixes and not path.endswith("/"):
            found.add(f"route {clean}")
    return found


routes = controller_routes()
for md in glob.glob(ROOT + "/SDD/C4/*.md"):
    text = open(md, encoding="utf-8").read()
    relative = os.path.relpath(md, ROOT)
    for diagram in re.findall(r"```mermaid\n(.*?)```", text, flags=re.S):
        for issue in check_diagram(diagram):
            problems.setdefault(relative, set()).add(issue)
    for issue in check_routes(text, routes):
        problems.setdefault(relative, set()).add(issue)

matrix = ROOT + "/SDD/C4/11-endpoints-flujos.md"
if os.path.exists(matrix):
    rows = set(re.findall(rf"^\| `({HTTP_METHODS})` \| `(/api/v1/[^`]+)` \|", open(matrix, encoding="utf-8").read(), flags=re.M))
    for verb, path in sorted(routes - rows):
        problems.setdefault(os.path.relpath(matrix, ROOT), set()).add(f"endpoint missing from the matrix: {verb} {path}")

for md, items in sorted(problems.items()):
    print(md)
    for i in sorted(items):
        print("   ", i)
print("files with issues:", len(problems))
raise SystemExit(1 if problems else 0)
