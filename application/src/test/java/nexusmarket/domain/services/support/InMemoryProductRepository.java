package nexusmarket.domain.services.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.valueobjects.ProductStatus;

/** In-memory ProductRepositoryPort. */
public class InMemoryProductRepository implements ProductRepositoryPort {

    private final Map<String, Product> products = new LinkedHashMap<>();

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
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> findByStatus(ProductStatus status) {
        return products.values().stream().filter(product -> product.getStatus() == status).toList();
    }

    @Override
    public List<Product> findBySeller(Seller seller) {
        return products.values().stream().filter(product -> product.getSeller().getId().equals(seller.getId())).toList();
    }
}
