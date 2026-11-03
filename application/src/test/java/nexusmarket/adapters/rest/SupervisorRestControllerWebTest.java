package nexusmarket.adapters.rest;

import static nexusmarket.adapters.rest.RestResponseFixtures.AUDIT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.AUDIT_LOG;
import static nexusmarket.adapters.rest.RestResponseFixtures.BUYER;
import static nexusmarket.adapters.rest.RestResponseFixtures.BUYER_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.CART;
import static nexusmarket.adapters.rest.RestResponseFixtures.CART_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.COMMERCIAL_REPORT;
import static nexusmarket.adapters.rest.RestResponseFixtures.COMMERCIAL_REPORT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.END_DATE;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY_REPORT;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY_REPORT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVOICE;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVOICE_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.LOGISTICS_REPORT;
import static nexusmarket.adapters.rest.RestResponseFixtures.LOGISTICS_REPORT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.ORDER;
import static nexusmarket.adapters.rest.RestResponseFixtures.ORDER_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.PASSWORD_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.PERFORMANCE_REPORT;
import static nexusmarket.adapters.rest.RestResponseFixtures.PERFORMANCE_REPORT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.PERIOD;
import static nexusmarket.adapters.rest.RestResponseFixtures.PRODUCT;
import static nexusmarket.adapters.rest.RestResponseFixtures.PRODUCT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.REFUND;
import static nexusmarket.adapters.rest.RestResponseFixtures.REFUND_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.RETURN_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.RETURN_REQUEST;
import static nexusmarket.adapters.rest.RestResponseFixtures.SELLER;
import static nexusmarket.adapters.rest.RestResponseFixtures.SELLER_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.START_DATE;
import static nexusmarket.adapters.rest.RestResponseFixtures.THRESHOLD;
import static nexusmarket.adapters.rest.RestResponseFixtures.WAREHOUSE;
import static nexusmarket.adapters.rest.RestResponseFixtures.WAREHOUSE_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.userJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.Supervisor;
import nexusmarket.domain.ports.in.SupervisorPort;
import nexusmarket.domain.valueobjects.AuditEntityType;

class SupervisorRestControllerWebTest extends EndpointWebTestBase<SupervisorPort> {

    private static final Supervisor USER = DomainFixtures.supervisor();
    private static final Administrator OTHER_USER = DomainFixtures.administrator();

    private static final EndpointCase.Routes<SupervisorPort> AS_SUPERVISOR = new EndpointCase.Routes<>(USER);

    @Override
    protected SupervisorPort port() {
        return supervisorPort;
    }

    static Stream<EndpointCase<SupervisorPort>> endpoints() {
        return Stream.of(
                AS_SUPERVISOR.put("/api/v1/supervisor/account/password")
                        .body(PASSWORD_BODY)
                        .calls(port -> verify(port).changeUserPassword(same(USER), eq("Current123*"), eq("Newpass123*")))
                        .status(204),
                AS_SUPERVISOR.get("/api/v1/supervisor/users/{userId}", "u-1")
                        .stub(port -> when(port.consultUser(any(), any())).thenReturn(OTHER_USER))
                        .calls(port -> verify(port).consultUser(same(USER), eq("u-1")))
                        .json(userJson(OTHER_USER)),
                AS_SUPERVISOR.get("/api/v1/supervisor/sellers/{sellerId}", "s-1")
                        .stub(port -> when(port.consultSeller(any(), any())).thenReturn(SELLER))
                        .calls(port -> verify(port).consultSeller(same(USER), eq("s-1")))
                        .json(SELLER_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/buyers/{buyerId}", "b-1")
                        .stub(port -> when(port.consultBuyer(any(), any())).thenReturn(BUYER))
                        .calls(port -> verify(port).consultBuyer(same(USER), eq("b-1")))
                        .json(BUYER_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/warehouses")
                        .stub(port -> when(port.consultWarehouses(any())).thenReturn(List.of(WAREHOUSE)))
                        .calls(port -> verify(port).consultWarehouses(same(USER)))
                        .jsonList(WAREHOUSE_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/warehouses/{warehouseId}", "w-1")
                        .stub(port -> when(port.consultWarehouse(any(), any())).thenReturn(WAREHOUSE))
                        .calls(port -> verify(port).consultWarehouse(same(USER), eq("w-1")))
                        .json(WAREHOUSE_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/sellers/{sellerId}/products", "s-1")
                        .stub(port -> when(port.consultSellerProducts(any(), any())).thenReturn(List.of(PRODUCT)))
                        .calls(port -> verify(port).consultSellerProducts(same(USER), eq("s-1")))
                        .jsonList(PRODUCT_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/products/{productId}/inventories", "p-1")
                        .stub(port -> when(port.consultProductInventory(any(), any())).thenReturn(List.of(INVENTORY)))
                        .calls(port -> verify(port).consultProductInventory(same(USER), eq("p-1")))
                        .jsonList(INVENTORY_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/warehouses/{warehouseId}/inventories", "w-1")
                        .stub(port -> when(port.consultWarehouseInventory(any(), any())).thenReturn(List.of(INVENTORY)))
                        .calls(port -> verify(port).consultWarehouseInventory(same(USER), eq("w-1")))
                        .jsonList(INVENTORY_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/buyers/{buyerId}/cart", "b-1")
                        .stub(port -> when(port.consultCart(any(), any())).thenReturn(CART))
                        .calls(port -> verify(port).consultCart(same(USER), eq("b-1")))
                        .json(CART_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/orders/{orderId}", "o-1")
                        .stub(port -> when(port.consultOrder(any(), any())).thenReturn(ORDER))
                        .calls(port -> verify(port).consultOrder(same(USER), eq("o-1")))
                        .json(ORDER_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/buyers/{buyerId}/orders", "b-1")
                        .stub(port -> when(port.consultBuyerOrders(any(), any())).thenReturn(List.of(ORDER)))
                        .calls(port -> verify(port).consultBuyerOrders(same(USER), eq("b-1")))
                        .jsonList(ORDER_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/orders/{orderId}/invoice", "o-1")
                        .stub(port -> when(port.consultInvoice(any(), any())).thenReturn(INVOICE))
                        .calls(port -> verify(port).consultInvoice(same(USER), eq("o-1")))
                        .json(INVOICE_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/returns/{returnRequestId}", "r-1")
                        .stub(port -> when(port.consultReturn(any(), any())).thenReturn(RETURN_REQUEST))
                        .calls(port -> verify(port).consultReturn(same(USER), eq("r-1")))
                        .json(RETURN_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/returns/{returnRequestId}/refund", "r-1")
                        .stub(port -> when(port.consultRefund(any(), any())).thenReturn(REFUND))
                        .calls(port -> verify(port).consultRefund(same(USER), eq("r-1")))
                        .json(REFUND_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/audit-logs")
                        .stub(port -> when(port.consultAuditLogs(any())).thenReturn(List.of(AUDIT_LOG)))
                        .calls(port -> verify(port).consultAuditLogs(same(USER)))
                        .jsonList(AUDIT_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/audit-logs/users/{userId}", "u-1")
                        .stub(port -> when(port.consultAuditLogsByUser(any(), any())).thenReturn(List.of(AUDIT_LOG)))
                        .calls(port -> verify(port).consultAuditLogsByUser(same(USER), eq("u-1")))
                        .jsonList(AUDIT_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/audit-logs/entities/{entityType}/{entityId}", "ORDER", "o-1")
                        .stub(port -> when(port.consultAuditLogsByEntity(any(), any(), any())).thenReturn(List.of(AUDIT_LOG)))
                        .calls(port -> verify(port).consultAuditLogsByEntity(same(USER), eq(AuditEntityType.ORDER), eq("o-1")))
                        .jsonList(AUDIT_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/reports/commercial")
                        .param("startDate", START_DATE).param("endDate", END_DATE)
                        .stub(port -> when(port.consultCommercialReport(any(), any())).thenReturn(COMMERCIAL_REPORT))
                        .calls(port -> verify(port).consultCommercialReport(same(USER), eq(PERIOD)))
                        .json(COMMERCIAL_REPORT_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/reports/sellers/{sellerId}/performance", "s-1")
                        .param("startDate", START_DATE).param("endDate", END_DATE)
                        .stub(port -> when(port.consultSellerPerformanceReport(any(), any(), any()))
                                .thenReturn(PERFORMANCE_REPORT))
                        .calls(port -> verify(port).consultSellerPerformanceReport(same(USER), eq("s-1"), eq(PERIOD)))
                        .json(PERFORMANCE_REPORT_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/reports/inventory")
                        .param("lowStockThreshold", THRESHOLD)
                        .stub(port -> when(port.consultInventoryReport(any(), anyInt())).thenReturn(INVENTORY_REPORT))
                        .calls(port -> verify(port).consultInventoryReport(same(USER), eq(5)))
                        .json(INVENTORY_REPORT_JSON),
                AS_SUPERVISOR.get("/api/v1/supervisor/reports/logistics")
                        .param("startDate", START_DATE).param("endDate", END_DATE)
                        .stub(port -> when(port.consultLogisticsReport(any(), any())).thenReturn(LOGISTICS_REPORT))
                        .calls(port -> verify(port).consultLogisticsReport(same(USER), eq(PERIOD)))
                        .json(LOGISTICS_REPORT_JSON));
    }
}
