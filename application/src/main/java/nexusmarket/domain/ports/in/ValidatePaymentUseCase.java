package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.User;

/**
 * Input port: ValidatePayment use case.
 *
 * <p>Processes the payment of the buyer's own {@code PENDING_PAYMENT} order through the payment gateway.</p>
 */
public interface ValidatePaymentUseCase {

    Order validatePayment(User requestingUser, String orderId);
}
