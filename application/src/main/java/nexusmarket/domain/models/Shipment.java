package nexusmarket.domain.models;

import nexusmarket.domain.exceptions.InvalidShipmentException;
import nexusmarket.domain.exceptions.InvalidStatusTransitionException;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.ShipmentStatus;

/**
 * Shipment.
 *
 * <p>Represents the logistics process of packing, dispatching, and
 * transporting the physical products of an order. A shipment belongs to
 * exactly one {@link Order} and exactly one origin {@link Warehouse}, and is
 * handled by exactly one {@link LogisticsOperator}.</p>
 */
public class Shipment {

    private final String id;
    private final Order order;
    private final Warehouse originWarehouse;
    /** Delivery address, taken from the order. */
    private final Address deliveryAddress;
    private final LogisticsOperator logisticsOperator;
    private ShipmentStatus status;

    /** Creates a new shipment starting at {@code PREPARING}. */
    public Shipment(String id, Order order, Warehouse originWarehouse,
                    Address deliveryAddress, LogisticsOperator logisticsOperator) {
        this(id, order, originWarehouse, deliveryAddress, logisticsOperator, ShipmentStatus.PREPARING);
    }

    /** Rehydration constructor used to rebuild a persisted shipment. */
    public Shipment(String id, Order order, Warehouse originWarehouse,
                    Address deliveryAddress, LogisticsOperator logisticsOperator, ShipmentStatus status) {
        if (id == null || id.isBlank()) {
            throw new InvalidShipmentException("Shipment id cannot be empty.");
        }
        if (deliveryAddress == null) {
            throw new InvalidShipmentException("Shipment delivery address is required.");
        }
        this.id = id;
        if (order == null) {
            throw new InvalidShipmentException("Shipment order is required.");
        }
        this.order = order;
        if (originWarehouse == null) {
            throw new InvalidShipmentException("Shipment origin warehouse is required.");
        }
        this.originWarehouse = originWarehouse;
        this.deliveryAddress = deliveryAddress;
        if (logisticsOperator == null) {
            throw new InvalidShipmentException("Shipment logistics operator is required.");
        }
        this.logisticsOperator = logisticsOperator;
        if (status == null) {
            throw new InvalidShipmentException("Shipment status is required.");
        }
        this.status = status;
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

    public Address getDeliveryAddress() {
        return deliveryAddress;
    }

    public LogisticsOperator getLogisticsOperator() {
        return logisticsOperator;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    /** The shipment has been packed at the origin warehouse. */
    public void markPacked() {
        if (status != ShipmentStatus.PREPARING) {
            throw new InvalidStatusTransitionException("shipment", status.getCode(),
                    ShipmentStatus.PACKED.getCode());
        }
        status = ShipmentStatus.PACKED;
    }

    /** The shipment leaves the origin warehouse. */
    public void markInTransit() {
        if (status != ShipmentStatus.PACKED) {
            throw new InvalidStatusTransitionException("shipment", status.getCode(),
                    ShipmentStatus.IN_TRANSIT.getCode());
        }
        status = ShipmentStatus.IN_TRANSIT;
    }

    /** The shipment was successfully delivered. */
    public void markDelivered() {
        if (status != ShipmentStatus.IN_TRANSIT) {
            throw new InvalidStatusTransitionException("shipment", status.getCode(),
                    ShipmentStatus.DELIVERED.getCode());
        }
        status = ShipmentStatus.DELIVERED;
    }

    public boolean isDelivered() {
        return status == ShipmentStatus.DELIVERED;
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
