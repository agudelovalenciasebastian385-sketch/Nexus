package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.InventoryCondition;

/**
 * Input port: RegisterInventoryAdjustment use case.
 *
 * <p>Applies a manual correction of stock and/or changes the condition of an inventory record (e.g. marks it {@code DAMAGED}).</p>
 */
public interface RegisterInventoryAdjustmentUseCase {

    Inventory registerInventoryAdjustment(User requestingUser, String productId, String warehouseId,
                                          int quantity, InventoryCondition condition);
}
