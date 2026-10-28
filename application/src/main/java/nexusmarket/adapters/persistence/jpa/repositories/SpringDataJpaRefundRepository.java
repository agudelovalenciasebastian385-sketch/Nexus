package nexusmarket.adapters.persistence.jpa.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.entities.RefundJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Refund persistence entity.
 *
 * <p>The date range uses GreaterThanEqual + LessThan (exclusive upper bound), never Between.
 */
public interface SpringDataJpaRefundRepository extends JpaRepository<RefundJpaEntity, String> {

    Optional<RefundJpaEntity> findByReturnRequestId(String returnRequestId);

    List<RefundJpaEntity> findByProcessedAtGreaterThanEqualAndProcessedAtLessThan(LocalDateTime from,
                                                                                  LocalDateTime toExclusive);
}
