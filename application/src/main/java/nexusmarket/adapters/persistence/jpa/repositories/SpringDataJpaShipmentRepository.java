package nexusmarket.adapters.persistence.jpa.repositories;

import java.time.LocalDateTime;
import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.ShipmentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Shipment persistence entity.
 *
 * <p>The date range uses GreaterThanEqual + LessThan (exclusive upper bound), never Between.
 */
public interface SpringDataJpaShipmentRepository extends JpaRepository<ShipmentJpaEntity, String> {

    List<ShipmentJpaEntity> findByOrderIdOrderByCreatedAtAsc(String orderId);

    List<ShipmentJpaEntity> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from,
                                                                                LocalDateTime toExclusive);
}
