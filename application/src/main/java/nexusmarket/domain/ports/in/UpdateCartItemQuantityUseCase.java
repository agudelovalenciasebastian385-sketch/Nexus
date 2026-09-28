package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Input port: UpdateCartItemQuantity use case.
 *
 * <p>Replaces the quantity of a product/variant line of the requesting buyer's cart.</p>
 */
public interface UpdateCartItemQuantityUseCase {

    Cart updateCartItemQuantity(User requestingUser, String productId, Variant variant, int quantity);
}
