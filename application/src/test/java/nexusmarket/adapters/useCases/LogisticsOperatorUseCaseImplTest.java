package nexusmarket.adapters.useCases;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.support.FakePasswordService;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.InventoryCondition;
import nexusmarket.domain.valueobjects.SystemRole;
import org.junit.jupiter.api.Test;

class LogisticsOperatorUseCaseImplTest {

    private final ServiceHarness h = new ServiceHarness();
    private final LogisticsOperatorUseCaseImpl useCase = UseCaseFactory.logisticsOperator(h);
    private final LogisticsOperator operator = h.operator();
    private final Seller seller = h.seller();
    private final Warehouse hub = h.marketplaceWarehouse();
    private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00");

    private Shipment createdShipment() {
        h.stock(mouse, hub, 10);
        Order paid = h.paidOrder(h.buyer(), mouse, 1);
        return h.createShipments.createShipments(operator, paid.getId()).get(0);
    }

    @Test
    void consultUserReturnsTheOwnUser() {
        assertThat(useCase.consultUser(operator, operator.getId())).isSameAs(operator);
    }

    @Test
    void changeUserPasswordStoresTheNewHash() {
        Administrator admin = h.admin();
        User registered = h.registerStaffUser.registerStaffUser(admin, SystemRole.LOGISTICS_OPERATOR, "5001", "Laura",
                "laura@nexusmarket.com", "Current123*");

        useCase.changeUserPassword(registered, "Current123*", "Newpass123*");

        assertThat(registered.getPassword()).isEqualTo(FakePasswordService.hash("Newpass123*"));
    }

    @Test
    void consultWarehouseReturnsTheWarehouse() {
        assertThat(useCase.consultWarehouse(operator, hub.getId())).isSameAs(hub);
    }

    @Test
    void consultWarehousesIncludesTheMarketplaceWarehouse() {
        assertThat(useCase.consultWarehouses(operator)).contains(hub);
    }

    @Test
    void registerInitialStockStoresTheInventory() {
        Inventory inventory = useCase.registerInitialStock(operator, mouse.getId(), hub.getId(), 10);

        assertThat(h.inventoryOf(mouse, hub)).isSameAs(inventory);
    }

    @Test
    void registerInventoryInboundAddsTheQuantity() {
        h.stock(mouse, hub, 10);

        assertThat(useCase.registerInventoryInbound(operator, mouse.getId(), hub.getId(), 5).getQuantity()).isEqualTo(15);
    }

    @Test
    void registerInventoryAdjustmentIsAudited() {
        h.stock(mouse, hub, 10);

        useCase.registerInventoryAdjustment(operator, mouse.getId(), hub.getId(), -2, InventoryCondition.DAMAGED);

        assertThat(h.audit.actions()).contains(AuditAction.INVENTORY_ADJUSTED);
    }

    @Test
    void reserveInventoryReservesTheQuantity() {
        h.stock(mouse, hub, 10);

        assertThat(useCase.reserveInventory(operator, mouse.getId(), hub.getId(), 3).getReservedQuantity()).isEqualTo(3);
    }

    @Test
    void releaseInventoryReservationReleasesTheQuantity() {
        h.stock(mouse, hub, 10);
        h.reserveInventory.reserveInventory(operator, mouse.getId(), hub.getId(), 3);

        assertThat(useCase.releaseInventoryReservation(operator, mouse.getId(), hub.getId(), 1).getReservedQuantity())
                .isEqualTo(2);
    }

    @Test
    void consultProductInventoryReturnsTheRecords() {
        Inventory inventory = h.stock(mouse, hub, 10);

        assertThat(useCase.consultProductInventory(operator, mouse.getId())).containsExactly(inventory);
    }

    @Test
    void consultWarehouseInventoryReturnsTheRecords() {
        Inventory inventory = h.stock(mouse, hub, 10);

        assertThat(useCase.consultWarehouseInventory(operator, hub.getId())).containsExactly(inventory);
    }

    @Test
    void consultOrderReturnsTheOrder() {
        h.stock(mouse, hub, 10);
        Order order = h.confirmedOrder(h.buyer(), mouse, 1);

        assertThat(useCase.consultOrder(operator, order.getId())).isSameAs(order);
    }

    @Test
    void createShipmentsStoresTheShipments() {
        h.stock(mouse, hub, 10);
        Order paid = h.paidOrder(h.buyer(), mouse, 1);

        List<Shipment> shipments = useCase.createShipments(operator, paid.getId());

        assertThat(h.shipments.findById(shipments.get(0).getId())).contains(shipments.get(0));
    }

    @Test
    void packShipmentIsAudited() {
        Shipment shipment = createdShipment();

        useCase.packShipment(operator, shipment.getId());

        assertThat(h.audit.actions()).contains(AuditAction.SHIPMENT_PACKED);
    }

    @Test
    void dispatchShipmentIsAudited() {
        Shipment shipment = createdShipment();
        h.packShipment.packShipment(operator, shipment.getId());

        useCase.dispatchShipment(operator, shipment.getId());

        assertThat(h.audit.actions()).contains(AuditAction.SHIPMENT_DISPATCHED);
    }

    @Test
    void confirmDeliveryIsAudited() {
        Shipment shipment = createdShipment();
        h.packShipment.packShipment(operator, shipment.getId());
        h.dispatchShipment.dispatchShipment(operator, shipment.getId());

        useCase.confirmDelivery(operator, shipment.getId());

        assertThat(h.audit.actions()).contains(AuditAction.SHIPMENT_DELIVERED);
    }

    @Test
    void consultInventoryReportUsesTheThreshold() {
        assertThat(useCase.consultInventoryReport(operator, 7).getLowStockThreshold()).isEqualTo(7);
    }
}
