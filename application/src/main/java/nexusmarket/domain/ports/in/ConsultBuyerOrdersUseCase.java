package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.User;

/**
 * Input port: ConsultBuyerOrders use case.
 *
 * <p>Retrieves the historical and active orders of a buyer.</p>
 */
public interface ConsultBuyerOrdersUseCase {

    List<Order> consultBuyerOrders(User requestingUser, String buyerId);
}
