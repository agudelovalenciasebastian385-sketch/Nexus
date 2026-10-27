package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryReport;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.InventoryCondition;

/**
 * Role input port ({@code LOGISTICS_OPERATOR}).
 *
 * <p>Physical operation of warehouses: inventory movements, reservations, the shipment lifecycle and the inventory report.</p>
 */
public interface LogisticsOperatorPort {

    User consultUser(User requestingUser, String userId);

    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);

    Warehouse consultWarehouse(User requestingUser, String warehouseId);

    List<Warehouse> consultWarehouses(User requestingUser);

    Inventory registerInitialStock(User requestingUser, String productId, String warehouseId, int quantity);

    Inventory registerInventoryInbound(User requestingUser, String productId, String warehouseId,
                                       int quantity);

    Inventory registerInventoryAdjustment(User requestingUser, String productId, String warehouseId,
                                          int quantity, InventoryCondition condition);

    Inventory reserveInventory(User requestingUser, String productId, String warehouseId, int quantity);

    Inventory releaseInventoryReservation(User requestingUser, String productId, String warehouseId,
                                          int quantity);

    List<Inventory> consultProductInventory(User requestingUser, String productId);

    List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId);

    Order consultOrder(User requestingUser, String orderId);

    List<Shipment> createShipments(User requestingUser, String orderId);

    Shipment packShipment(User requestingUser, String shipmentId);

    Shipment dispatchShipment(User requestingUser, String shipmentId);

    Shipment confirmDelivery(User requestingUser, String shipmentId);

    InventoryReport consultInventoryReport(User requestingUser, int lowStockThreshold);
}
