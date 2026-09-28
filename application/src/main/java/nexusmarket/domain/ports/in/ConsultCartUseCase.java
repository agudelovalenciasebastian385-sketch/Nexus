package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.User;

/**
 * Input port: ConsultCart use case.
 *
 * <p>Retrieves the cart of a buyer with its items and total.</p>
 */
public interface ConsultCartUseCase {

    Cart consultCart(User requestingUser, String buyerId);
}
