package nexusmarket.domain.services.support;

import java.util.ArrayList;
import java.util.List;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.ports.out.PaymentGatewayPort;
import nexusmarket.domain.valueobjects.PaymentResult;

/** Configurable PaymentGatewayPort: approves by default; {@link #rejectNextPayments()} makes it reject. */
public class FakePaymentGateway implements PaymentGatewayPort {

    private PaymentResult result = PaymentResult.APPROVED;
    private final List<String> processedOrders = new ArrayList<>();

    public void rejectNextPayments() {
        result = PaymentResult.REJECTED;
    }

    public List<String> processedOrders() {
        return List.copyOf(processedOrders);
    }

    @Override
    public PaymentResult processPayment(Order order) {
        processedOrders.add(order.getId());
        return result;
    }
}
