package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Input port: AddItemToCart use case.
 *
 * <p>Adds a published product to the requesting buyer's cart at its current price.</p>
 */
public interface AddItemToCartUseCase {

    Cart addItemToCart(User requestingUser, String productId, Variant variant, int quantity);
}
