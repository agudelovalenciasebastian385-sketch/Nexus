package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;

/**
 * Input port: ConsultWarehouse use case.
 *
 * <p>Retrieves one warehouse or the list of warehouses visible to the requesting user.</p>
 */
public interface ConsultWarehouseUseCase {

    Warehouse consultWarehouse(User requestingUser, String warehouseId);

    List<Warehouse> consultWarehouses(User requestingUser);
}
