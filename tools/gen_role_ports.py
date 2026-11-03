"""Generates the role ports (domain/ports/in/<Role>Port) and use case adapters (adapters/useCases/<Role>UseCaseImpl)."""
from javagen import *  # noqa


def port_methods(port):
    return [(s, m) for s in SERVICES for m in s["methods"] if port in m["ports"]]


def lower_first(name):
    return name[0].lower() + name[1:]


# Transaction boundary (Use-cases-adapters.md, section 3): the class is @Transactional; queries and reports
# (methods named consult*) are read-only; a rejected payment commits so its PAYMENT_REJECTED audit survives.
NO_ROLLBACK_FOR = {"validatePayment": "PaymentRejectedException"}


def transactional(method_name):
    if method_name.startswith("consult"):
        return "    @Transactional(readOnly = true)\n"
    if method_name in NO_ROLLBACK_FOR:
        return f"    @Transactional(noRollbackFor = {NO_ROLLBACK_FOR[method_name]}.class)\n"
    return ""


for port, impl, role, desc in ROLE_PORTS:
    pm = port_methods(port)
    types = [t for _, m in pm for t in method_types(m)]
    imps = imports_for(types)
    desc_java = re.sub(r"`([^`]+)`", r"{@code \1}", desc)
    role_txt = f" ({{@code {role}}})" if role else " (public, no authentication required)"

    decls = "\n\n".join(declaration(m["ret"], m["name"], [(t, n) for t, n, _ in m["params"]]) for _, m in pm)
    write(f"domain/ports/in/{port}.java", f"""package nexusmarket.domain.ports.in;
{chr(10) + imps + chr(10) if imps else ''}
/**
 * Role input port{role_txt}.
 *
 * <p>{desc_java}</p>
 */
public interface {port} {{

{decls}
}}
""")

    services = []
    for s, _ in pm:
        if s not in services:
            services.append(s)
    svc_imports = [f"nexusmarket.domain.services.{s['sub']}.{s['name']}Service" for s in services]
    exc_imports = [f"nexusmarket.domain.exceptions.{NO_ROLLBACK_FOR[m['name']]}" for _, m in pm
                   if m["name"] in NO_ROLLBACK_FOR]
    all_imports = sorted(set([l[7:-1] for l in imps.split("\n") if l.startswith("import ")]
                             + svc_imports + exc_imports
                             + ["lombok.RequiredArgsConstructor",
                                "org.springframework.stereotype.Service",
                                "org.springframework.transaction.annotation.Transactional",
                                f"nexusmarket.domain.ports.in.{port}"]))
    java_imps = [i for i in all_imports if i.startswith("java.")]
    other_imps = [i for i in all_imports if not i.startswith("java.")]
    imp_txt = "\n".join(f"import {i};" for i in java_imps) + ("\n\n" if java_imps else "") + \
        "\n".join(f"import {i};" for i in other_imps)
    fields = "\n".join(f"    private final {s['name']}Service {lower_first(s['name'])}Service;" for s in services)
    methods = []
    for s, m in pm:
        call = f"{lower_first(s['name'])}Service.{m['name']}({', '.join(n for _, n, _ in m['params'])})"
        stmt = f"{call};" if m["ret"] == "void" else f"return {call};"
        methods.append(f"""    @Override
{transactional(m['name'])}{declaration(m['ret'], m['name'], [(t, n) for t, n, _ in m['params']], indent='    public ', suffix=' {')}
        {stmt}
    }}""")
    write(f"adapters/useCases/{impl}.java", f"""package nexusmarket.adapters.useCases;

{imp_txt}

/**
 * Use case adapter of {{@link {port}}}: delegates every operation to the
 * corresponding domain service (Use-cases-adapters.md). It contains no business
 * rules.
 *
 * <p>It is the transaction boundary of every use case: each operation runs in one
 * transaction (all or nothing); queries and reports are read-only.</p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class {impl} implements {port} {{

{fields}

{chr(10).join(m + chr(10) for m in methods).rstrip()}
}}
""")
    print(port, len(pm), "methods,", len(services), "services")
