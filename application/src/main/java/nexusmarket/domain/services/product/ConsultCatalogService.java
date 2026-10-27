package nexusmarket.domain.services.product;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.valueobjects.ProductStatus;
import org.springframework.stereotype.Service;

/**
 * Retrieves the public catalog: only products in {@code PUBLISHED} status
 * (product-services.md - ConsultCatalog). Public operation.
 */
@Service
@RequiredArgsConstructor
public class ConsultCatalogService {

    private final ProductRepositoryPort productRepositoryPort;

    public List<Product> consultCatalog() {
        return productRepositoryPort.findByStatus(ProductStatus.PUBLISHED);
    }

    public Product consultCatalogProduct(String productId) {
        return productRepositoryPort.findById(productId)
                .filter(Product::isPublished)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
    }
}
