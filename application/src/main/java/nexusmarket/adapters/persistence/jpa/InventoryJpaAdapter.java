package nexusmarket.adapters.persistence.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.entities.InventoryJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.InventoryJpaId;
import nexusmarket.adapters.persistence.jpa.mappers.InventoryJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.InventoryMovementJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaInventoryMovementRepository;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaInventoryRepository;
import nexusmarket.domain.models.Inventory;
import nexusmarket.domain.models.InventoryMovement;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.InventoryRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Output persistence adapter for Inventory backed by Spring Data JPA (MySQL).
 *
 * <p>Inventory is keyed by (product_id, warehouse_id). The domain only appends movements, so
 * writing an inventory inserts the movements that are not stored yet. The inventory row carries an
 * optimistic lock version: two concurrent reservations of the same stock cannot both commit.
 */
@Repository
public class InventoryJpaAdapter implements InventoryRepositoryPort {

    private final SpringDataJpaInventoryRepository inventoryRepository;
    private final SpringDataJpaInventoryMovementRepository movementRepository;
    private final JpaAggregateLoader loader;

    public InventoryJpaAdapter(SpringDataJpaInventoryRepository inventoryRepository,
                               SpringDataJpaInventoryMovementRepository movementRepository,
                               JpaAggregateLoader loader) {
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.loader = loader;
    }

    @Override
    @Transactional
    public Inventory save(Inventory inventory) {
        persist(inventory);
        return inventory;
    }

    @Override
    @Transactional
    public void update(Inventory inventory) {
        persist(inventory);
    }

    @Override
    public Optional<Inventory> findByProductAndWarehouse(PhysicalProduct product, Warehouse warehouse) {
        if (product == null || warehouse == null) {
            return Optional.empty();
        }
        return inventoryRepository.findById(new InventoryJpaId(product.getId(), warehouse.getId()))
                .map(entity -> loader.toInventory(entity, product, warehouse));
    }

    @Override
    public List<Inventory> findByProduct(PhysicalProduct product) {
        if (product == null || product.getId() == null) {
            return List.of();
        }
        return inventoryRepository.findByIdProductId(product.getId()).stream()
                .map(entity -> loader.toInventory(entity, product,
                        loader.requireWarehouse(entity.getId().getWarehouseId())))
                .toList();
    }

    @Override
    public List<Inventory> findByWarehouse(Warehouse warehouse) {
        if (warehouse == null || warehouse.getId() == null) {
            return List.of();
        }
        return inventoryRepository.findByIdWarehouseId(warehouse.getId()).stream()
                .map(entity -> loader.toInventory(entity,
                        loader.requirePhysicalProduct(entity.getId().getProductId()), warehouse))
                .toList();
    }

    @Override
    public List<Inventory> findAll() {
        Map<String, PhysicalProduct> products = new HashMap<>();
        Map<String, Warehouse> warehouses = new HashMap<>();
        return inventoryRepository.findAll().stream()
                .map(entity -> toDomain(entity, products, warehouses))
                .toList();
    }

    private Inventory toDomain(InventoryJpaEntity entity, Map<String, PhysicalProduct> products,
                               Map<String, Warehouse> warehouses) {
        PhysicalProduct product = products.computeIfAbsent(entity.getId().getProductId(),
                loader::requirePhysicalProduct);
        Warehouse warehouse = warehouses.computeIfAbsent(entity.getId().getWarehouseId(),
                loader::requireWarehouse);
        return loader.toInventory(entity, product, warehouse);
    }

    /**
     * Writes onto the managed entity loaded earlier in the same transaction (findById returns it from
     * the persistence context, still holding the version read at the start of the use case). The commit
     * then runs UPDATE ... WHERE version = v and fails with an optimistic lock exception if another
     * transaction changed the row meanwhile. A new inventory is persisted with version 0.
     */
    private void persist(Inventory inventory) {
        InventoryJpaEntity entity = inventoryRepository.findById(InventoryJpaMapper.idOf(inventory))
                .map(managed -> InventoryJpaMapper.copyToEntity(inventory, managed))
                .orElseGet(() -> InventoryJpaMapper.toEntity(inventory));
        inventoryRepository.save(entity);
        String productId = entity.getId().getProductId();
        String warehouseId = entity.getId().getWarehouseId();
        List<InventoryMovement> movements = inventory.getMovements();
        long stored = movementRepository.countByProductIdAndWarehouseId(productId, warehouseId);
        for (int index = (int) stored; index < movements.size(); index++) {
            movementRepository.save(InventoryMovementJpaMapper.toEntity(movements.get(index), productId,
                    warehouseId));
        }
    }
}
