package nexusmarket.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import nexusmarket.domain.exceptions.InvalidRefundException;
import nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import nexusmarket.domain.valueobjects.RefundStatus;

/**
 * Refund.
 *
 * <p>Represents the financial restitution associated with an approved return.
 * A refund belongs to exactly one {@link ReturnRequest}.</p>
 */
public class Refund {

    private final String id;
    private final ReturnRequest returnRequest;
    private final BigDecimal amount;
    private RefundStatus status;
    /** Moment the amount was restituted; {@code null} unless the refund is {@code PROCESSED}. */
    private LocalDateTime processedAt;

    /** Creates a new refund starting at {@code PENDING}. */
    public Refund(String id, ReturnRequest returnRequest, BigDecimal amount) {
        this(id, returnRequest, amount, RefundStatus.PENDING, null);
    }

    /** Rehydration constructor used to rebuild a persisted refund. */
    public Refund(String id, ReturnRequest returnRequest, BigDecimal amount, RefundStatus status,
                  LocalDateTime processedAt) {
        if (id == null || id.isBlank()) {
            throw new InvalidRefundException("Refund id cannot be empty.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidRefundException("Refund amount must be positive.");
        }
        this.id = id;
        if (returnRequest == null) {
            throw new InvalidRefundException("Refund return request is required.");
        }
        this.returnRequest = returnRequest;
        this.amount = amount;
        if (status == null) {
            throw new InvalidRefundException("Refund status is required.");
        }
        this.status = status;
        if (status == RefundStatus.PROCESSED && processedAt == null) {
            throw new InvalidRefundException("A processed refund requires its processedAt date.");
        }
        if (status != RefundStatus.PROCESSED && processedAt != null) {
            throw new InvalidRefundException("Only a processed refund has a processedAt date.");
        }
        this.processedAt = processedAt;
    }

    public String getId() {
        return id;
    }

    public ReturnRequest getReturnRequest() {
        return returnRequest;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    /** The amount was restituted to the buyer at the given moment. */
    public void markProcessed(LocalDateTime processedAt) {
        if (status != RefundStatus.PENDING) {
            throw new InvalidStatusTransitionException("refund", status.getCode(),
                    RefundStatus.PROCESSED.getCode());
        }
        if (processedAt == null) {
            throw new InvalidRefundException("Refund processedAt is required.");
        }
        status = RefundStatus.PROCESSED;
        this.processedAt = processedAt;
    }

    /** The refund did not proceed. */
    public void reject() {
        if (status != RefundStatus.PENDING) {
            throw new InvalidStatusTransitionException("refund", status.getCode(),
                    RefundStatus.REJECTED.getCode());
        }
        status = RefundStatus.REJECTED;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Refund)) {
            return false;
        }
        return id.equals(((Refund) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Refund{id='" + id + "', returnRequest=" + returnRequest.getId()
                + ", amount=" + amount + ", status=" + status + "}";
    }
}
