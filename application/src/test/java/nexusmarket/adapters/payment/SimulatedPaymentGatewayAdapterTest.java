package nexusmarket.adapters.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.valueobjects.PaymentResult;
import org.junit.jupiter.api.Test;

class SimulatedPaymentGatewayAdapterTest {

    /** One digital item at 50.00. */
    private final Order order = DomainFixtures.digitalOrder(DomainFixtures.buyer());

    private PaymentResult paymentWithLimit(String limit) {
        return new SimulatedPaymentGatewayAdapter(new BigDecimal(limit)).processPayment(order);
    }

    @Test
    void totalBelowTheLimitIsApproved() {
        assertThat(paymentWithLimit("50.01")).isEqualTo(PaymentResult.APPROVED);
    }

    @Test
    void totalExactlyAtTheLimitIsApproved() {
        assertThat(paymentWithLimit("50.00")).isEqualTo(PaymentResult.APPROVED);
    }

    @Test
    void limitWithAnotherScaleIsComparedByValue() {
        assertThat(paymentWithLimit("50")).isEqualTo(PaymentResult.APPROVED);
    }

    @Test
    void totalAboveTheLimitIsRejected() {
        assertThat(paymentWithLimit("49.99")).isEqualTo(PaymentResult.REJECTED);
    }
}
