package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.Address;

/**
 * Input port: UpdateWarehouse use case.
 *
 * <p>Updates the name and location of a warehouse.</p>
 */
public interface UpdateWarehouseUseCase {

    Warehouse updateWarehouse(User requestingUser, String warehouseId, String name, Address location);
}
