package nexusmarket.domain.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;

/**
 * Buyer.
 *
 * <p>Represents a user who makes purchases within the Marketplace. Holds the
 * information required to participate in commercial processes.</p>
 *
 * <p>Key constraint: a Buyer never manages information belonging to other
 * buyers or to inventory.</p>
 */
public class Buyer extends User {

    /** Usual delivery location (modeled as String in this delivery). */
    private String primaryAddress;

    /** Secondary delivery locations. Empty by default. */
    private final List<String> additionalAddresses = new ArrayList<>();

    /** Buyer's condition for making purchases. */
    private CommercialStatus commercialStatus;

    /** Buyer's historical and active orders. Empty by default; loaded on demand. */
    private final List<Order> orders = new ArrayList<>();

    public Buyer(String id, String fullName, String email, UserStatus status,
                String primaryAddress, CommercialStatus commercialStatus) {
        super(id, fullName, email, SystemRole.BUYER, status);
        this.primaryAddress = requireAddress(primaryAddress, "Buyer primary address cannot be empty.");
        this.commercialStatus = Objects.requireNonNull(commercialStatus, "Buyer commercial status is required.");
    }

    /** Rehydration constructor used to rebuild a persisted buyer. */
    public Buyer(String id, String fullName, String email, UserStatus status,
                String primaryAddress, List<String> additionalAddresses,
                CommercialStatus commercialStatus, List<Order> orders) {
        this(id, fullName, email, status, primaryAddress, commercialStatus);
        if (additionalAddresses != null) {
            for (String address : additionalAddresses) {
                addAdditionalAddress(address);
            }
        }
        if (orders != null) {
            for (Order order : orders) {
                addOrder(order);
            }
        }
    }

    public String getPrimaryAddress() {
        return primaryAddress;
    }

    public void setPrimaryAddress(String primaryAddress) {
        this.primaryAddress = requireAddress(primaryAddress, "Buyer primary address cannot be empty.");
    }

    public List<String> getAdditionalAddresses() {
        return Collections.unmodifiableList(additionalAddresses);
    }

    public void addAdditionalAddress(String address) {
        String value = requireAddress(address, "Buyer additional address cannot be empty.");
        if (additionalAddresses.contains(value)) {
            throw new IllegalArgumentException("The additional address is already registered for this buyer.");
        }
        additionalAddresses.add(value);
    }

    public CommercialStatus getCommercialStatus() {
        return commercialStatus;
    }

    public void setCommercialStatus(CommercialStatus commercialStatus) {
        this.commercialStatus = Objects.requireNonNull(commercialStatus, "Buyer commercial status is required.");
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    /** Registers an order placed by this buyer. */
    public void addOrder(Order order) {
        Objects.requireNonNull(order, "Order is required.");
        if (order.getBuyer() != this) {
            throw new IllegalArgumentException("The order does not belong to this buyer.");
        }
        orders.add(order);
    }

    /** Indicates whether the buyer can currently make purchases. */
    public boolean canPurchase() {
        return isActive() && commercialStatus == CommercialStatus.ENABLED;
    }

    private String requireAddress(String address, String message) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return address;
    }

    @Override
    public String toString() {
        return "Buyer{id='" + getId() + "', fullName='" + getFullName() + "', email='" + getEmail()
                + "', commercialStatus=" + commercialStatus + ", orders=" + orders.size() + "}";
    }
}
