package nexusmarket.domain.models;

import java.math.BigDecimal;
import java.util.Objects;

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

    /** Creates a new refund starting at {@code PENDING}. */
    public Refund(String id, ReturnRequest returnRequest, BigDecimal amount) {
        this(id, returnRequest, amount, RefundStatus.PENDING);
    }

    /** Rehydration constructor used to rebuild a persisted refund. */
    public Refund(String id, ReturnRequest returnRequest, BigDecimal amount, RefundStatus status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Refund id cannot be empty.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Refund amount must be positive.");
        }
        this.id = id;
        this.returnRequest = Objects.requireNonNull(returnRequest, "Refund return request is required.");
        this.amount = amount;
        this.status = Objects.requireNonNull(status, "Refund status is required.");
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

    /** The amount was restituted to the buyer. */
    public void markProcessed() {
        if (status != RefundStatus.PENDING) {
            throw new IllegalStateException("Only a pending refund can be processed.");
        }
        status = RefundStatus.PROCESSED;
    }

    /** The refund did not proceed. */
    public void reject() {
        if (status != RefundStatus.PENDING) {
            throw new IllegalStateException("Only a pending refund can be rejected.");
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
