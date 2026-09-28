package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.User;

/**
 * Input port: ConsultOrder use case.
 *
 * <p>Retrieves an order.</p>
 */
public interface ConsultOrderUseCase {

    Order consultOrder(User requestingUser, String orderId);
}
