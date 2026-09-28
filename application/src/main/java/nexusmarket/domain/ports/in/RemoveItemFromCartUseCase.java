package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Input port: RemoveItemFromCart use case.
 *
 * <p>Removes a product/variant line from the requesting buyer's cart.</p>
 */
public interface RemoveItemFromCartUseCase {

    Cart removeItemFromCart(User requestingUser, String productId, Variant variant);
}
