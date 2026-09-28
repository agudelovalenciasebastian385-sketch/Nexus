package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.User;

/**
 * Input port: ClearCart use case.
 *
 * <p>Removes every item from the requesting buyer's cart.</p>
 */
public interface ClearCartUseCase {

    Cart clearCart(User requestingUser);
}
