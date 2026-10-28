package nexusmarket.adapters.persistence.jpa.repositories;

import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.OrderItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the OrderItem persistence entity.
 */
public interface SpringDataJpaOrderItemRepository extends JpaRepository<OrderItemJpaEntity, Long> {

    List<OrderItemJpaEntity> findByOrderIdOrderByIdAsc(String orderId);

    boolean existsByOrderId(String orderId);
}
