package nexusmarket.adapters.useCases;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryReport;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.in.LogisticsOperatorPort;
import nexusmarket.domain.services.inventory.ConsultInventoryService;
import nexusmarket.domain.services.inventory.RegisterInitialStockService;
import nexusmarket.domain.services.inventory.RegisterInventoryAdjustmentService;
import nexusmarket.domain.services.inventory.RegisterInventoryInboundService;
import nexusmarket.domain.services.inventory.ReleaseInventoryReservationService;
import nexusmarket.domain.services.inventory.ReserveInventoryService;
import nexusmarket.domain.services.order.ConsultOrderService;
import nexusmarket.domain.services.reporting.ConsultInventoryReportService;
import nexusmarket.domain.services.shipment.ConfirmDeliveryService;
import nexusmarket.domain.services.shipment.CreateShipmentsService;
import nexusmarket.domain.services.shipment.DispatchShipmentService;
import nexusmarket.domain.services.shipment.PackShipmentService;
import nexusmarket.domain.services.user.ChangeUserPasswordService;
import nexusmarket.domain.services.user.ConsultUserService;
import nexusmarket.domain.services.warehouse.ConsultWarehouseService;
import nexusmarket.domain.valueobjects.InventoryCondition;
import org.springframework.stereotype.Service;

/**
 * Use case adapter of {@link LogisticsOperatorPort}: delegates every operation to the
 * corresponding domain service (Use-cases-adapters.md). It contains no business
 * rules.
 */
@Service
@RequiredArgsConstructor
public class LogisticsOperatorUseCaseImpl implements LogisticsOperatorPort {

    private final ConsultUserService consultUserService;
    private final ChangeUserPasswordService changeUserPasswordService;
    private final ConsultWarehouseService consultWarehouseService;
    private final RegisterInitialStockService registerInitialStockService;
    private final RegisterInventoryInboundService registerInventoryInboundService;
    private final RegisterInventoryAdjustmentService registerInventoryAdjustmentService;
    private final ReserveInventoryService reserveInventoryService;
    private final ReleaseInventoryReservationService releaseInventoryReservationService;
    private final ConsultInventoryService consultInventoryService;
    private final ConsultOrderService consultOrderService;
    private final CreateShipmentsService createShipmentsService;
    private final PackShipmentService packShipmentService;
    private final DispatchShipmentService dispatchShipmentService;
    private final ConfirmDeliveryService confirmDeliveryService;
    private final ConsultInventoryReportService consultInventoryReportService;

    @Override
    public User consultUser(User requestingUser, String userId) {
        return consultUserService.consultUser(requestingUser, userId);
    }

    @Override
    public void changeUserPassword(User requestingUser, String currentPassword, String newPassword) {
        changeUserPasswordService.changeUserPassword(requestingUser, currentPassword, newPassword);
    }

    @Override
    public Warehouse consultWarehouse(User requestingUser, String warehouseId) {
        return consultWarehouseService.consultWarehouse(requestingUser, warehouseId);
    }

    @Override
    public List<Warehouse> consultWarehouses(User requestingUser) {
        return consultWarehouseService.consultWarehouses(requestingUser);
    }

    @Override
    public Inventory registerInitialStock(User requestingUser, String productId, String warehouseId,
                                          int quantity) {
        return registerInitialStockService.registerInitialStock(requestingUser, productId, warehouseId, quantity);
    }

    @Override
    public Inventory registerInventoryInbound(User requestingUser, String productId, String warehouseId,
                                              int quantity) {
        return registerInventoryInboundService.registerInventoryInbound(requestingUser, productId, warehouseId, quantity);
    }

    @Override
    public Inventory registerInventoryAdjustment(User requestingUser, String productId, String warehouseId,
                                                 int quantity, InventoryCondition condition) {
        return registerInventoryAdjustmentService.registerInventoryAdjustment(requestingUser, productId, warehouseId, quantity, condition);
    }

    @Override
    public Inventory reserveInventory(User requestingUser, String productId, String warehouseId,
                                      int quantity) {
        return reserveInventoryService.reserveInventory(requestingUser, productId, warehouseId, quantity);
    }

    @Override
    public Inventory releaseInventoryReservation(User requestingUser, String productId, String warehouseId,
                                                 int quantity) {
        return releaseInventoryReservationService.releaseInventoryReservation(requestingUser, productId, warehouseId, quantity);
    }

    @Override
    public List<Inventory> consultProductInventory(User requestingUser, String productId) {
        return consultInventoryService.consultProductInventory(requestingUser, productId);
    }

    @Override
    public List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId) {
        return consultInventoryService.consultWarehouseInventory(requestingUser, warehouseId);
    }

    @Override
    public Order consultOrder(User requestingUser, String orderId) {
        return consultOrderService.consultOrder(requestingUser, orderId);
    }

    @Override
    public List<Shipment> createShipments(User requestingUser, String orderId) {
        return createShipmentsService.createShipments(requestingUser, orderId);
    }

    @Override
    public Shipment packShipment(User requestingUser, String shipmentId) {
        return packShipmentService.packShipment(requestingUser, shipmentId);
    }

    @Override
    public Shipment dispatchShipment(User requestingUser, String shipmentId) {
        return dispatchShipmentService.dispatchShipment(requestingUser, shipmentId);
    }

    @Override
    public Shipment confirmDelivery(User requestingUser, String shipmentId) {
        return confirmDeliveryService.confirmDelivery(requestingUser, shipmentId);
    }

    @Override
    public InventoryReport consultInventoryReport(User requestingUser, int lowStockThreshold) {
        return consultInventoryReportService.consultInventoryReport(requestingUser, lowStockThreshold);
    }
}
