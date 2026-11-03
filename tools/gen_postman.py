"""Generates postman/NexusMarket.postman_collection.json and its environment.

The role folders come from the endpoint table of SDD/Adapters/Api-rest-endpoints.md, so the 108 endpoints
stay in step with the specification. The "Flujo completo" folder is written by hand below.
"""
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "postman")

ADDRESS = {"line1": "Calle 10 # 20-30", "city": "Medellin", "state": "Antioquia", "country": "Colombia"}
SELLER_PASSWORD, BUYER_PASSWORD, STAFF_PASSWORD = "Seller123*", "Buyer123*", "Staff123*"

COLLECTION_PRE_REQUEST = [
    "// Fecha de hoy en Colombia (UTC-5, sin horario de verano), la misma zona de la aplicacion.",
    "const bogota = new Date(Date.now() - 5 * 60 * 60 * 1000);",
    "pm.environment.set('today', bogota.toISOString().slice(0, 10));",
]


def script(kind, lines):
    return {"listen": kind, "script": {"type": "text/javascript", "exec": lines}}


def bearer(variable):
    return {"type": "bearer", "bearer": [{"key": "token", "value": "{{%s}}" % variable, "type": "string"}]}


def url(path, query=None):
    raw = "{{baseUrl}}" + path
    result = {"raw": raw, "host": ["{{baseUrl}}"], "path": path.strip("/").split("/")}
    if query:
        result["raw"] += "?" + "&".join(f"{item['key']}={item['value']}" for item in query if not item.get("disabled"))
        result["query"] = query
    return result


def request(name, method, path, token, body=None, query=None, events=None, description=None):
    req = {"method": method, "header": [], "url": url(path, query)}
    if token == "none":
        req["auth"] = {"type": "noauth"}
    elif token:
        req["auth"] = bearer(token)
    if body is not None:
        req["header"].append({"key": "Content-Type", "value": "application/json"})
        req["body"] = {"mode": "raw", "raw": json.dumps(body, indent=2, ensure_ascii=False)}
    if description:
        req["description"] = description
    item = {"name": name, "request": req}
    if events:
        item["event"] = events
    return item


# ---------------------------------------------------------------------------- role folders

PATH_VARIABLES = {
    "productId": "{{physicalProductId}}", "warehouseId": "{{warehouseId}}", "orderId": "{{orderId}}",
    "shipmentId": "{{shipmentId}}", "returnRequestId": "{{returnRequestId}}", "refundId": "{{refundId}}",
    "sellerId": "{{sellerId}}", "buyerId": "{{buyerId}}", "userId": "{{operatorId}}", "entityType": "ORDER",
    "entityId": "{{orderId}}",
}
QUERY_VALUES = {"startDate": "{{today}}", "endDate": "{{today}}", "lowStockThreshold": "5"}

FOLDERS = [
    ("Autenticacion", "/api/v1/auth", None),
    ("Publico", "/api/v1/public", None),
    ("Comprador", "/api/v1/buyer", "buyerToken"),
    ("Vendedor", "/api/v1/seller", "sellerToken"),
    ("Operador logistico", "/api/v1/logistics", "operatorToken"),
    ("Administrador", "/api/v1/admin", "adminToken"),
    ("Supervisor", "/api/v1/supervisor", "supervisorToken"),
]

PASSWORD_OF_FOLDER = {
    "buyerToken": BUYER_PASSWORD, "sellerToken": SELLER_PASSWORD, "operatorToken": STAFF_PASSWORD,
    "adminToken": "{{adminPassword}}", "supervisorToken": STAFF_PASSWORD,
}
FOLDER_LOGIN = {
    "buyerToken": ("buyer{{runId}}@example.com", BUYER_PASSWORD),
    "sellerToken": ("seller{{runId}}@example.com", SELLER_PASSWORD),
    "operatorToken": ("operator{{runId}}@example.com", STAFF_PASSWORD),
    "adminToken": ("{{adminEmail}}", "{{adminPassword}}"),
    "supervisorToken": ("supervisor{{runId}}@example.com", STAFF_PASSWORD),
}

ROLE_FOLDER_TESTS = [
    "// Exploracion: la respuesta depende del estado, asi que aqui no se exige el codigo de exito.",
    "pm.test('Sin error del servidor', () => pm.expect(pm.response.code).to.be.below(500));",
    "pm.test('Token y rol aceptados', () => pm.expect(pm.response.code).to.not.be.oneOf([401, 403]));",
    "pm.test('Cuerpo y parametros validos', () => {",
    "    let message = '';",
    "    try { message = pm.response.json().message || ''; } catch (e) { /* respuesta sin cuerpo */ }",
    "    pm.expect(message).to.not.equal('Request validation failed.');",
    "});",
]


def body_for(dto, port_method, token):
    product = {"name": "Producto de prueba", "description": "Creado desde Postman", "price": 75.50, "variants": []}
    password = PASSWORD_OF_FOLDER.get(token)
    unique = "{{$timestamp}}"
    bodies = {
        "LoginRequestDTO": {"email": "{{adminEmail}}", "password": "{{adminPassword}}"},
        "RegisterBuyerRequestDTO": {"identificationNumber": f"B{unique}", "fullName": "Comprador de prueba",
                                    "email": f"buyer{unique}@example.com", "password": BUYER_PASSWORD,
                                    "primaryAddress": ADDRESS},
        # The new password equals the current one, so exploring never locks the account.
        "ChangePasswordRequestDTO": {"currentPassword": password, "newPassword": password},
        "UpdateBuyerProfileRequestDTO": {"fullName": "Maria Gomez", "primaryAddress": ADDRESS,
                                         "additionalAddresses": []},
        "AddCartItemRequestDTO": {"productId": "{{physicalProductId}}", "quantity": 1},
        "UpdateCartItemQuantityRequestDTO": {"quantity": 2},
        "RequestReturnRequestDTO": {"reason": "Color equivocado"},
        "UpdateSellerRequestDTO": {"fullName": "Tech Store"},
        "WarehouseRequestDTO": {"name": "Bodega de prueba", "location": ADDRESS},
        "RegisterProductRequestDTO": {"type": "PHYSICAL", **product},
        "UpdateProductRequestDTO": product,
        "InitialStockRequestDTO": {"productId": "{{physicalProductId}}", "warehouseId": "{{warehouseId}}",
                                   "quantity": 5},
        "StockQuantityRequestDTO": {"quantity": 1},
        "InventoryAdjustmentRequestDTO": {"quantity": -1, "condition": "AVAILABLE"},
        "RegisterStaffUserRequestDTO": {"role": "LOGISTICS_OPERATOR", "identificationNumber": f"O{unique}",
                                        "fullName": "Operador de prueba", "email": f"staff{unique}@example.com",
                                        "password": STAFF_PASSWORD},
        # Same status the user already has, so exploring never blocks anyone.
        "ChangeUserStatusRequestDTO": {"status": "ACTIVE"},
        "RegisterSellerRequestDTO": {"identificationNumber": f"S{unique}", "fullName": "Vendedor de prueba",
                                     "email": f"seller{unique}@example.com", "password": SELLER_PASSWORD,
                                     "warehouseName": "Bodega inicial", "warehouseLocation": ADDRESS},
        "ChangeCommercialStatusRequestDTO": {"commercialStatus": "ENABLED"},
    }
    return bodies[dto]


def parse_endpoints():
    rows = []
    pattern = re.compile(r"\| (\d+) \| `(\w+)` \| `([^`]+)` \| [^|]+ \| `\w+\.(\w+)` \| (.*?) \| (\d+)")
    with open(os.path.join(ROOT, "SDD", "Adapters", "Api-rest-endpoints.md"), encoding="utf-8") as spec:
        for line in spec:
            match = pattern.match(line)
            if match:
                rows.append({"method": match[2], "path": match[3], "port": match[4], "request": match[5],
                             "status": match[6]})
    assert len(rows) == 108, len(rows)
    assert len({(row["method"], row["path"]) for row in rows}) == 108
    return rows


def sort_key(row):
    """Keeps the table order, except that a password change invalidates the token, so it goes last,
    and a seller loads stock before discontinuing the product it has just registered."""
    if row["port"] == "changeUserPassword":
        return 2
    if row["path"].endswith(("/publish", "/suspend", "/discontinue")):
        return 1
    return 0


def role_request(row, token):
    path = re.sub(r"\{(\w+)\}", lambda m: PATH_VARIABLES[m[1]], row["path"])
    # An administrator only updates Marketplace warehouses, not the warehouse of the flow's seller.
    if token == "adminToken" and re.search(r"/warehouses/\{warehouseId\}$", row["path"]):
        path = path.replace("{{warehouseId}}", "{{marketplaceWarehouseId}}")
    dto = re.search(r"`(\w+DTO)`", row["request"])
    query_names = re.findall(r"`(\w+)`", row["request"]) if row["request"].startswith("query") else []
    query = [{"key": name, "value": QUERY_VALUES.get(name, ""), "disabled": name not in QUERY_VALUES}
             for name in query_names]
    item_token = "none" if token is None else None
    if row["port"] == "logout":
        item_token = "adminToken"  # the login row of this folder renews it; the admin folder logs in again
    body = body_for(dto[1], row["port"], token) if dto else None
    tests = list(ROLE_FOLDER_TESTS)
    if row["port"] == "registerProduct":
        tests.append("if (pm.response.code === 201) pm.environment.set('physicalProductId', pm.response.json().id);")
    if row["port"] == "login":
        tests.append("if (pm.response.code === 200) pm.environment.set('adminToken', pm.response.json().token);")
    if row["port"] == "registerWarehouse" and token == "adminToken":
        tests.append("if (pm.response.code === 201) pm.environment.set('marketplaceWarehouseId', pm.response.json().id);")
    name = f"{row['port']} ({row['status']})"
    return request(name, row["method"], path, item_token, body=body, query=query or None,
                   events=[script("test", tests)],
                   description=f"Respuesta exitosa documentada: {row['status']}. Ver SDD/Adapters/Api-rest-endpoints.md.")


def folder_login(token):
    email, password = FOLDER_LOGIN[token]
    tests = ["pm.test('Login 200', () => pm.response.to.have.status(200));",
             f"if (pm.response.code === 200) pm.environment.set('{token}', pm.response.json().token);"]
    return request("Iniciar sesion", "POST", "/api/v1/auth/login", "none", body={"email": email, "password": password},
                   events=[script("test", tests)],
                   description="No es un endpoint adicional: renueva el token de esta carpeta.")


def role_folders(rows):
    folders = []
    for name, prefix, token in FOLDERS:
        selected = sorted((row for row in rows if row["path"].startswith(prefix + "/")), key=sort_key)
        items = [role_request(row, token) for row in selected]
        if token:
            items.insert(0, folder_login(token))
        folder = {"name": name, "item": items,
                  "description": "Para explorar a mano, despues de ejecutar 'Flujo completo', que llena las variables. "
                                 "Las respuestas 400, 404 o 409 pueden ser correctas segun el estado de los datos."}
        if token:
            folder["auth"] = bearer(token)
        else:
            folder["auth"] = {"type": "noauth"}
        folders.append(folder)
    return folders


# ---------------------------------------------------------------------------- complete flow

def step(name, method, path, token, status, body=None, query=None, checks=(), saves=None, pre=None):
    tests = [f"pm.test('Responde {status}', () => pm.response.to.have.status({status}));"]
    if checks or saves:
        tests.append("let json = {};")
        tests.append("try { json = pm.response.json(); } catch (e) { /* sin cuerpo */ }")
    for variable, expression in (saves or {}).items():
        tests.append(f"pm.environment.set('{variable}', {expression});")
    for label, condition in checks:
        tests.append(f"pm.test({json.dumps(label, ensure_ascii=False)}, () => pm.expect({condition}).to.be.true);")
    events = [script("test", tests)]
    if pre:
        events.insert(0, script("prerequest", pre))
    return request(name, method, path, token, body=body, query=query, events=events)


def query_of(**values):
    return [{"key": key, "value": value} for key, value in values.items()]


def stock_check(name, quantity, reserved):
    return step(name, "GET", "/api/v1/logistics/products/{{physicalProductId}}/inventories", "operatorToken", 200,
                checks=[(f"Disponible {quantity} y reservado {reserved}",
                         f"json[0].quantity === {quantity} && json[0].reservedQuantity === {reserved}")])


def complete_flow():
    seller_body = {"identificationNumber": "S{{runId}}", "fullName": "Tech Store", "email": "seller{{runId}}@example.com",
                   "password": SELLER_PASSWORD, "warehouseName": "Bodega Principal", "warehouseLocation": ADDRESS}
    today = query_of(startDate="{{today}}", endDate="{{today}}")
    steps = [
        step("Admin: iniciar sesion", "POST", "/api/v1/auth/login", "none", 200,
             body={"email": "{{adminEmail}}", "password": "{{adminPassword}}"},
             pre=["// Sufijo unico por ejecucion: el flujo se puede repetir sin duplicados.",
                  "pm.environment.set('runId', String(Date.now()));"],
             saves={"adminToken": "json.token"},
             checks=[("El rol es ADMINISTRATOR", "json.user.role === 'ADMINISTRATOR'")]),
        step("Admin: registrar vendedor con su primera bodega", "POST", "/api/v1/admin/sellers", "adminToken", 201,
             body=seller_body, saves={"sellerId": "json.seller.id", "warehouseId": "json.firstWarehouse.id"},
             checks=[("Trae la primera bodega", "json.firstWarehouse.type === 'SELLER'")]),
        step("Negativo: vendedor duplicado responde 409", "POST", "/api/v1/admin/sellers", "adminToken", 409,
             body=seller_body),
        step("Admin: registrar operador logistico", "POST", "/api/v1/admin/staff-users", "adminToken", 201,
             body={"role": "LOGISTICS_OPERATOR", "identificationNumber": "O{{runId}}", "fullName": "Laura Operadora",
                   "email": "operator{{runId}}@example.com", "password": STAFF_PASSWORD},
             saves={"operatorId": "json.id"}),
        step("Admin: registrar supervisor", "POST", "/api/v1/admin/staff-users", "adminToken", 201,
             body={"role": "SUPERVISOR", "identificationNumber": "U{{runId}}", "fullName": "Pedro Supervisor",
                   "email": "supervisor{{runId}}@example.com", "password": STAFF_PASSWORD},
             saves={"supervisorId": "json.id"}),
        step("Vendedor: iniciar sesion", "POST", "/api/v1/auth/login", "none", 200,
             body={"email": "seller{{runId}}@example.com", "password": SELLER_PASSWORD},
             saves={"sellerToken": "json.token"}),
        step("Vendedor: registrar producto fisico", "POST", "/api/v1/seller/products", "sellerToken", 201,
             body={"type": "PHYSICAL", "name": "Mouse inalambrico", "description": "Mouse ergonomico", "price": 100.00,
                   "variants": []},
             saves={"physicalProductId": "json.id"},
             checks=[("Queda en DRAFT", "json.status === 'DRAFT'")]),
        step("Vendedor: publicar producto fisico", "PATCH", "/api/v1/seller/products/{{physicalProductId}}/publish",
             "sellerToken", 200, checks=[("Queda en PUBLISHED", "json.status === 'PUBLISHED'")]),
        step("Vendedor: registrar producto digital", "POST", "/api/v1/seller/products", "sellerToken", 201,
             body={"type": "DIGITAL", "name": "Guia de Java", "description": "Libro electronico", "price": 50.00,
                   "variants": []},
             saves={"digitalProductId": "json.id"}),
        step("Vendedor: publicar producto digital", "PATCH", "/api/v1/seller/products/{{digitalProductId}}/publish",
             "sellerToken", 200, checks=[("Queda en PUBLISHED", "json.status === 'PUBLISHED'")]),
        step("Operador: iniciar sesion", "POST", "/api/v1/auth/login", "none", 200,
             body={"email": "operator{{runId}}@example.com", "password": STAFF_PASSWORD},
             saves={"operatorToken": "json.token"}),
        step("Operador: registrar stock inicial", "POST", "/api/v1/logistics/inventories", "operatorToken", 201,
             body={"productId": "{{physicalProductId}}", "warehouseId": "{{warehouseId}}", "quantity": 10},
             checks=[("Quedan 10 disponibles", "json.quantity === 10")]),
        step("Comprador: registrarse", "POST", "/api/v1/auth/register/buyers", "none", 201,
             body={"identificationNumber": "B{{runId}}", "fullName": "Maria Gomez", "email": "buyer{{runId}}@example.com",
                   "password": BUYER_PASSWORD, "primaryAddress": ADDRESS},
             saves={"buyerId": "json.id"}),
        step("Comprador: iniciar sesion", "POST", "/api/v1/auth/login", "none", 200,
             body={"email": "buyer{{runId}}@example.com", "password": BUYER_PASSWORD},
             saves={"buyerToken": "json.token"}),
        step("Negativo: otro rol responde 403", "GET", "/api/v1/admin/warehouses", "buyerToken", 403),
        step("Negativo: sin token responde 401", "GET", "/api/v1/buyer/cart", "none", 401),
        step("Comprador: agregar producto fisico al carrito", "POST", "/api/v1/buyer/cart/items", "buyerToken", 200,
             body={"productId": "{{physicalProductId}}", "quantity": 2},
             checks=[("El carrito tiene 1 linea", "json.items.length === 1")]),
        step("Comprador: agregar producto digital al carrito", "POST", "/api/v1/buyer/cart/items", "buyerToken", 200,
             body={"productId": "{{digitalProductId}}", "quantity": 1},
             checks=[("El carrito tiene 2 lineas", "json.items.length === 2")]),
        step("Comprador: confirmar pedido", "POST", "/api/v1/buyer/orders", "buyerToken", 201,
             saves={"orderId": "json.id"},
             checks=[("Queda en PENDING_PAYMENT", "json.status === 'PENDING_PAYMENT'"),
                     ("El total es 250", "json.totalAmount === 250"),
                     ("La fecha del pedido es hoy en Colombia",
                      "json.createdAt.startsWith(pm.environment.get('today'))")]),
        stock_check("Operador: consultar stock tras la reserva", 8, 2),
        step("Comprador: pagar pedido", "POST", "/api/v1/buyer/orders/{{orderId}}/payment", "buyerToken", 200,
             checks=[("Queda en PAID", "json.status === 'PAID'")]),
        step("Comprador: consultar factura", "GET", "/api/v1/buyer/orders/{{orderId}}/invoice", "buyerToken", 200,
             checks=[("Corresponde al pedido", "json.orderId === pm.environment.get('orderId')"),
                     ("El total es 250", "json.totalAmount === 250")]),
        step("Operador: crear envios", "POST", "/api/v1/logistics/orders/{{orderId}}/shipments", "operatorToken", 201,
             saves={"shipmentId": "json[0].id"},
             checks=[("Se crea 1 envio", "json.length === 1")]),
        step("Operador: empacar envio", "PATCH", "/api/v1/logistics/shipments/{{shipmentId}}/pack", "operatorToken", 200,
             checks=[("Queda en PACKED", "json.status === 'PACKED'")]),
        step("Operador: despachar envio", "PATCH", "/api/v1/logistics/shipments/{{shipmentId}}/dispatch", "operatorToken",
             200, checks=[("Queda en IN_TRANSIT", "json.status === 'IN_TRANSIT'")]),
        stock_check("Operador: consultar stock tras el despacho", 8, 0),
        step("Operador: entregar envio", "PATCH", "/api/v1/logistics/shipments/{{shipmentId}}/deliver", "operatorToken",
             200, checks=[("Queda en DELIVERED", "json.status === 'DELIVERED'")]),
        step("Comprador: consultar pedido entregado", "GET", "/api/v1/buyer/orders/{{orderId}}", "buyerToken", 200,
             checks=[("Queda en DELIVERED", "json.status === 'DELIVERED'")]),
        step("Comprador: solicitar devolucion", "POST", "/api/v1/buyer/orders/{{orderId}}/returns", "buyerToken", 201,
             body={"reason": "Color equivocado"}, saves={"returnRequestId": "json.id"},
             checks=[("Queda en REQUESTED", "json.status === 'REQUESTED'")]),
        step("Admin: aprobar devolucion", "PATCH", "/api/v1/admin/returns/{{returnRequestId}}/approve", "adminToken", 200,
             checks=[("Queda en APPROVED", "json.status === 'APPROVED'")]),
        stock_check("Operador: consultar stock tras la devolucion", 10, 0),
        step("Admin: consultar reembolso", "GET", "/api/v1/admin/returns/{{returnRequestId}}/refund", "adminToken", 200,
             saves={"refundId": "json.id"}, checks=[("Queda en PENDING", "json.status === 'PENDING'")]),
        step("Admin: procesar reembolso", "PATCH", "/api/v1/admin/refunds/{{refundId}}/process", "adminToken", 200,
             checks=[("Queda procesado", "json.status === 'PROCESSED'")]),
        step("Supervisor: iniciar sesion", "POST", "/api/v1/auth/login", "none", 200,
             body={"email": "supervisor{{runId}}@example.com", "password": STAFF_PASSWORD},
             saves={"supervisorToken": "json.token"}),
        step("Supervisor: reporte comercial de hoy", "GET", "/api/v1/supervisor/reports/commercial", "supervisorToken", 200,
             query=today, checks=[("Incluye el pedido de hoy", "json.totalOrders >= 1")]),
        step("Supervisor: reporte de desempeno del vendedor de hoy", "GET",
             "/api/v1/supervisor/reports/sellers/{{sellerId}}/performance", "supervisorToken", 200, query=today,
             checks=[("Corresponde al vendedor", "json.sellerId === pm.environment.get('sellerId')"),
                     ("Incluye las 3 unidades vendidas", "json.unitsSold === 3")]),
        step("Supervisor: reporte de inventario", "GET", "/api/v1/supervisor/reports/inventory", "supervisorToken", 200,
             query=query_of(lowStockThreshold="5"), checks=[("Usa el umbral 5", "json.lowStockThreshold === 5")]),
        step("Supervisor: reporte logistico de hoy", "GET", "/api/v1/supervisor/reports/logistics", "supervisorToken", 200,
             query=today, checks=[("Incluye el envio de hoy", "json.totalShipments >= 1")]),
        step("Negativo: el supervisor no aprueba devoluciones", "PATCH",
             "/api/v1/admin/returns/{{returnRequestId}}/approve", "supervisorToken", 403),
        step("Comprador: cerrar sesion", "POST", "/api/v1/auth/logout", "buyerToken", 204),
        step("Negativo: el token tras el logout responde 401", "GET", "/api/v1/buyer/profile", "buyerToken", 401),
    ]
    numbered = [{**item, "name": f"{index:02d} - {item['name']}"} for index, item in enumerate(steps, start=1)]
    return {"name": "Flujo completo", "item": numbered,
            "description": "Ejecutar en orden con el Collection Runner. Cada peticion verifica su codigo y guarda lo que "
                           "necesita la siguiente. Usa datos nuevos en cada ejecucion."}


def main():
    rows = parse_endpoints()
    collection = {
        "info": {"name": "NexusMarket", "description": "API REST de NexusMarket: flujo completo ejecutable y los 108 "
                                                       "endpoints por rol. Ver SETUP.md.",
                 "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"},
        "event": [script("prerequest", COLLECTION_PRE_REQUEST)],
        "item": [complete_flow(), *role_folders(rows)],
    }
    keys = ["adminToken", "sellerToken", "operatorToken", "supervisorToken", "buyerToken", "runId", "today", "sellerId",
            "warehouseId", "marketplaceWarehouseId", "operatorId", "supervisorId", "buyerId", "physicalProductId", "digitalProductId", "orderId",
            "shipmentId", "returnRequestId", "refundId"]
    defaults = [("baseUrl", "http://localhost:8080"), ("adminEmail", "admin@nexusmarket.com"), ("adminPassword", "Admin123*")]
    environment = {"name": "NexusMarket", "_postman_variable_scope": "environment",
                   "values": [{"key": key, "value": value, "type": "default", "enabled": True}
                              for key, value in defaults + [(key, "") for key in keys]]}
    os.makedirs(OUT, exist_ok=True)
    for name, content in (("NexusMarket.postman_collection.json", collection),
                          ("NexusMarket.postman_environment.json", environment)):
        with open(os.path.join(OUT, name), "w", encoding="utf-8", newline="\n") as target:
            json.dump(content, target, indent=2, ensure_ascii=False)
            target.write("\n")
    role_total = sum(len([i for i in folder["item"] if i["name"] != "Iniciar sesion"]) for folder in role_folders(rows))
    print("flow steps:", len(complete_flow()["item"]), "| role endpoints:", role_total)


if __name__ == "__main__":
    main()
