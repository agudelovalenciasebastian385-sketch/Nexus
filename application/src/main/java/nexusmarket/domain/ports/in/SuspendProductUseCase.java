package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;

/**
 * Input port: SuspendProduct use case.
 *
 * <p>Temporarily removes a product from the catalog ({@code PUBLISHED} → {@code SUSPENDED}).</p>
 */
public interface SuspendProductUseCase {

    Product suspendProduct(User requestingUser, String productId);
}
