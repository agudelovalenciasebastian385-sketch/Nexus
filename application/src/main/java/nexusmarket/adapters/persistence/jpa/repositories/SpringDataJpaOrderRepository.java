package nexusmarket.adapters.persistence.jpa.repositories;

import java.time.LocalDateTime;
import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Order persistence entity.
 *
 * <p>The date range uses GreaterThanEqual + LessThan (exclusive upper bound), never Between.
 */
public interface SpringDataJpaOrderRepository extends JpaRepository<OrderJpaEntity, String> {

    List<OrderJpaEntity> findByBuyerIdOrderByCreatedAtAsc(String buyerId);

    List<OrderJpaEntity> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime from,
                                                                             LocalDateTime toExclusive);
}
