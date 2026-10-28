package nexusmarket.adapters.persistence.jpa.repositories;

import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.InventoryJpaEntity;
import nexusmarket.adapters.persistence.jpa.entities.InventoryJpaId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Inventory persistence entity (composite key).
 */
public interface SpringDataJpaInventoryRepository extends JpaRepository<InventoryJpaEntity, InventoryJpaId> {

    List<InventoryJpaEntity> findByIdProductId(String productId);

    List<InventoryJpaEntity> findByIdWarehouseId(String warehouseId);
}
