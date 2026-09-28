package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.User;

/**
 * Input port: ProcessRefund use case.
 *
 * <p>Processes a pending refund: the amount is restituted to the buyer ({@code PENDING} → {@code PROCESSED}).</p>
 */
public interface ProcessRefundUseCase {

    Refund processRefund(User requestingUser, String refundId);
}
