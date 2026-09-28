package nexusmarket.domain.ports.out;

import java.util.List;
import java.util.Optional;

import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Warehouse;

/**
 * Output port: Persistence and queries of {@code Inventory} records (identified by product and warehouse), including their movements.
 */
public interface InventoryRepositoryPort {

    Inventory save(Inventory inventory);

    void update(Inventory inventory);

    Optional<Inventory> findByProductAndWarehouse(PhysicalProduct product, Warehouse warehouse);

    List<Inventory> findByProduct(PhysicalProduct product);

    List<Inventory> findByWarehouse(Warehouse warehouse);
}
