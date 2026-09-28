package nexusmarket.domain.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nexusmarket.domain.exceptions.InvalidCartException;
import nexusmarket.domain.exceptions.InvalidOrderException;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.valueobjects.Address;
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

    /** Usual delivery location. */
    private Address primaryAddress;

    /** Secondary delivery locations. Empty by default. */
    private final List<Address> additionalAddresses = new ArrayList<>();

    /** Buyer's condition for making purchases. */
    private CommercialStatus commercialStatus;

    /** Buyer's active shopping cart. A buyer owns exactly one cart. */
    private Cart cart;

    /** Buyer's historical and active orders. Empty by default; loaded on demand. */
    private final List<Order> orders = new ArrayList<>();

    public Buyer(String id, String identificationNumber, String fullName, String email, String password,
                UserStatus status, Address primaryAddress, CommercialStatus commercialStatus) {
        super(id, identificationNumber, fullName, email, password, SystemRole.BUYER, status);
        this.primaryAddress = requireAddress(primaryAddress, "Buyer primary address is required.");
        if (commercialStatus == null) {
            throw new InvalidUserException("Buyer commercial status is required.");
        }
        this.commercialStatus = commercialStatus;
    }

    /** Rehydration constructor used to rebuild a persisted buyer. */
    public Buyer(String id, String identificationNumber, String fullName, String email, String password,
                UserStatus status, Address primaryAddress, List<Address> additionalAddresses,
                CommercialStatus commercialStatus, List<Order> orders) {
        this(id, identificationNumber, fullName, email, password, status, primaryAddress, commercialStatus);
        if (additionalAddresses != null) {
            for (Address address : additionalAddresses) {
                addAdditionalAddress(address);
            }
        }
        if (orders != null) {
            for (Order order : orders) {
                addOrder(order);
            }
        }
    }

    public Address getPrimaryAddress() {
        return primaryAddress;
    }

    public void setPrimaryAddress(Address primaryAddress) {
        this.primaryAddress = requireAddress(primaryAddress, "Buyer primary address is required.");
    }

    public List<Address> getAdditionalAddresses() {
        return Collections.unmodifiableList(additionalAddresses);
    }

    public void addAdditionalAddress(Address address) {
        Address value = requireAddress(address, "Buyer additional address is required.");
        if (additionalAddresses.contains(value)) {
            throw new InvalidUserException("The additional address is already registered for this buyer.");
        }
        additionalAddresses.add(value);
    }

    /** Removes a registered secondary delivery location. */
    public void removeAdditionalAddress(Address address) {
        if (!additionalAddresses.remove(address)) {
            throw new InvalidUserException("The additional address is not registered for this buyer.");
        }
    }

    public CommercialStatus getCommercialStatus() {
        return commercialStatus;
    }

    public void setCommercialStatus(CommercialStatus commercialStatus) {
        if (commercialStatus == null) {
            throw new InvalidUserException("Buyer commercial status is required.");
        }
        this.commercialStatus = commercialStatus;
    }

    public Cart getCart() {
        return cart;
    }

    /** Assigns the buyer's unique shopping cart. */
    public void assignCart(Cart cart) {
        if (cart == null) {
            throw new InvalidCartException("Cart is required.");
        }
        if (cart.getBuyer() != this) {
            throw new InvalidCartException("The cart does not belong to this buyer.");
        }
        if (this.cart != null) {
            throw new InvalidCartException("The buyer already has a cart. A buyer owns exactly one cart.");
        }
        this.cart = cart;
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    /** Registers an order placed by this buyer. */
    public void addOrder(Order order) {
        if (order == null) {
            throw new InvalidOrderException("Order is required.");
        }
        if (order.getBuyer() != this) {
            throw new InvalidOrderException("The order does not belong to this buyer.");
        }
        orders.add(order);
    }

    /** Indicates whether the buyer can currently make purchases. */
    public boolean canPurchase() {
        return isActive() && commercialStatus == CommercialStatus.ENABLED;
    }

    private Address requireAddress(Address address, String message) {
        if (address == null) {
            throw new InvalidUserException(message);
        }
        return address;
    }

    @Override
    public String toString() {
        return "Buyer{id='" + getId() + "', fullName='" + getFullName() + "', email='" + getEmail()
                + "', commercialStatus=" + commercialStatus + ", orders=" + orders.size() + "}";
    }
}
