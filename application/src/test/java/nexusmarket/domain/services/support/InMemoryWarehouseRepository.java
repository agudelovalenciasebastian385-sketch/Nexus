package nexusmarket.domain.services.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;

/** In-memory WarehouseRepositoryPort. */
public class InMemoryWarehouseRepository implements WarehouseRepositoryPort {

    private final Map<String, Warehouse> warehouses = new LinkedHashMap<>();

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
        return Optional.ofNullable(warehouses.get(id));
    }

    @Override
    public List<Warehouse> findAll() {
        return new ArrayList<>(warehouses.values());
    }

    @Override
    public List<Warehouse> findBySeller(Seller seller) {
        return warehouses.values().stream()
                .filter(warehouse -> warehouse.getSeller() != null && warehouse.getSeller().getId().equals(seller.getId()))
                .toList();
    }
}
