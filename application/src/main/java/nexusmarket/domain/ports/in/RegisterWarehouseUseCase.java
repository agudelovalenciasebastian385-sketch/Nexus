package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.WarehouseType;

/**
 * Input port: RegisterWarehouse use case.
 *
 * <p>Registers a warehouse: the administrator registers Marketplace warehouses and a seller registers additional warehouses of its own.</p>
 */
public interface RegisterWarehouseUseCase {

    Warehouse registerWarehouse(User requestingUser, String name, Address location, WarehouseType type);
}
