"""Shared helpers to generate Java sources from nexus_spec."""
import os
import re
import sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from nexus_spec import *  # noqa

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = os.path.join(ROOT, "application", "src", "main", "java", "nexusmarket")

MODELS = {"User", "Buyer", "Seller", "LogisticsOperator", "Administrator", "Supervisor", "Warehouse", "Product",
          "PhysicalProduct", "DigitalProduct", "AuthenticationResult", "Inventory", "InventoryMovement", "Cart",
          "CartItem", "Order", "OrderItem", "Invoice", "Shipment", "ReturnRequest", "Refund", "AuditLog",
          "CommercialReport", "SellerPerformanceReport", "InventoryReport", "LogisticsReport",
          "WarehouseStockSummary", "LowStockProduct"}
VOS = {"Address", "Variant", "SystemRole", "UserStatus", "CommercialStatus", "WarehouseType", "ProductType",
       "ProductStatus", "InventoryCondition", "MovementType", "OrderStatus", "ShipmentStatus", "ReturnStatus",
       "RefundStatus", "PaymentResult", "AuditAction", "AuditEntityType", "ReportPeriod"}
JAVA = {"List": "java.util.List", "Optional": "java.util.Optional", "Map": "java.util.Map",
        "BigDecimal": "java.math.BigDecimal", "LocalDateTime": "java.time.LocalDateTime"}


def imports_for(type_strings):
    names = set()
    for t in type_strings:
        names |= set(re.findall(r"[A-Z]\w*", t))
    java = sorted(JAVA[n] for n in names if n in JAVA)
    dom = sorted([f"nexusmarket.domain.models.{n}" for n in names if n in MODELS]
                 + [f"nexusmarket.domain.valueobjects.{n}" for n in names if n in VOS])
    out = [f"import {i};" for i in java]
    if java and dom:
        out.append("")
    out += [f"import {i};" for i in dom]
    return "\n".join(out)


def write(rel, text):
    p = os.path.join(BASE, rel)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        f.write(text)


def params_sig(m):
    return ", ".join(f"{t} {n}" for t, n, _ in m["params"])


def method_types(m):
    return [m["ret"]] + [t for t, _, _ in m["params"]]


def declaration(ret, name, params, indent="    ", suffix=";"):
    """Method declaration wrapped at 110 columns with parameters aligned after the parenthesis."""
    head = f"{indent}{ret} {name}("
    parts = [f"{t} {n}" for t, n in params]
    line = head + ", ".join(parts) + ")" + suffix
    if len(line) <= 110:
        return line
    lines, cur = [], head
    pad = " " * len(head)
    for i, p in enumerate(parts):
        piece = p + ("," if i < len(parts) - 1 else ")" + suffix)
        if cur.strip() and len(cur) + len(piece) + 1 > 110 and cur not in (head,):
            lines.append(cur.rstrip())
            cur = pad
        cur += piece + (" " if i < len(parts) - 1 else "")
    lines.append(cur)
    return "\n".join(lines)
