package nexusmarket.adapters.useCases;

import static nexusmarket.domain.DomainFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Map;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Supervisor;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.support.FakePasswordService;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.ReportPeriod;
import nexusmarket.domain.valueobjects.SystemRole;
import org.junit.jupiter.api.Test;

class SupervisorUseCaseImplTest {

    private static final ReportPeriod PERIOD = new ReportPeriod(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

    private final ServiceHarness h = new ServiceHarness();
    private final SupervisorUseCaseImpl useCase = UseCaseFactory.supervisor(h);
    private final Supervisor supervisor = h.supervisor();
    private final Buyer buyer = h.buyer();
    private final Seller seller = h.seller();
    private final Warehouse warehouse = h.firstWarehouse(seller);
    private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00");
    private final Inventory inventory = h.stock(mouse, warehouse, 10);

    private ReturnRequest requestedReturn() {
        Order delivered = h.deliveredOrder(buyer, mouse, 1, h.operator());
        return h.requestReturn.requestReturn(buyer, delivered.getId(), "Wrong color");
    }

    private AuditLog storedAuditLog() {
        return h.audit.save(new AuditLog("audit-1", AuditAction.USER_LOGGED_IN, buyer, AuditEntityType.USER,
                buyer.getId(), NOW, Map.of()));
    }

    @Test
    void consultUserReturnsTheUser() {
        assertThat(useCase.consultUser(supervisor, buyer.getId())).isSameAs(buyer);
    }

    @Test
    void changeUserPasswordStoresTheNewHash() {
        User registered = h.registerStaffUser.registerStaffUser(h.admin(), SystemRole.SUPERVISOR, "5002", "Pedro",
                "pedro@nexusmarket.com", "Current123*");

        useCase.changeUserPassword(registered, "Current123*", "Newpass123*");

        assertThat(registered.getPassword()).isEqualTo(FakePasswordService.hash("Newpass123*"));
    }

    @Test
    void consultSellerReturnsTheSeller() {
        assertThat(useCase.consultSeller(supervisor, seller.getId())).isSameAs(seller);
    }

    @Test
    void consultBuyerReturnsTheBuyer() {
        assertThat(useCase.consultBuyer(supervisor, buyer.getId())).isSameAs(buyer);
    }

    @Test
    void consultWarehouseReturnsTheWarehouse() {
        assertThat(useCase.consultWarehouse(supervisor, warehouse.getId())).isSameAs(warehouse);
    }

    @Test
    void consultWarehousesIncludesTheWarehouse() {
        assertThat(useCase.consultWarehouses(supervisor)).contains(warehouse);
    }

    @Test
    void consultSellerProductsReturnsTheProducts() {
        assertThat(useCase.consultSellerProducts(supervisor, seller.getId())).containsExactly(mouse);
    }

    @Test
    void consultProductInventoryReturnsTheRecords() {
        assertThat(useCase.consultProductInventory(supervisor, mouse.getId())).containsExactly(inventory);
    }

    @Test
    void consultWarehouseInventoryReturnsTheRecords() {
        assertThat(useCase.consultWarehouseInventory(supervisor, warehouse.getId())).containsExactly(inventory);
    }

    @Test
    void consultCartReturnsTheCartOfTheBuyer() {
        h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 2);

        assertThat(useCase.consultCart(supervisor, buyer.getId()).getItems()).hasSize(1);
    }

    @Test
    void consultOrderReturnsTheOrder() {
        Order order = h.confirmedOrder(buyer, mouse, 1);

        assertThat(useCase.consultOrder(supervisor, order.getId())).isSameAs(order);
    }

    @Test
    void consultBuyerOrdersReturnsTheOrdersOfTheBuyer() {
        Order order = h.confirmedOrder(buyer, mouse, 1);

        assertThat(useCase.consultBuyerOrders(supervisor, buyer.getId())).containsExactly(order);
    }

    @Test
    void consultInvoiceReturnsTheInvoiceOfThePaidOrder() {
        Order paid = h.paidOrder(buyer, mouse, 1);

        assertThat(useCase.consultInvoice(supervisor, paid.getId()).getOrder()).isSameAs(paid);
    }

    @Test
    void consultReturnReturnsTheRequest() {
        ReturnRequest returnRequest = requestedReturn();

        assertThat(useCase.consultReturn(supervisor, returnRequest.getId())).isSameAs(returnRequest);
    }

    @Test
    void consultRefundReturnsTheRefundOfTheApprovedReturn() {
        ReturnRequest returnRequest = requestedReturn();
        h.approveReturn.approveReturn(h.admin(), returnRequest.getId());

        assertThat(useCase.consultRefund(supervisor, returnRequest.getId()).getReturnRequest()).isSameAs(returnRequest);
    }

    @Test
    void consultAuditLogsReturnsTheLogs() {
        AuditLog auditLog = storedAuditLog();

        assertThat(useCase.consultAuditLogs(supervisor)).contains(auditLog);
    }

    @Test
    void consultAuditLogsByUserReturnsTheLogsOfTheUser() {
        AuditLog auditLog = storedAuditLog();

        assertThat(useCase.consultAuditLogsByUser(supervisor, buyer.getId())).contains(auditLog);
    }

    @Test
    void consultAuditLogsByEntityReturnsTheLogsOfTheEntity() {
        AuditLog auditLog = storedAuditLog();

        assertThat(useCase.consultAuditLogsByEntity(supervisor, AuditEntityType.USER, buyer.getId()))
                .containsExactly(auditLog);
    }

    @Test
    void consultCommercialReportUsesThePeriod() {
        assertThat(useCase.consultCommercialReport(supervisor, PERIOD).getPeriod()).isEqualTo(PERIOD);
    }

    @Test
    void consultSellerPerformanceReportIsOfTheSeller() {
        assertThat(useCase.consultSellerPerformanceReport(supervisor, seller.getId(), PERIOD).getSeller()).isSameAs(seller);
    }

    @Test
    void consultInventoryReportUsesTheThreshold() {
        assertThat(useCase.consultInventoryReport(supervisor, 7).getLowStockThreshold()).isEqualTo(7);
    }

    @Test
    void consultLogisticsReportUsesThePeriod() {
        assertThat(useCase.consultLogisticsReport(supervisor, PERIOD).getPeriod()).isEqualTo(PERIOD);
    }
}
