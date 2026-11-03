import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from nexus_spec import AUDIT_ACTIONS, AUDIT_ENTITY_TYPES, NEW_EXCEPTIONS

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = os.path.join(ROOT, "application", "src", "main", "java", "nexusmarket", "domain")


def enum(name, doc, values):
    lines = [f'    {c}("{c}", "{n}",\n            "{d}")' if len(c) + len(n) + len(d) > 80 else f'    {c}("{c}", "{n}", "{d}")'
             for c, n, d in values]
    body = ",\n".join(lines) + ";"
    return f'''package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * {name} (business catalog).
 *
 * <p>{doc}</p>
 */
public enum {name} implements DomainCatalog {{

{body}

    private final String code;
    private final String name;
    private final String description;

    {name}(String code, String name, String description) {{
        this.code = code;
        this.name = name;
        this.description = description;
    }}

    @Override
    public String getCode() {{
        return code;
    }}

    @Override
    public String getName() {{
        return name;
    }}

    @Override
    public String getDescription() {{
        return description;
    }}

    /** Resolves a catalog value from its unique business code. */
    public static {name} fromCode(String code) {{
        for ({name} value : values()) {{
            if (value.code.equals(code)) {{
                return value;
            }}
        }}
        throw new InvalidCatalogCodeException("{name}", code);
    }}
}}
'''


def write(rel, text):
    with open(os.path.join(BASE, rel), 'w', encoding='utf-8', newline='\n') as f:
        f.write(text)


write('valueobjects/AuditAction.java', enum('AuditAction',
      'Classifies the business action recorded by an {@code AuditLog}. Every service that changes information\n'
      ' * registers one or more of these actions.', AUDIT_ACTIONS))
write('valueobjects/AuditEntityType.java', enum('AuditEntityType',
      'Classifies the type of entity affected by an audited operation.', AUDIT_ENTITY_TYPES))
write('valueobjects/PaymentResult.java', enum('PaymentResult',
      'Result returned by {@code PaymentGatewayPort.processPayment(order)}.',
      [("APPROVED", "Approved", "The payment gateway approved the payment."),
       ("REJECTED", "Rejected", "The payment gateway rejected the payment.")]))

for name, params, doc in NEW_EXCEPTIONS:
    if name == 'EntityNotFoundException':
        ctor = f'''    public {name}(String entity, String id) {{
        super(entity + " with id " + id + " not found.");
    }}'''
    elif name == 'InvalidCredentialsException':
        ctor = f'''    public {name}() {{
        super("Invalid credentials.");
    }}'''
    elif name == 'PaymentRejectedException':
        ctor = f'''    public {name}(String orderId) {{
        super("The payment of order " + orderId + " was rejected. The order remains pending payment.");
    }}'''
    else:
        ctor = f'''    public {name}(String message) {{
        super(message);
    }}'''
    write(f'exceptions/{name}.java', f'''package nexusmarket.domain.exceptions;

/** {doc} */
public class {name} extends DomainException {{

{ctor}
}}
''')
print('ok')
