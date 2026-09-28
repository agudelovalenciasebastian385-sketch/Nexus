package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.User;

/**
 * Input port: ReserveInventory use case.
 *
 * <p>Holds stock of a physical product for an order in progress ({@code RESERVATION} movement: available → reserved).</p>
 */
public interface ReserveInventoryUseCase {

    Inventory reserveInventory(User requestingUser, String productId, String warehouseId, int quantity);
}
