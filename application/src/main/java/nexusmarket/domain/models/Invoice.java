package nexusmarket.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Invoice.
 *
 * <p>Represents the commercial information associated with the sale of an
 * order, generated once payment is validated. An invoice belongs to exactly
 * one {@link Order}.</p>
 */
public class Invoice {

    private final String id;
    private final Order order;
    private final BigDecimal totalAmount;
    private final LocalDateTime issuedAt;

    public Invoice(String id, Order order, BigDecimal totalAmount, LocalDateTime issuedAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invoice id cannot be empty.");
        }
        if (totalAmount == null || totalAmount.signum() < 0) {
            throw new IllegalArgumentException("Invoice total amount must be zero or positive.");
        }
        this.id = id;
        this.order = Objects.requireNonNull(order, "Invoice order is required.");
        this.totalAmount = totalAmount;
        this.issuedAt = Objects.requireNonNull(issuedAt, "Invoice issuedAt is required.");
    }

    /** Creates the invoice computing the total from the order items. */
    public Invoice(String id, Order order, LocalDateTime issuedAt) {
        this(id, order, order.totalAmount(), issuedAt);
    }

    public String getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Invoice)) {
            return false;
        }
        return id.equals(((Invoice) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Invoice{id='" + id + "', order=" + order.getId() + ", totalAmount=" + totalAmount
                + ", issuedAt=" + issuedAt + "}";
    }
}
