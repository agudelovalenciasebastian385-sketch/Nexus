package nexusmarket.domain.services.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.InventoryRepositoryPort;

/** In-memory InventoryRepositoryPort keyed by (product id, warehouse id), like the composite key in MySQL. */
public class InMemoryInventoryRepository implements InventoryRepositoryPort {

    private final Map<String, Inventory> inventories = new LinkedHashMap<>();

    private static String key(String productId, String warehouseId) {
        return productId + ":" + warehouseId;
    }

    private static String key(Inventory inventory) {
        return key(inventory.getProduct().getId(), inventory.getWarehouse().getId());
    }

    @Override
    public Inventory save(Inventory inventory) {
        inventories.put(key(inventory), inventory);
        return inventory;
    }

    @Override
    public void update(Inventory inventory) {
        inventories.put(key(inventory), inventory);
    }

    @Override
    public Optional<Inventory> findByProductAndWarehouse(PhysicalProduct product, Warehouse warehouse) {
        return Optional.ofNullable(inventories.get(key(product.getId(), warehouse.getId())));
    }

    @Override
    public List<Inventory> findByProduct(PhysicalProduct product) {
        return inventories.values().stream()
                .filter(inventory -> inventory.getProduct().getId().equals(product.getId())).toList();
    }

    @Override
    public List<Inventory> findByWarehouse(Warehouse warehouse) {
        return inventories.values().stream()
                .filter(inventory -> inventory.getWarehouse().getId().equals(warehouse.getId())).toList();
    }

    @Override
    public List<Inventory> findAll() {
        return new ArrayList<>(inventories.values());
    }
}
