package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.User;

/**
 * Input port: ConfirmOrder use case.
 *
 * <p>Converts the requesting buyer's cart into an order in {@code PENDING_PAYMENT}, reserves the inventory of its physical items and empties the cart.</p>
 */
public interface ConfirmOrderUseCase {

    Order confirmOrder(User requestingUser);
}
