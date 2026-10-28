package nexusmarket.adapters.persistence.jpa.repositories;

import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.entities.CartJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Cart persistence entity.
 */
public interface SpringDataJpaCartRepository extends JpaRepository<CartJpaEntity, String> {

    Optional<CartJpaEntity> findByBuyerId(String buyerId);
}
