package nexusmarket.domain.ports.out;

import nexusmarket.domain.models.Order;
import nexusmarket.domain.valueobjects.PaymentResult;

/**
 * Output port: Processing of order payments through an external payment gateway (simulated by an adapter in this phase).
 */
public interface PaymentGatewayPort {

    PaymentResult processPayment(Order order);
}
