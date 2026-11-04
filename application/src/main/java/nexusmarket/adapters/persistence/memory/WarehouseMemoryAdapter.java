package nexusmarket.adapters.persistence.memory;

import java.util.List;
import java.util.Optional;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Warehouses in memory. */
@Repository
@Profile("memory")
public class WarehouseMemoryAdapter implements WarehouseRepositoryPort {

    private final MemoryStore<String, Warehouse> warehouses = new MemoryStore<>();

    @Override
    public Warehouse save(Warehouse warehouse) {
        warehouses.put(warehouse.getId(), warehouse);
        return warehouse;
    }

    @Override
    public void update(Warehouse warehouse) {
        warehouses.put(warehouse.getId(), warehouse);
    }

    @Override
    public Optional<Warehouse> findById(String id) {
        return warehouses.get(id);
    }

    @Override
    public List<Warehouse> findAll() {
        return warehouses.values();
    }

    @Override
    public List<Warehouse> findBySeller(Seller seller) {
        if (seller == null || seller.getId() == null) {
            return List.of();
        }
        return warehouses.values().stream()
                .filter(warehouse -> warehouse.getSeller() != null
                        && warehouse.getSeller().getId().equals(seller.getId()))
                .toList();
    }
}
