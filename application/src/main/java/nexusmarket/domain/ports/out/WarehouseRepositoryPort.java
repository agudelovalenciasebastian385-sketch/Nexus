package nexusmarket.domain.ports.out;

import java.util.List;
import java.util.Optional;

import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;

/**
 * Output port: Persistence and queries of {@code Warehouse}.
 */
public interface WarehouseRepositoryPort {

    Warehouse save(Warehouse warehouse);

    void update(Warehouse warehouse);

    Optional<Warehouse> findById(String id);

    List<Warehouse> findAll();

    List<Warehouse> findBySeller(Seller seller);
}
