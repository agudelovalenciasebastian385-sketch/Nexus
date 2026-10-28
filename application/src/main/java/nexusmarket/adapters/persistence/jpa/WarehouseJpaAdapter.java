package nexusmarket.adapters.persistence.jpa;

import java.util.List;
import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.mappers.WarehouseJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaWarehouseRepository;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.ports.out.WarehouseRepositoryPort;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for Warehouse backed by Spring Data JPA (MySQL).
 */
@Repository
public class WarehouseJpaAdapter implements WarehouseRepositoryPort {

    private final SpringDataJpaWarehouseRepository warehouseRepository;
    private final JpaAggregateLoader loader;

    public WarehouseJpaAdapter(SpringDataJpaWarehouseRepository warehouseRepository, JpaAggregateLoader loader) {
        this.warehouseRepository = warehouseRepository;
        this.loader = loader;
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        warehouseRepository.save(WarehouseJpaMapper.toEntity(warehouse));
        return warehouse;
    }

    @Override
    public void update(Warehouse warehouse) {
        warehouseRepository.save(WarehouseJpaMapper.toEntity(warehouse));
    }

    @Override
    public Optional<Warehouse> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return warehouseRepository.findById(id).map(loader::toWarehouse);
    }

    @Override
    public List<Warehouse> findAll() {
        return warehouseRepository.findAll().stream()
                .map(loader::toWarehouse)
                .toList();
    }

    @Override
    public List<Warehouse> findBySeller(Seller seller) {
        if (seller == null || seller.getId() == null) {
            return List.of();
        }
        return warehouseRepository.findBySellerId(seller.getId()).stream()
                .map(entity -> WarehouseJpaMapper.toDomain(entity, seller))
                .toList();
    }
}
