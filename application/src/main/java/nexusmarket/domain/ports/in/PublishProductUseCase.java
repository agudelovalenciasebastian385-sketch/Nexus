package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;

/**
 * Input port: PublishProduct use case.
 *
 * <p>Makes a product visible in the public catalog ({@code DRAFT} or {@code SUSPENDED} → {@code PUBLISHED}).</p>
 */
public interface PublishProductUseCase {

    Product publishProduct(User requestingUser, String productId);
}
