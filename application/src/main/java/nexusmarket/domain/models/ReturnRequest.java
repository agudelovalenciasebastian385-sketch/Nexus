package nexusmarket.domain.models;

import nexusmarket.domain.exceptions.InvalidReturnRequestException;
import nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import nexusmarket.domain.valueobjects.ReturnStatus;

/**
 * ReturnRequest (named {@code Return} in the Domain Model document).
 *
 * <p>Represents a request to return one or more products from an already
 * delivered order. A return belongs to exactly one {@link Order} and may give
 * rise to a {@link Refund}. Once approved, it generates an inventory movement
 * of type {@code RETURN} on the corresponding inventory record (physical
 * products only).</p>
 */
public class ReturnRequest {

    private final String id;
    private final Order order;
    private final String reason;
    private ReturnStatus status;

    /** Creates a new return request starting at {@code REQUESTED}. */
    public ReturnRequest(String id, Order order, String reason) {
        this(id, order, reason, ReturnStatus.REQUESTED);
    }

    /** Rehydration constructor used to rebuild a persisted return request. */
    public ReturnRequest(String id, Order order, String reason, ReturnStatus status) {
        if (id == null || id.isBlank()) {
            throw new InvalidReturnRequestException("ReturnRequest id cannot be empty.");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidReturnRequestException("ReturnRequest reason cannot be empty.");
        }
        this.id = id;
        if (order == null) {
            throw new InvalidReturnRequestException("ReturnRequest order is required.");
        }
        this.order = order;
        this.reason = reason;
        if (status == null) {
            throw new InvalidReturnRequestException("ReturnRequest status is required.");
        }
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public String getReason() {
        return reason;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    /** The return was validated and triggers inventory re-entry. */
    public void approve() {
        if (status != ReturnStatus.REQUESTED) {
            throw new InvalidStatusTransitionException("return request", status.getCode(),
                    ReturnStatus.APPROVED.getCode());
        }
        status = ReturnStatus.APPROVED;
    }

    /** The return did not meet the required conditions. */
    public void reject() {
        if (status != ReturnStatus.REQUESTED) {
            throw new InvalidStatusTransitionException("return request", status.getCode(),
                    ReturnStatus.REJECTED.getCode());
        }
        status = ReturnStatus.REJECTED;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ReturnRequest)) {
            return false;
        }
        return id.equals(((ReturnRequest) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ReturnRequest{id='" + id + "', order=" + order.getId() + ", status=" + status + "}";
    }
}
