package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.NOW;
import static nexusmarket.domain.DomainFixtures.physicalOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import nexusmarket.domain.exceptions.InvalidRefundException;
import nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import nexusmarket.domain.valueobjects.OrderStatus;
import nexusmarket.domain.valueobjects.RefundStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RefundTest {

    private static final BigDecimal AMOUNT = new BigDecimal("200.00");

    private final ReturnRequest returnRequest = new ReturnRequest("return-1", physicalOrder(OrderStatus.DELIVERED),
            "Wrong color");

    private Refund pending() {
        return new Refund("refund-1", returnRequest, AMOUNT);
    }

    @Test
    void newRefundIsPendingWithoutProcessedAt() {
        Refund refund = pending();

        assertThat(refund.getStatus()).isEqualTo(RefundStatus.PENDING);
        assertThat(refund.getProcessedAt()).isNull();
    }

    @Test
    void processingStoresTheProcessedAtDate() {
        Refund refund = pending();

        refund.markProcessed(NOW);

        assertThat(refund.getStatus()).isEqualTo(RefundStatus.PROCESSED);
        assertThat(refund.getProcessedAt()).isEqualTo(NOW);
    }

    @Test
    void processingWithoutDateIsRejected() {
        Refund refund = pending();

        assertThatThrownBy(() -> refund.markProcessed(null)).isInstanceOf(InvalidRefundException.class);
    }

    @Test
    void processingTwiceIsRejected() {
        Refund refund = pending();
        refund.markProcessed(NOW);

        assertThatThrownBy(() -> refund.markProcessed(NOW)).isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void processingARejectedRefundIsRejected() {
        Refund refund = pending();
        refund.reject();

        assertThatThrownBy(() -> refund.markProcessed(NOW)).isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void rehydratedProcessedRefundWithoutDateIsRejected() {
        assertThatThrownBy(() -> new Refund("refund-1", returnRequest, AMOUNT, RefundStatus.PROCESSED, null))
                .isInstanceOf(InvalidRefundException.class);
    }

    @Test
    void rehydratedPendingRefundWithDateIsRejected() {
        assertThatThrownBy(() -> new Refund("refund-1", returnRequest, AMOUNT, RefundStatus.PENDING, NOW))
                .isInstanceOf(InvalidRefundException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void nonPositiveAmountIsRejected(String amount) {
        assertThatThrownBy(() -> new Refund("refund-1", returnRequest, new BigDecimal(amount)))
                .isInstanceOf(InvalidRefundException.class);
    }
}
