package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;

/**
 * Input port: DiscontinueProduct use case.
 *
 * <p>Permanently withdraws a product from the catalog (any status except {@code DISCONTINUED} → {@code DISCONTINUED}).</p>
 */
public interface DiscontinueProductUseCase {

    Product discontinueProduct(User requestingUser, String productId);
}
