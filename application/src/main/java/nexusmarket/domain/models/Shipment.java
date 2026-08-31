package nexusmarket.domain.models;

import java.util.Objects;

import nexusmarket.domain.valueobjects.ShipmentStatus;
import nexusmarket.domain.valueobjects.SystemRole;

/**
 * Shipment.
 *
 * <p>Represents the logistics process of packing, dispatching, and
 * transporting the physical products of an order. A shipment belongs to
 * exactly one {@link Order} and exactly one origin {@link Warehouse}, and is
 * handled by exactly one logistics operator.</p>
 *
 * <p>Because the {@code LogisticsOperator} class is out of the scope of this
 * delivery, the operator is modeled as a {@link User} whose role must be
 * {@link SystemRole#LOGISTICS_OPERATOR} (validated by this class).</p>
 */
public class Shipment {

    private final String id;
    private final Order order;
    private final Warehouse originWarehouse;
    /** Delivery address, taken from the order (modeled as String in this delivery). */
    private final String deliveryAddress;
    private final User logisticsOperator;
    private ShipmentStatus status;

    /** Creates a new shipment starting at {@code PREPARING}. */
    public Shipment(String id, Order order, Warehouse originWarehouse,
                    String deliveryAddress, User logisticsOperator) {
        this(id, order, originWarehouse, deliveryAddress, logisticsOperator, ShipmentStatus.PREPARING);
    }

    /** Rehydration constructor used to rebuild a persisted shipment. */
    public Shipment(String id, Order order, Warehouse originWarehouse,
                    String deliveryAddress, User logisticsOperator, ShipmentStatus status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Shipment id cannot be empty.");
        }
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            throw new IllegalArgumentException("Shipment delivery address cannot be empty.");
        }
        Objects.requireNonNull(logisticsOperator, "Shipment logistics operator is required.");
        if (logisticsOperator.getRole() != SystemRole.LOGISTICS_OPERATOR) {
            throw new IllegalArgumentException(
                    "The shipment must be handled by a user with the " + SystemRole.LOGISTICS_OPERATOR + " role.");
        }
        this.id = id;
        this.order = Objects.requireNonNull(order, "Shipment order is required.");
        this.originWarehouse = Objects.requireNonNull(originWarehouse, "Shipment origin warehouse is required.");
        this.deliveryAddress = deliveryAddress;
        this.logisticsOperator = logisticsOperator;
        this.status = Objects.requireNonNull(status, "Shipment status is required.");
    }

    public String getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Warehouse getOriginWarehouse() {
        return originWarehouse;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public User getLogisticsOperator() {
        return logisticsOperator;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    /** The shipment leaves the origin warehouse. */
    public void markInTransit() {
        if (status != ShipmentStatus.PREPARING) {
            throw new IllegalStateException("Only a preparing shipment can be sent in transit.");
        }
        status = ShipmentStatus.IN_TRANSIT;
    }

    /** The shipment was successfully delivered. */
    public void markDelivered() {
        if (status != ShipmentStatus.IN_TRANSIT) {
            throw new IllegalStateException("Only an in-transit shipment can be marked as delivered.");
        }
        status = ShipmentStatus.DELIVERED;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Shipment)) {
            return false;
        }
        return id.equals(((Shipment) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Shipment{id='" + id + "', order=" + order.getId() + ", originWarehouse=" + originWarehouse.getId()
                + ", operator=" + logisticsOperator.getId() + ", status=" + status + "}";
    }
}
