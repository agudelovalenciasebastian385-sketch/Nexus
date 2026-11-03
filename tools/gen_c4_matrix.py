"""Fills the endpoint matrix of SDD/C4/11-endpoints-flujos.md from the controllers and the use cases.

It fails if a row disagrees with the endpoint table of SDD/Adapters/Api-rest-endpoints.md.
"""
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, "application", "src", "main", "java", "nexusmarket", "adapters")
DOC = os.path.join(ROOT, "SDD", "C4", "11-endpoints-flujos.md")

CONTROLLERS = [
    ("AuthRestController", "PublicAccessUseCaseImpl", "PublicAccessPort", "Autenticación"),
    ("PublicRestController", "PublicAccessUseCaseImpl", "PublicAccessPort", "Público"),
    ("BuyerRestController", "BuyerUseCaseImpl", "BuyerPort", "Comprador"),
    ("SellerRestController", "SellerUseCaseImpl", "SellerPort", "Vendedor"),
    ("LogisticsOperatorRestController", "LogisticsOperatorUseCaseImpl", "LogisticsOperatorPort", "Operador logístico"),
    ("AdministratorRestController", "AdministratorUseCaseImpl", "AdministratorPort", "Administrador"),
    ("SupervisorRestController", "SupervisorUseCaseImpl", "SupervisorPort", "Supervisor"),
]


def read(*parts):
    with open(os.path.join(*parts), encoding="utf-8") as source:
        return source.read()


def services_of(use_case):
    """Port method -> service class that the use case delegates to."""
    code = read(JAVA, "useCases", use_case + ".java")
    pattern = r"public [^;{]*?\b(\w+)\([^)]*\)\s*\{\s*(?:return )?(\w+Service)\.\w+\("
    return {method: field[0].upper() + field[1:] for method, field in re.findall(pattern, code)}


def rows_of(controller, use_case):
    code = read(JAVA, "rest", "controllers", controller + ".java")
    base = re.search(r'@RequestMapping\("([^"]+)"\)', code)[1]
    services = services_of(use_case)
    rows = []
    for part in re.split(r"(?=@(?:Get|Post|Put|Patch|Delete)Mapping)", code)[1:]:
        part = part.split("private static")[0]
        mapping = re.match(r'@(\w+)Mapping(?:\("([^"]*)"\))?', part)
        method = re.search(r"\w+Port\.(\w+)\(", part)[1]
        if "noContent" in part:
            status = 204
        elif "HttpStatus.CREATED" in part or "return created(" in part:
            status = 201
        else:
            status = 200
        rows.append((mapping[1].upper(), base + (mapping[2] or ""), method, services[method], status))
    return rows


def spec_rows():
    pattern = r"\| \d+ \| `(\w+)` \| `([^`]+)` \| [^|]+ \| `\w+\.(\w+)` \| .*? \| (\d+)"
    text = read(ROOT, "SDD", "Adapters", "Api-rest-endpoints.md")
    return {(m[0], m[1]): (m[2], int(m[3])) for m in re.findall(pattern, text)}


def main():
    spec = spec_rows()
    sections, total = [], 0
    for controller, use_case, port, title in CONTROLLERS:
        rows = rows_of(controller, use_case)
        total += len(rows)
        table = [f"### {title} — `{controller}`, `{port}`", "",
                 "| Método | Ruta | Método del puerto | Servicio | OK |", "| --- | --- | --- | --- | ---: |"]
        for http, path, method, service, status in rows:
            assert spec[(http, path)] == (method, status), (http, path, method, status, spec[(http, path)])
            table.append(f"| `{http}` | `{path}` | `{method}` | `{service}` | {status} |")
        sections.append("\n".join(table))
    assert total == 108 == len(spec), (total, len(spec))
    text = read(DOC)
    start, end = "<!-- MATRIZ-INICIO: la genera tools/gen_c4_matrix.py -->", "<!-- MATRIZ-FIN -->"
    head, rest = text.split(start)
    tail = rest.split(end)[1]
    with open(DOC, "w", encoding="utf-8", newline="\n") as target:
        target.write(head + start + "\n\n" + "\n\n".join(sections) + "\n\n" + end + tail)
    print("matrix rows:", total)


if __name__ == "__main__":
    main()
