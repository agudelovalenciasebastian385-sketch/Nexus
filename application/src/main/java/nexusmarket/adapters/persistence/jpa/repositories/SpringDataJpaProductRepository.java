package nexusmarket.adapters.persistence.jpa.repositories;

import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Product persistence entity.
 */
public interface SpringDataJpaProductRepository extends JpaRepository<ProductJpaEntity, String> {

    List<ProductJpaEntity> findByStatus(String status);

    List<ProductJpaEntity> findBySellerId(String sellerId);
}
