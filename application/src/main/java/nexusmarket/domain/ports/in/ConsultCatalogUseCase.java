package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.Product;

/**
 * Input port: ConsultCatalog use case.
 *
 * <p>Retrieves the public catalog: only products in {@code PUBLISHED} status.</p>
 */
public interface ConsultCatalogUseCase {

    List<Product> consultCatalog();

    Product consultCatalogProduct(String productId);
}
