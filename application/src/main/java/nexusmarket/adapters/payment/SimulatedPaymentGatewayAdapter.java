package nexusmarket.adapters.payment;

import java.math.BigDecimal;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.ports.out.PaymentGatewayPort;
import nexusmarket.domain.valueobjects.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simulated payment gateway (no external provider in this phase).
 *
 * <p>Rule: a payment is APPROVED when the order total ({@code Order.totalAmount()}) is less than
 * or equal to {@code app.payment.simulated.approval-limit}, and REJECTED when it is greater. Set
 * a low limit (or order above it) to test the rejection path.
 */
@Component
public class SimulatedPaymentGatewayAdapter implements PaymentGatewayPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulatedPaymentGatewayAdapter.class);

    private final BigDecimal approvalLimit;

    public SimulatedPaymentGatewayAdapter(@Value("${app.payment.simulated.approval-limit}") BigDecimal approvalLimit) {
        this.approvalLimit = approvalLimit;
    }

    @Override
    public PaymentResult processPayment(Order order) {
        BigDecimal total = order.totalAmount();
        PaymentResult result = total.compareTo(approvalLimit) > 0 ? PaymentResult.REJECTED : PaymentResult.APPROVED;
        LOGGER.info("Simulated payment for order {}: total={} limit={} result={}", order.getId(), total,
                approvalLimit, result.getCode());
        return result;
    }
}
