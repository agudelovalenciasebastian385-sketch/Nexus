package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.User;

/**
 * Input port: ConsultInventory use case.
 *
 * <p>Retrieves the inventory records of a product or of a warehouse.</p>
 */
public interface ConsultInventoryUseCase {

    List<Inventory> consultProductInventory(User requestingUser, String productId);

    List<Inventory> consultWarehouseInventory(User requestingUser, String warehouseId);
}
