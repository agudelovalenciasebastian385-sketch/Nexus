package nexusmarket.adapters.persistence.jpa.repositories;

import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.ReturnRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the ReturnRequest persistence entity.
 */
public interface SpringDataJpaReturnRequestRepository extends JpaRepository<ReturnRequestJpaEntity, String> {

    List<ReturnRequestJpaEntity> findByOrderId(String orderId);
}
