package nexusmarket.adapters.persistence.memory;

import java.util.List;
import java.util.Optional;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.InventoryRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Inventory records in memory, identified by product and warehouse like the composite key in MySQL. */
@Repository
@Profile("memory")
public class InventoryMemoryAdapter implements InventoryRepositoryPort {

    private final MemoryStore<String, Inventory> inventories = new MemoryStore<>();

    private static String key(String productId, String warehouseId) {
        return productId + ":" + warehouseId;
    }

    @Override
    public Inventory save(Inventory inventory) {
        inventories.put(key(inventory.getProduct().getId(), inventory.getWarehouse().getId()), inventory);
        return inventory;
    }

    @Override
    public void update(Inventory inventory) {
        save(inventory);
    }

    @Override
    public Optional<Inventory> findByProductAndWarehouse(PhysicalProduct product, Warehouse warehouse) {
        if (product == null || warehouse == null) {
            return Optional.empty();
        }
        return inventories.get(key(product.getId(), warehouse.getId()));
    }

    @Override
    public List<Inventory> findByProduct(PhysicalProduct product) {
        if (product == null || product.getId() == null) {
            return List.of();
        }
        return inventories.values().stream()
                .filter(inventory -> inventory.getProduct().getId().equals(product.getId())).toList();
    }

    @Override
    public List<Inventory> findByWarehouse(Warehouse warehouse) {
        if (warehouse == null || warehouse.getId() == null) {
            return List.of();
        }
        return inventories.values().stream()
                .filter(inventory -> inventory.getWarehouse().getId().equals(warehouse.getId())).toList();
    }

    @Override
    public List<Inventory> findAll() {
        return inventories.values();
    }
}
