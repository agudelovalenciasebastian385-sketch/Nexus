package nexusmarket.domain.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nexusmarket.domain.exceptions.InvalidCartException;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Cart.
 *
 * <p>Represents the provisional selection of products made by a buyer before
 * confirming an order. A cart belongs to exactly one {@link Buyer} and
 * contains zero or more {@link CartItem} instances; upon confirmation it gives
 * rise to an {@link Order}.</p>
 *
 * <p>Items are identified inside the cart by the pair (product, variant):
 * adding a product/variant that is already in the cart accumulates its
 * quantity instead of creating a duplicate line.</p>
 */
public class Cart {

    private final String id;
    private final Buyer buyer;
    private final List<CartItem> items = new ArrayList<>();

    /** Creates a new empty cart. */
    public Cart(String id, Buyer buyer) {
        if (id == null || id.isBlank()) {
            throw new InvalidCartException("Cart id cannot be empty.");
        }
        if (buyer == null) {
            throw new InvalidCartException("Cart buyer is required.");
        }
        this.id = id;
        this.buyer = buyer;
    }

    /** Rehydration constructor used to rebuild a persisted cart. */
    public Cart(String id, Buyer buyer, List<CartItem> items) {
        this(id, buyer);
        if (items != null) {
            for (CartItem item : items) {
                addItem(item);
            }
        }
    }

    public String getId() {
        return id;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Adds a product to the cart. If the same product and variant are already
     * in the cart, the quantities are accumulated and the unit price is
     * updated to the given one.
     */
    public void addItem(Product product, Variant variant, int quantity, BigDecimal unitPrice) {
        addItem(new CartItem(product, variant, quantity, unitPrice));
    }

    private void addItem(CartItem newItem) {
        if (newItem == null) {
            throw new InvalidCartException("Cart item is required.");
        }
        CartItem existing = findItem(newItem.getProduct(), newItem.getVariant());
        if (existing == null) {
            items.add(newItem);
            return;
        }
        CartItem merged = new CartItem(newItem.getProduct(), newItem.getVariant(),
                existing.getQuantity() + newItem.getQuantity(), newItem.getUnitPrice());
        items.set(items.indexOf(existing), merged);
    }

    /** Removes the line of the given product and variant. */
    public void removeItem(Product product, Variant variant) {
        items.remove(requireItem(product, variant));
    }

    /** Replaces the quantity of the line of the given product and variant. */
    public void changeItemQuantity(Product product, Variant variant, int quantity) {
        requireItem(product, variant).changeQuantity(quantity);
    }

    /** Sum of all item subtotals. */
    public BigDecimal totalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.subtotal());
        }
        return total;
    }

    /** Removes every item from the cart (e.g. after it gives rise to an order). */
    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    private CartItem findItem(Product product, Variant variant) {
        for (CartItem item : items) {
            if (item.matches(product, variant)) {
                return item;
            }
        }
        return null;
    }

    private CartItem requireItem(Product product, Variant variant) {
        CartItem item = findItem(product, variant);
        if (item == null) {
            throw new InvalidCartException("The product is not in the cart.");
        }
        return item;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Cart)) {
            return false;
        }
        return id.equals(((Cart) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Cart{id='" + id + "', buyer=" + buyer.getId() + ", items=" + items.size() + "}";
    }
}
