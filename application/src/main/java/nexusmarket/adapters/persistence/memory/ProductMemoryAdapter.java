package nexusmarket.adapters.persistence.memory;

import java.util.List;
import java.util.Optional;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.valueobjects.ProductStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Products in memory. */
@Repository
@Profile("memory")
public class ProductMemoryAdapter implements ProductRepositoryPort {

    private final MemoryStore<String, Product> products = new MemoryStore<>();

    @Override
    public Product save(Product product) {
        products.put(product.getId(), product);
        return product;
    }

    @Override
    public void update(Product product) {
        products.put(product.getId(), product);
    }

    @Override
    public Optional<Product> findById(String id) {
        return products.get(id);
    }

    @Override
    public List<Product> findByStatus(ProductStatus status) {
        return products.values().stream().filter(product -> product.getStatus() == status).toList();
    }

    @Override
    public List<Product> findBySeller(Seller seller) {
        if (seller == null || seller.getId() == null) {
            return List.of();
        }
        return products.values().stream().filter(product -> product.getSeller().getId().equals(seller.getId())).toList();
    }
}
