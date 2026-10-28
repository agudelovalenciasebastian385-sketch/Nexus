package nexusmarket.adapters.persistence.jpa.repositories;

import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.CartItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the CartItem persistence entity.
 */
public interface SpringDataJpaCartItemRepository extends JpaRepository<CartItemJpaEntity, Long> {

    List<CartItemJpaEntity> findByCartIdOrderByIdAsc(String cartId);

    void deleteByCartId(String cartId);
}
