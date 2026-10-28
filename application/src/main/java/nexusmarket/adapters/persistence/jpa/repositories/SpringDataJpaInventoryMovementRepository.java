package nexusmarket.adapters.persistence.jpa.repositories;

import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.InventoryMovementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the InventoryMovement persistence entity.
 */
public interface SpringDataJpaInventoryMovementRepository extends JpaRepository<InventoryMovementJpaEntity, Long> {

    List<InventoryMovementJpaEntity> findByProductIdAndWarehouseIdOrderByIdAsc(String productId, String warehouseId);

    long countByProductIdAndWarehouseId(String productId, String warehouseId);
}
