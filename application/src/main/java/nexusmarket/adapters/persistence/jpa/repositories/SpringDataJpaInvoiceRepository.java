package nexusmarket.adapters.persistence.jpa.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Invoice persistence entity.
 *
 * <p>The date range uses GreaterThanEqual + LessThan (exclusive upper bound), never Between.
 */
public interface SpringDataJpaInvoiceRepository extends JpaRepository<InvoiceJpaEntity, String> {

    Optional<InvoiceJpaEntity> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    List<InvoiceJpaEntity> findByIssuedAtGreaterThanEqualAndIssuedAtLessThan(LocalDateTime from,
                                                                             LocalDateTime toExclusive);
}
