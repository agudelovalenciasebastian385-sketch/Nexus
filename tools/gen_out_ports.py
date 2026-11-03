from javagen import *  # noqa

for name, resp, methods in OUTPUT_PORTS:
    types = []
    for ret, _, params in methods:
        types.append(ret)
        types += [p.strip().rsplit(" ", 1)[0] for p in params.split(",") if p.strip()]
    imps = imports_for(types)
    body = "\n\n".join(f"    {ret} {m}({params});" for ret, m, params in methods)
    resp_java = re.sub(r"`([^`]+)`", r"{@code \1}", resp)
    write(f"domain/ports/out/{name}.java", f"""package nexusmarket.domain.ports.out;
{chr(10) + imps + chr(10) if imps else ''}
/**
 * Output port: {resp_java}
 */
public interface {name} {{

{body}
}}
""")
print(len(OUTPUT_PORTS), "output ports")
