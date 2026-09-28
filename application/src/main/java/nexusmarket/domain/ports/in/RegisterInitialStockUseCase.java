package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.User;

/**
 * Input port: RegisterInitialStock use case.
 *
 * <p>Creates the inventory record of a physical product in a warehouse with its initial stock.</p>
 */
public interface RegisterInitialStockUseCase {

    Inventory registerInitialStock(User requestingUser, String productId, String warehouseId, int quantity);
}
