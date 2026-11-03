"""End-to-end REST test of step 4C against the running application (http://localhost:8080).

Usage: start the application (mvnw spring-boot:run in application/) and run `python tools/e2e_4c.py`.
It creates its own sellers, staff and buyers with a unique suffix, so it can run on a clean database
and be repeated. The automated equivalent is EndToEndPurchaseFlowIT (application/src/test).
"""
import json
import time
import urllib.error
import urllib.request

BASE = "http://localhost:8080/api/v1"
RUN = str(int(time.time()))[-6:]
RESULTS = []


def call(method, path, token=None, body=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req) as resp:
            raw = resp.read().decode()
            return resp.status, (json.loads(raw) if raw else None)
    except urllib.error.HTTPError as e:
        raw = e.read().decode()
        return e.code, (json.loads(raw) if raw else None)


def step(name, expected, method, path, token=None, body=None, show=None):
    status, payload = call(method, path, token, body)
    ok = status == expected
    RESULTS.append(ok)
    extra = ""
    if show and payload is not None and ok:
        extra = " | " + show(payload)
    elif not ok or (isinstance(payload, dict) and isinstance(payload.get("status"), int) and payload["status"] >= 400):
        extra = " | " + (payload.get("message", "") if isinstance(payload, dict) else "")
    print(f"[{'PASS' if ok else 'FAIL'}] {expected}->{status}  {name}{extra}")
    return payload


def login(email, password, expected=200):
    payload = step(f"login {email}", expected, "POST", "/auth/login", body={"email": email, "password": password})
    return payload.get("token") if expected == 200 and payload else None


def address(city):
    return {"line1": "Calle 1 # 2-3", "city": city, "state": "Antioquia", "country": "Colombia"}


def inventory(op_token, product_id, label):
    payload = step(f"inventory of P in W1 {label}", 200, "GET", f"/logistics/products/{product_id}/inventories",
                   op_token, show=lambda p: f"quantity={p[0]['quantity']} reserved={p[0]['reservedQuantity']} "
                                            f"movements={[m['type'] for m in p[0]['movements']]}")
    return payload[0]


print("=== 1. Administrator registers sellers (with first warehouse) and staff")
admin = login("admin@nexusmarket.com", "Admin123*")
s1 = step("register seller S1", 201, "POST", "/admin/sellers", admin, {
    "identificationNumber": "S1-" + RUN, "fullName": "Tech Store " + RUN, "email": f"tech.store.{RUN}@example.com",
    "password": "Seller123*", "warehouseName": "Tech Store Medellin", "warehouseLocation": address("Medellin")},
    show=lambda p: f"seller={p['seller']['id'][:8]} firstWarehouse={p['firstWarehouse']['name']} "
                   f"type={p['firstWarehouse']['type']}")
W1 = s1["firstWarehouse"]["id"]
step("register seller with same email in upper case", 409, "POST", "/admin/sellers", admin, {
    "identificationNumber": "S1b-" + RUN, "fullName": "Copy", "email": f"TECH.STORE.{RUN}@EXAMPLE.COM",
    "password": "Seller123*", "warehouseName": "X", "warehouseLocation": address("Cali")})
step("register seller with same document", 409, "POST", "/admin/sellers", admin, {
    "identificationNumber": "S1-" + RUN, "fullName": "Copy", "email": f"other.{RUN}@example.com",
    "password": "Seller123*", "warehouseName": "X", "warehouseLocation": address("Cali")})
s2 = step("register seller S2", 201, "POST", "/admin/sellers", admin, {
    "identificationNumber": "S2-" + RUN, "fullName": "Home Store " + RUN, "email": f"home.store.{RUN}@example.com",
    "password": "Seller123*", "warehouseName": "Home Store Bogota", "warehouseLocation": address("Bogota")})
step("register logistics operator", 201, "POST", "/admin/staff-users", admin, {
    "role": "LOGISTICS_OPERATOR", "identificationNumber": "OP-" + RUN, "fullName": "Laura Operator",
    "email": f"operator.{RUN}@nexusmarket.com", "password": "Operator123*"})
step("register supervisor", 201, "POST", "/admin/staff-users", admin, {
    "role": "SUPERVISOR", "identificationNumber": "SU-" + RUN, "fullName": "Pedro Supervisor",
    "email": f"supervisor.{RUN}@nexusmarket.com", "password": "Supervisor123*"})

seller1 = login(f"tech.store.{RUN}@example.com", "Seller123*")
seller2 = login(f"home.store.{RUN}@example.com", "Seller123*")
operator = login(f"operator.{RUN}@nexusmarket.com", "Operator123*")
supervisor = login(f"supervisor.{RUN}@nexusmarket.com", "Supervisor123*")
MARIA = f"maria.{RUN}@example.com"
JUAN = f"juan.{RUN}@example.com"
for name, email, doc in (("Maria Gomez", MARIA, "BM-" + RUN), ("Juan Perez", JUAN, "BJ-" + RUN)):
    step(f"register buyer {email}", 201, "POST", "/auth/register/buyers", body={
        "identificationNumber": doc, "fullName": name, "email": email, "password": "Buyer123*",
        "primaryAddress": address("Medellin")})
maria = login(MARIA, "Buyer123*")
juan = login(JUAN, "Buyer123*")

print("\n=== 2. Full purchase flow")
p = step("S1 registers physical product P", 201, "POST", "/seller/products", seller1, {
    "type": "PHYSICAL", "name": "Wireless Mouse " + RUN, "description": "Ergonomic wireless mouse",
    "price": 150000.00, "variants": []}, show=lambda x: f"status={x['status']}")
P = p["id"]
step("S1 publishes P", 200, "PATCH", f"/seller/products/{P}/publish", seller1, show=lambda x: f"status={x['status']}")
step("operator registers initial stock 10 of P in W1", 201, "POST", "/logistics/inventories", operator,
     {"productId": P, "warehouseId": W1, "quantity": 10},
     show=lambda x: f"quantity={x['quantity']} reserved={x['reservedQuantity']}")
step("maria clears her cart", 200, "DELETE", "/buyer/cart/items", maria)
step("maria adds 2 x P", 200, "POST", "/buyer/cart/items", maria, {"productId": P, "quantity": 2},
     show=lambda x: f"total={x['totalAmount']}")
order = step("maria confirms the order", 201, "POST", "/buyer/orders", maria,
             show=lambda x: f"status={x['status']} total={x['totalAmount']}")
O = order["id"]
after_confirm = inventory(operator, P, "after confirming the order")
step("maria pays", 200, "POST", f"/buyer/orders/{O}/payment", maria, show=lambda x: f"status={x['status']}")
step("maria consults the invoice", 200, "GET", f"/buyer/orders/{O}/invoice", maria,
     show=lambda x: f"total={x['totalAmount']}")
step("operator consults the order", 200, "GET", f"/logistics/orders/{O}", operator, show=lambda x: f"status={x['status']}")
shipments = step("operator creates shipments", 201, "POST", f"/logistics/orders/{O}/shipments", operator,
                 show=lambda x: f"shipments={len(x)} status={x[0]['status']}")
SH = shipments[0]["id"]
step("operator packs", 200, "PATCH", f"/logistics/shipments/{SH}/pack", operator, show=lambda x: f"status={x['status']}")
step("operator dispatches", 200, "PATCH", f"/logistics/shipments/{SH}/dispatch", operator,
     show=lambda x: f"status={x['status']}")
after_dispatch = inventory(operator, P, "after dispatching")
step("operator confirms delivery", 200, "PATCH", f"/logistics/shipments/{SH}/deliver", operator,
     show=lambda x: f"status={x['status']}")
step("maria sees the order delivered", 200, "GET", f"/buyer/orders/{O}", maria, show=lambda x: f"status={x['status']}")
ret = step("maria requests a return", 201, "POST", f"/buyer/orders/{O}/returns", maria, {"reason": "Wrong color"},
           show=lambda x: f"status={x['status']}")
R = ret["id"]
step("admin approves the return", 200, "PATCH", f"/admin/returns/{R}/approve", admin, show=lambda x: f"status={x['status']}")
after_return = inventory(operator, P, "after approving the return")
refund = step("admin consults the refund", 200, "GET", f"/admin/returns/{R}/refund", admin,
              show=lambda x: f"status={x['status']} amount={x['amount']}")
step("admin processes the refund", 200, "PATCH", f"/admin/refunds/{refund['id']}/process", admin,
     show=lambda x: f"status={x['status']} processedAt={x['processedAt']}")
today = time.strftime("%Y-%m-%d")
step("S1 performance report", 200, "GET", f"/seller/reports/performance?startDate={today}&endDate={today}", seller1,
     show=lambda x: f"orders={x['ordersWithSellerProducts']} units={x['unitsSold']} amount={x['amountSold']} "
                    f"approvedReturns={x['approvedReturns']} returnRate={x['returnRate']}")
step("S1 performance report with inverted dates", 400, "GET",
     "/seller/reports/performance?startDate=2026-12-31&endDate=2026-01-01", seller1)

print("\n=== 3. Supervisor reports and audit")
step("supervisor commercial report", 200, "GET", f"/supervisor/reports/commercial?startDate={today}&endDate={today}",
     supervisor, show=lambda x: f"orders={x['totalOrders']} byStatus={x['ordersByStatus']} invoiced={x['totalInvoiced']} "
                                f"refunded={x['totalRefunded']} net={x['netRevenue']}")
step("supervisor inventory report (threshold 5)", 200, "GET", "/supervisor/reports/inventory?lowStockThreshold=5",
     supervisor, show=lambda x: f"warehouses={len(x['warehouses'])} lowStock={len(x['lowStockProducts'])}")
step("supervisor logistics report", 200, "GET", f"/supervisor/reports/logistics?startDate={today}&endDate={today}",
     supervisor, show=lambda x: f"shipments={x['totalShipments']} delivered={x['delivered']}")
step("supervisor audit of the order", 200, "GET", f"/supervisor/audit-logs/entities/ORDER/{O}", supervisor,
     show=lambda x: f"actions={[a['action'] for a in x]}")
step("inventory report lowStockThreshold=0", 400, "GET", "/supervisor/reports/inventory?lowStockThreshold=0", supervisor)
step("inventory report lowStockThreshold=-1", 400, "GET", "/supervisor/reports/inventory?lowStockThreshold=-1", supervisor)
step("audit with unknown entity type", 400, "GET", f"/supervisor/audit-logs/entities/FOO/{O}", supervisor)
step("supervisor tries to register a seller", 403, "POST", "/admin/sellers", supervisor, {
    "identificationNumber": "X-" + RUN, "fullName": "X", "email": f"x.{RUN}@example.com", "password": "Seller123*",
    "warehouseName": "X", "warehouseLocation": address("Cali")})
step("supervisor tries to publish a product", 403, "PATCH", f"/seller/products/{P}/publish", supervisor)

print("\n=== 4. Ownership between buyers (juan on maria's data)")
step("juan consults maria's order", 403, "GET", f"/buyer/orders/{O}", juan)
step("juan consults maria's invoice", 403, "GET", f"/buyer/orders/{O}/invoice", juan)
step("juan consults maria's return", 403, "GET", f"/buyer/returns/{R}", juan)

print("\n=== 5. Ownership between sellers (S2 on S1's data)")
step("S2 consults an order without its products", 403, "GET", f"/seller/orders/{O}", seller2)
step("S2 updates S1's product", 403, "PUT", f"/seller/products/{P}", seller2,
     {"name": "Hacked", "description": "Hacked", "price": 1.00, "variants": []})
step("S2 suspends S1's product", 403, "PATCH", f"/seller/products/{P}/suspend", seller2)
p2 = step("S2 registers its own product P2", 201, "POST", "/seller/products", seller2, {
    "type": "PHYSICAL", "name": "Lamp " + RUN, "description": "Desk lamp", "price": 80000.00, "variants": []})
step("S2 registers stock of P2 in S1's warehouse W1", 403, "POST", "/seller/inventories", seller2,
     {"productId": p2["id"], "warehouseId": W1, "quantity": 5})
step("S2 registers an inbound of P in W1", 403, "POST", f"/seller/inventories/{P}/{W1}/inbounds", seller2,
     {"quantity": 5})

print("\n=== 6. Digital product")
d = step("S1 registers digital product D", 201, "POST", "/seller/products", seller1, {
    "type": "DIGITAL", "name": "E-book " + RUN, "description": "Java guide", "price": 50000.00, "variants": []})
step("S1 publishes D", 200, "PATCH", f"/seller/products/{d['id']}/publish", seller1)
step("maria adds D", 200, "POST", "/buyer/cart/items", maria, {"productId": d["id"], "quantity": 1})
digital = step("maria confirms the digital order", 201, "POST", "/buyer/orders", maria,
               show=lambda x: f"status={x['status']}")
step("maria pays the digital order", 200, "POST", f"/buyer/orders/{digital['id']}/payment", maria,
     show=lambda x: f"status={x['status']} shipments={len(x['shipments'])}")

print("\n=== 7. Blocking and reactivating juan")
juan_id = call("GET", "/buyer/account", juan)[1]["id"]
step("admin blocks juan", 200, "PATCH", f"/admin/users/{juan_id}/status", admin, {"status": "BLOCKED"},
     show=lambda x: f"status={x['status']}")
step("juan's previous token", 401, "GET", "/buyer/profile", juan)
login(JUAN, "Buyer123*", expected=403)
step("admin reactivates juan", 200, "PATCH", f"/admin/users/{juan_id}/status", admin, {"status": "ACTIVE"},
     show=lambda x: f"status={x['status']}")
juan2 = login(JUAN, "Buyer123*")
step("juan with the new token", 200, "GET", "/buyer/profile", juan2, show=lambda x: f"status={x['status']}")

print("\n=== 8. Administrator cannot change its own status")
admin_id = call("POST", "/auth/login", body={"email": "admin@nexusmarket.com", "password": "Admin123*"})[1]["user"]["id"]
step("admin blocks itself", 403, "PATCH", f"/admin/users/{admin_id}/status", admin, {"status": "BLOCKED"})

print("\n=== Inventory of P in W1 by stage")
for label, inv in (("after confirm", after_confirm), ("after dispatch", after_dispatch), ("after return", after_return)):
    print(f"  {label:15s} quantity={inv['quantity']:2d} reserved={inv['reservedQuantity']} "
          f"movements={[m['type'] + ':' + str(m['quantity']) for m in inv['movements']]}")
print(f"\nRESULT: {sum(RESULTS)}/{len(RESULTS)} steps passed")
raise SystemExit(0 if all(RESULTS) else 1)
