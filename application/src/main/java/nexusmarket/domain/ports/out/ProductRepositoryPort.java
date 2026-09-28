package nexusmarket.domain.ports.out;

import java.util.List;
import java.util.Optional;

import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.valueobjects.ProductStatus;

/**
 * Output port: Persistence and queries of {@code Product} (physical and digital).
 */
public interface ProductRepositoryPort {

    Product save(Product product);

    void update(Product product);

    Optional<Product> findById(String id);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findBySeller(Seller seller);
}
