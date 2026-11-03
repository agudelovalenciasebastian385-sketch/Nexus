package nexusmarket.adapters.rest;

import static nexusmarket.adapters.rest.RestResponseFixtures.ADDRESS_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.AUDIT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.AUDIT_LOG;
import static nexusmarket.adapters.rest.RestResponseFixtures.BUYER;
import static nexusmarket.adapters.rest.RestResponseFixtures.BUYER_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.COMMERCIAL_REPORT;
import static nexusmarket.adapters.rest.RestResponseFixtures.COMMERCIAL_REPORT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.END_DATE;
import static nexusmarket.adapters.rest.RestResponseFixtures.FULL_NAME_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY_REPORT;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVENTORY_REPORT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVOICE;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVOICE_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.LOGISTICS_REPORT;
import static nexusmarket.adapters.rest.RestResponseFixtures.LOGISTICS_REPORT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.PASSWORD_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.PERFORMANCE_REPORT;
import static nexusmarket.adapters.rest.RestResponseFixtures.PERFORMANCE_REPORT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.PERIOD;
import static nexusmarket.adapters.rest.RestResponseFixtures.PRODUCT;
import static nexusmarket.adapters.rest.RestResponseFixtures.PRODUCT_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.REFUND;
import static nexusmarket.adapters.rest.RestResponseFixtures.REFUND_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.REQUEST_ADDRESS;
import static nexusmarket.adapters.rest.RestResponseFixtures.RETURN_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.RETURN_REQUEST;
import static nexusmarket.adapters.rest.RestResponseFixtures.SELLER;
import static nexusmarket.adapters.rest.RestResponseFixtures.SELLER_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.START_DATE;
import static nexusmarket.adapters.rest.RestResponseFixtures.THRESHOLD;
import static nexusmarket.adapters.rest.RestResponseFixtures.WAREHOUSE;
import static nexusmarket.adapters.rest.RestResponseFixtures.WAREHOUSE_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.WAREHOUSE_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.userJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.Supervisor;
import nexusmarket.domain.ports.in.AdministratorPort;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
import nexusmarket.domain.valueobjects.WarehouseType;

class AdministratorRestControllerWebTest extends EndpointWebTestBase<AdministratorPort> {

    private static final Administrator USER = DomainFixtures.administrator();
    private static final Supervisor OTHER_USER = DomainFixtures.supervisor();

    private static final EndpointCase.Routes<AdministratorPort> AS_ADMIN = new EndpointCase.Routes<>(USER);

    @Override
    protected AdministratorPort port() {
        return administratorPort;
    }

    static Stream<EndpointCase<AdministratorPort>> endpoints() {
        return Stream.of(
                AS_ADMIN.put("/api/v1/admin/account/password")
                        .body(PASSWORD_BODY)
                        .calls(port -> verify(port).changeUserPassword(same(USER), eq("Current123*"), eq("Newpass123*")))
                        .status(204),
                AS_ADMIN.post("/api/v1/admin/staff-users")
                        .body("""
                                {"role": "SUPERVISOR", "identificationNumber": "1040", "fullName": "Pedro",
                                 "email": "p@b.com", "password": "Staff123*"}""")
                        .stub(port -> when(port.registerStaffUser(any(), any(), any(), any(), any(), any()))
                                .thenReturn(OTHER_USER))
                        .calls(port -> verify(port).registerStaffUser(same(USER), eq(SystemRole.SUPERVISOR), eq("1040"),
                                eq("Pedro"), eq("p@b.com"), eq("Staff123*")))
                        .status(201)
                        .json(userJson(OTHER_USER)),
                AS_ADMIN.get("/api/v1/admin/users/{userId}", "u-1")
                        .stub(port -> when(port.consultUser(any(), any())).thenReturn(OTHER_USER))
                        .calls(port -> verify(port).consultUser(same(USER), eq("u-1")))
                        .json(userJson(OTHER_USER)),
                AS_ADMIN.patch("/api/v1/admin/users/{userId}/status", "u-1")
                        .body("{\"status\": \"BLOCKED\"}")
                        .stub(port -> when(port.changeUserStatus(any(), any(), any())).thenReturn(OTHER_USER))
                        .calls(port -> verify(port).changeUserStatus(same(USER), eq("u-1"), eq(UserStatus.BLOCKED)))
                        .json(userJson(OTHER_USER)),
                AS_ADMIN.post("/api/v1/admin/sellers")
                        .body("""
                                {"identificationNumber": "1050", "fullName": "Tech Store", "email": "s@b.com",
                                 "password": "Seller123*", "warehouseName": "Main", "warehouseLocation": %s}"""
                                .formatted(ADDRESS_BODY))
                        .stub(port -> when(port.registerSeller(any(), any(), any(), any(), any(), any(), any()))
                                .thenReturn(SELLER))
                        .calls(port -> verify(port).registerSeller(same(USER), eq("1050"), eq("Tech Store"), eq("s@b.com"),
                                eq("Seller123*"), eq("Main"), eq(REQUEST_ADDRESS)))
                        .status(201)
                        .json(Map.of("seller.id", SELLER.getId(), "seller.email", SELLER.getEmail(),
                                "firstWarehouse.id", WAREHOUSE.getId())),
                AS_ADMIN.get("/api/v1/admin/sellers/{sellerId}", "s-1")
                        .stub(port -> when(port.consultSeller(any(), any())).thenReturn(SELLER))
                        .calls(port -> verify(port).consultSeller(same(USER), eq("s-1")))
                        .json(SELLER_JSON),
                AS_ADMIN.patch("/api/v1/admin/sellers/{sellerId}", "s-1")
                        .body(FULL_NAME_BODY)
                        .stub(port -> when(port.updateSeller(any(), any(), any())).thenReturn(SELLER))
                        .calls(port -> verify(port).updateSeller(same(USER), eq("s-1"), eq("New Name")))
                        .json(SELLER_JSON),
                AS_ADMIN.get("/api/v1/admin/sellers/{sellerId}/products", "s-1")
                        .stub(port -> when(port.consultSellerProducts(any(), any())).thenReturn(List.of(PRODUCT)))
                        .calls(port -> verify(port).consultSellerProducts(same(USER), eq("s-1")))
                        .jsonList(PRODUCT_JSON),
                AS_ADMIN.get("/api/v1/admin/buyers/{buyerId}", "b-1")
                        .stub(port -> when(port.consultBuyer(any(), any())).thenReturn(BUYER))
                        .calls(port -> verify(port).consultBuyer(same(USER), eq("b-1")))
                        .json(BUYER_JSON),
                AS_ADMIN.patch("/api/v1/admin/buyers/{buyerId}/commercial-status", "b-1")
                        .body("{\"commercialStatus\": \"RESTRICTED\"}")
                        .stub(port -> when(port.changeBuyerCommercialStatus(any(), any(), any())).thenReturn(BUYER))
                        .calls(port -> verify(port).changeBuyerCommercialStatus(same(USER), eq("b-1"),
                                eq(CommercialStatus.RESTRICTED)))
                        .json(BUYER_JSON),
                AS_ADMIN.post("/api/v1/admin/warehouses")
                        .body(WAREHOUSE_BODY)
                        .stub(port -> when(port.registerWarehouse(any(), any(), any(), any())).thenReturn(WAREHOUSE))
                        .calls(port -> verify(port).registerWarehouse(same(USER), eq("Hub"), eq(REQUEST_ADDRESS),
                                eq(WarehouseType.MARKETPLACE)))
                        .status(201)
                        .json(WAREHOUSE_JSON),
                AS_ADMIN.get("/api/v1/admin/warehouses")
                        .stub(port -> when(port.consultWarehouses(any())).thenReturn(List.of(WAREHOUSE)))
                        .calls(port -> verify(port).consultWarehouses(same(USER)))
                        .jsonList(WAREHOUSE_JSON),
                AS_ADMIN.get("/api/v1/admin/warehouses/{warehouseId}", "w-1")
                        .stub(port -> when(port.consultWarehouse(any(), any())).thenReturn(WAREHOUSE))
                        .calls(port -> verify(port).consultWarehouse(same(USER), eq("w-1")))
                        .json(WAREHOUSE_JSON),
                AS_ADMIN.put("/api/v1/admin/warehouses/{warehouseId}", "w-1")
                        .body(WAREHOUSE_BODY)
                        .stub(port -> when(port.updateWarehouse(any(), any(), any(), any())).thenReturn(WAREHOUSE))
                        .calls(port -> verify(port).updateWarehouse(same(USER), eq("w-1"), eq("Hub"), eq(REQUEST_ADDRESS)))
                        .json(WAREHOUSE_JSON),
                AS_ADMIN.get("/api/v1/admin/orders/{orderId}/invoice", "o-1")
                        .stub(port -> when(port.consultInvoice(any(), any())).thenReturn(INVOICE))
                        .calls(port -> verify(port).consultInvoice(same(USER), eq("o-1")))
                        .json(INVOICE_JSON),
                AS_ADMIN.get("/api/v1/admin/returns/{returnRequestId}", "r-1")
                        .stub(port -> when(port.consultReturn(any(), any())).thenReturn(RETURN_REQUEST))
                        .calls(port -> verify(port).consultReturn(same(USER), eq("r-1")))
                        .json(RETURN_JSON),
                AS_ADMIN.patch("/api/v1/admin/returns/{returnRequestId}/approve", "r-1")
                        .stub(port -> when(port.approveReturn(any(), any())).thenReturn(RETURN_REQUEST))
                        .calls(port -> verify(port).approveReturn(same(USER), eq("r-1")))
                        .json(RETURN_JSON),
                AS_ADMIN.patch("/api/v1/admin/returns/{returnRequestId}/reject", "r-1")
                        .stub(port -> when(port.rejectReturn(any(), any())).thenReturn(RETURN_REQUEST))
                        .calls(port -> verify(port).rejectReturn(same(USER), eq("r-1")))
                        .json(RETURN_JSON),
                AS_ADMIN.get("/api/v1/admin/returns/{returnRequestId}/refund", "r-1")
                        .stub(port -> when(port.consultRefund(any(), any())).thenReturn(REFUND))
                        .calls(port -> verify(port).consultRefund(same(USER), eq("r-1")))
                        .json(REFUND_JSON),
                AS_ADMIN.patch("/api/v1/admin/refunds/{refundId}/process", "rf-1")
                        .stub(port -> when(port.processRefund(any(), any())).thenReturn(REFUND))
                        .calls(port -> verify(port).processRefund(same(USER), eq("rf-1")))
                        .json(REFUND_JSON),
                AS_ADMIN.get("/api/v1/admin/audit-logs")
                        .stub(port -> when(port.consultAuditLogs(any())).thenReturn(List.of(AUDIT_LOG)))
                        .calls(port -> verify(port).consultAuditLogs(same(USER)))
                        .jsonList(AUDIT_JSON),
                AS_ADMIN.get("/api/v1/admin/audit-logs/users/{userId}", "u-1")
                        .stub(port -> when(port.consultAuditLogsByUser(any(), any())).thenReturn(List.of(AUDIT_LOG)))
                        .calls(port -> verify(port).consultAuditLogsByUser(same(USER), eq("u-1")))
                        .jsonList(AUDIT_JSON),
                AS_ADMIN.get("/api/v1/admin/audit-logs/entities/{entityType}/{entityId}", "ORDER", "o-1")
                        .stub(port -> when(port.consultAuditLogsByEntity(any(), any(), any())).thenReturn(List.of(AUDIT_LOG)))
                        .calls(port -> verify(port).consultAuditLogsByEntity(same(USER), eq(AuditEntityType.ORDER), eq("o-1")))
                        .jsonList(AUDIT_JSON),
                AS_ADMIN.get("/api/v1/admin/reports/commercial")
                        .param("startDate", START_DATE).param("endDate", END_DATE)
                        .stub(port -> when(port.consultCommercialReport(any(), any())).thenReturn(COMMERCIAL_REPORT))
                        .calls(port -> verify(port).consultCommercialReport(same(USER), eq(PERIOD)))
                        .json(COMMERCIAL_REPORT_JSON),
                AS_ADMIN.get("/api/v1/admin/reports/sellers/{sellerId}/performance", "s-1")
                        .param("startDate", START_DATE).param("endDate", END_DATE)
                        .stub(port -> when(port.consultSellerPerformanceReport(any(), any(), any()))
                                .thenReturn(PERFORMANCE_REPORT))
                        .calls(port -> verify(port).consultSellerPerformanceReport(same(USER), eq("s-1"), eq(PERIOD)))
                        .json(PERFORMANCE_REPORT_JSON),
                AS_ADMIN.get("/api/v1/admin/reports/inventory")
                        .param("lowStockThreshold", THRESHOLD)
                        .stub(port -> when(port.consultInventoryReport(any(), anyInt())).thenReturn(INVENTORY_REPORT))
                        .calls(port -> verify(port).consultInventoryReport(same(USER), eq(5)))
                        .json(INVENTORY_REPORT_JSON),
                AS_ADMIN.get("/api/v1/admin/reports/logistics")
                        .param("startDate", START_DATE).param("endDate", END_DATE)
                        .stub(port -> when(port.consultLogisticsReport(any(), any())).thenReturn(LOGISTICS_REPORT))
                        .calls(port -> verify(port).consultLogisticsReport(same(USER), eq(PERIOD)))
                        .json(LOGISTICS_REPORT_JSON));
    }
}
