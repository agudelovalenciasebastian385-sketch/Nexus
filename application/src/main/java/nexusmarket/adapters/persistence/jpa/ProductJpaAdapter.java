package nexusmarket.adapters.persistence.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.mappers.ProductJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaProductRepository;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.valueobjects.ProductStatus;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for Product (physical and digital) backed by Spring Data JPA (MySQL).
 */
@Repository
public class ProductJpaAdapter implements ProductRepositoryPort {

    private final SpringDataJpaProductRepository productRepository;
    private final JpaAggregateLoader loader;

    public ProductJpaAdapter(SpringDataJpaProductRepository productRepository, JpaAggregateLoader loader) {
        this.productRepository = productRepository;
        this.loader = loader;
    }

    @Override
    public Product save(Product product) {
        productRepository.save(ProductJpaMapper.toEntity(product));
        return product;
    }

    @Override
    public void update(Product product) {
        productRepository.save(ProductJpaMapper.toEntity(product));
    }

    @Override
    public Optional<Product> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return productRepository.findById(id).map(loader::toProduct);
    }

    @Override
    public List<Product> findByStatus(ProductStatus status) {
        if (status == null) {
            return List.of();
        }
        Map<String, Seller> sellers = new HashMap<>();
        return productRepository.findByStatus(status.getCode()).stream()
                .map(entity -> ProductJpaMapper.toDomain(entity, sellers.computeIfAbsent(entity.getSellerId(),
                        sellerId -> loader.requireUser(sellerId, Seller.class))))
                .toList();
    }

    @Override
    public List<Product> findBySeller(Seller seller) {
        if (seller == null || seller.getId() == null) {
            return List.of();
        }
        return productRepository.findBySellerId(seller.getId()).stream()
                .map(entity -> ProductJpaMapper.toDomain(entity, seller))
                .toList();
    }
}
