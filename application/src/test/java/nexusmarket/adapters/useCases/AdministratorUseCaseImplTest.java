package nexusmarket.adapters.useCases;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static nexusmarket.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Map;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.support.FakePasswordService;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
import nexusmarket.domain.valueobjects.WarehouseType;
import org.junit.jupiter.api.Test;

class AdministratorUseCaseImplTest {

    private static final ReportPeriod PERIOD = new ReportPeriod(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

    private final ServiceHarness h = new ServiceHarness();
    private final AdministratorUseCaseImpl useCase = UseCaseFactory.administrator(h);
    private final Administrator admin = h.admin();
    private final Buyer buyer = h.buyer();
    private final Seller seller = h.seller();
    private final Warehouse warehouse = h.firstWarehouse(seller);
    private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00");

    AdministratorUseCaseImplTest() {
        h.stock(mouse, warehouse, 10);
    }

    private ReturnRequest requestedReturn() {
        Order delivered = h.deliveredOrder(buyer, mouse, 1, h.operator());
        return h.requestReturn.requestReturn(buyer, delivered.getId(), "Wrong color");
    }

    private AuditLog storedAuditLog() {
        return h.audit.save(new AuditLog("audit-1", AuditAction.USER_LOGGED_IN, buyer, AuditEntityType.USER,
                buyer.getId(), NOW, Map.of()));
    }

    @Test
    void registerStaffUserStoresTheUser() {
        User staff = useCase.registerStaffUser(admin, SystemRole.SUPERVISOR, "5002", "Pedro", "pedro@nexusmarket.com",
                "Staff123*");

        assertThat(h.users.findById(staff.getId())).contains(staff);
    }

    @Test
    void consultUserReturnsTheUser() {
        assertThat(useCase.consultUser(admin, buyer.getId())).isSameAs(buyer);
    }

    @Test
    void changeUserStatusChangesTheStatus() {
        useCase.changeUserStatus(admin, buyer.getId(), UserStatus.BLOCKED);

        assertThat(buyer.getStatus()).isEqualTo(UserStatus.BLOCKED);
    }

    @Test
    void changeUserPasswordStoresTheNewHash() {
        Administrator withPassword = (Administrator) h.users.save(new Administrator("admin-2", "9001", "Admin",
                "admin2@nexusmarket.com", FakePasswordService.hash("Current123*"), UserStatus.ACTIVE));

        useCase.changeUserPassword(withPassword, "Current123*", "Newpass123*");

        assertThat(withPassword.getPassword()).isEqualTo(FakePasswordService.hash("Newpass123*"));
    }

    @Test
    void registerSellerStoresTheSeller() {
        Seller registered = useCase.registerSeller(admin, "1050", "Tech Store", "s@gmail.com", "Seller123*", "Main",
                ADDRESS);

        assertThat(h.sellers.findById(registered.getId())).contains(registered);
    }

    @Test
    void consultSellerReturnsTheSeller() {
        assertThat(useCase.consultSeller(admin, seller.getId())).isSameAs(seller);
    }

    @Test
    void updateSellerChangesTheName() {
        useCase.updateSeller(admin, seller.getId(), "New Name");

        assertThat(seller.getFullName()).isEqualTo("New Name");
    }

    @Test
    void consultBuyerReturnsTheBuyer() {
        assertThat(useCase.consultBuyer(admin, buyer.getId())).isSameAs(buyer);
    }

    @Test
    void changeBuyerCommercialStatusChangesTheStatus() {
        useCase.changeBuyerCommercialStatus(admin, buyer.getId(), CommercialStatus.RESTRICTED);

        assertThat(buyer.getCommercialStatus()).isEqualTo(CommercialStatus.RESTRICTED);
    }

    @Test
    void registerWarehouseStoresTheMarketplaceWarehouse() {
        Warehouse registered = useCase.registerWarehouse(admin, "Hub", ADDRESS, WarehouseType.MARKETPLACE);

        assertThat(h.warehouses.findById(registered.getId())).contains(registered);
    }

    @Test
    void consultWarehouseReturnsTheWarehouse() {
        assertThat(useCase.consultWarehouse(admin, warehouse.getId())).isSameAs(warehouse);
    }

    @Test
    void consultWarehousesIncludesTheWarehouse() {
        assertThat(useCase.consultWarehouses(admin)).contains(warehouse);
    }

    @Test
    void updateWarehouseChangesTheName() {
        Warehouse hub = h.marketplaceWarehouse();

        useCase.updateWarehouse(admin, hub.getId(), "New Hub", ADDRESS);

        assertThat(hub.getName()).isEqualTo("New Hub");
    }

    @Test
    void consultSellerProductsReturnsTheProducts() {
        assertThat(useCase.consultSellerProducts(admin, seller.getId())).containsExactly(mouse);
    }

    @Test
    void consultInvoiceReturnsTheInvoiceOfThePaidOrder() {
        Order paid = h.paidOrder(buyer, mouse, 1);

        assertThat(useCase.consultInvoice(admin, paid.getId()).getOrder()).isSameAs(paid);
    }

    @Test
    void approveReturnIsAudited() {
        ReturnRequest returnRequest = requestedReturn();

        useCase.approveReturn(admin, returnRequest.getId());

        assertThat(h.audit.actions()).contains(AuditAction.RETURN_APPROVED);
    }

    @Test
    void rejectReturnIsAudited() {
        ReturnRequest returnRequest = requestedReturn();

        useCase.rejectReturn(admin, returnRequest.getId());

        assertThat(h.audit.actions()).contains(AuditAction.RETURN_REJECTED);
    }

    @Test
    void processRefundIsAudited() {
        ReturnRequest returnRequest = requestedReturn();
        h.approveReturn.approveReturn(admin, returnRequest.getId());
        Refund refund = h.refunds.findByReturnRequest(returnRequest).orElseThrow();

        useCase.processRefund(admin, refund.getId());

        assertThat(h.audit.actions()).contains(AuditAction.REFUND_PROCESSED);
    }

    @Test
    void consultReturnReturnsTheRequest() {
        ReturnRequest returnRequest = requestedReturn();

        assertThat(useCase.consultReturn(admin, returnRequest.getId())).isSameAs(returnRequest);
    }

    @Test
    void consultRefundReturnsTheRefundOfTheApprovedReturn() {
        ReturnRequest returnRequest = requestedReturn();
        h.approveReturn.approveReturn(admin, returnRequest.getId());

        assertThat(useCase.consultRefund(admin, returnRequest.getId()).getReturnRequest()).isSameAs(returnRequest);
    }

    @Test
    void consultAuditLogsReturnsTheLogs() {
        AuditLog auditLog = storedAuditLog();

        assertThat(useCase.consultAuditLogs(admin)).contains(auditLog);
    }

    @Test
    void consultAuditLogsByUserReturnsTheLogsOfTheUser() {
        AuditLog auditLog = storedAuditLog();

        assertThat(useCase.consultAuditLogsByUser(admin, buyer.getId())).contains(auditLog);
    }

    @Test
    void consultAuditLogsByEntityReturnsTheLogsOfTheEntity() {
        AuditLog auditLog = storedAuditLog();

        assertThat(useCase.consultAuditLogsByEntity(admin, AuditEntityType.USER, buyer.getId())).containsExactly(auditLog);
    }

    @Test
    void consultCommercialReportUsesThePeriod() {
        assertThat(useCase.consultCommercialReport(admin, PERIOD).getPeriod()).isEqualTo(PERIOD);
    }

    @Test
    void consultSellerPerformanceReportIsOfTheSeller() {
        assertThat(useCase.consultSellerPerformanceReport(admin, seller.getId(), PERIOD).getSeller()).isSameAs(seller);
    }

    @Test
    void consultInventoryReportUsesTheThreshold() {
        assertThat(useCase.consultInventoryReport(admin, 7).getLowStockThreshold()).isEqualTo(7);
    }

    @Test
    void consultLogisticsReportUsesThePeriod() {
        assertThat(useCase.consultLogisticsReport(admin, PERIOD).getPeriod()).isEqualTo(PERIOD);
    }
}
