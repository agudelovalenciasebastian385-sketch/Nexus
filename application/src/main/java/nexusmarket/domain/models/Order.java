package nexusmarket.domain.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import nexusmarket.domain.valueobjects.OrderStatus;

/**
 * Order.
 *
 * <p>Represents the formal commercial commitment derived from confirming a
 * shopping cart. Its lifecycle is the central process of the system.</p>
 *
 * <p><b>Constraint:</b> a finalized order (status {@code DELIVERED}) can never
 * be modified under any circumstance.</p>
 *
 * <p>Relationships: an order belongs to exactly one {@link Buyer}; contains
 * one or more {@link OrderItem} instances; generates exactly one
 * {@link Invoice} once payment is validated; and generates zero or more
 * {@link Shipment} instances (one per origin warehouse, only for physical
 * products).</p>
 */
public class Order {

    private final String id;
    private final Buyer buyer;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final LocalDateTime createdAt;
    private Invoice invoice;
    private final List<Shipment> shipments = new ArrayList<>();

    /** Creates a new confirmed order starting at {@code PENDING_PAYMENT}. */
    public Order(String id, Buyer buyer, List<OrderItem> items, LocalDateTime createdAt) {
        this(id, buyer, items, OrderStatus.PENDING_PAYMENT,
                createdAt == null ? LocalDateTime.now() : createdAt, null, null);
    }

    /** Rehydration constructor used to rebuild a persisted order. */
    public Order(String id, Buyer buyer, List<OrderItem> items, OrderStatus status,
                LocalDateTime createdAt, Invoice invoice, List<Shipment> shipments) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order id cannot be empty.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order contains one or more order items.");
        }
        this.id = id;
        this.buyer = Objects.requireNonNull(buyer, "Order buyer is required.");
        this.items = new ArrayList<>(items);
        this.status = Objects.requireNonNull(status, "Order status is required.");
        this.createdAt = Objects.requireNonNull(createdAt, "Order createdAt is required.");
        if (invoice != null) {
            if (invoice.getOrder() != this) {
                throw new IllegalArgumentException("The invoice does not belong to this order.");
            }
            this.invoice = invoice;
        }
        if (shipments != null) {
            for (Shipment shipment : shipments) {
                addShipment(shipment);
            }
        }
    }

    public String getId() {
        return id;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public List<Shipment> getShipments() {
        return Collections.unmodifiableList(shipments);
    }

    private void requireModifiable() {
        if (status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order " + id + " is finalized and can never be modified.");
        }
    }

    public void markPaid() {
        requireModifiable();
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Only a pending payment order can be marked as paid.");
        }
        status = OrderStatus.PAID;
    }

    public void markShipped() {
        requireModifiable();
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Only a paid order can be marked as shipped.");
        }
        status = OrderStatus.SHIPPED;
    }

    public void markDelivered() {
        requireModifiable();
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only a shipped order can be marked as delivered.");
        }
        status = OrderStatus.DELIVERED;
    }

    /** Attaches the unique invoice of the order, generated once payment is validated. */
    public void attachInvoice(Invoice invoice) {
        requireModifiable();
        Objects.requireNonNull(invoice, "Invoice is required.");
        if (this.invoice != null) {
            throw new IllegalStateException(
                    "Order " + id + " already has an invoice. An order generates exactly one invoice.");
        }
        if (invoice.getOrder() != this) {
            throw new IllegalArgumentException("The invoice does not belong to this order.");
        }
        this.invoice = invoice;
    }

    /** Registers a shipment of the order (one per origin warehouse). */
    public void addShipment(Shipment shipment) {
        requireModifiable();
        Objects.requireNonNull(shipment, "Shipment is required.");
        if (shipment.getOrder() != this) {
            throw new IllegalArgumentException("The shipment does not belong to this order.");
        }
        shipments.add(shipment);
    }

    /** Sum of all order item subtotals. */
    public BigDecimal totalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.subtotal());
        }
        return total;
    }

    public boolean isFinalized() {
        return status == OrderStatus.DELIVERED;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Order)) {
            return false;
        }
        return id.equals(((Order) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Order{id='" + id + "', buyer=" + buyer.getId() + ", status=" + status
                + ", items=" + items.size() + ", shipments=" + shipments.size()
                + ", invoiced=" + (invoice != null) + "}";
    }
}
