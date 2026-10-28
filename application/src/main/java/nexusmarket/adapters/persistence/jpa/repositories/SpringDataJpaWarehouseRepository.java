package nexusmarket.adapters.persistence.jpa.repositories;

import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.WarehouseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Warehouse persistence entity.
 */
public interface SpringDataJpaWarehouseRepository extends JpaRepository<WarehouseJpaEntity, String> {

    List<WarehouseJpaEntity> findBySellerId(String sellerId);
}
