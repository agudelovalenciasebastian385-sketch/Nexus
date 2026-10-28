package nexusmarket.domain.models;

import java.math.BigDecimal;
import java.util.Objects;

import nexusmarket.domain.exceptions.InvalidCartException;
import nexusmarket.domain.exceptions.InvalidProductStatusException;
import nexusmarket.domain.valueobjects.Variant;

/**
 * CartItem.
 *
 * <p>Represents a product selected within a {@link Cart}, along with the
 * chosen variant, quantity and the unit price captured when the product was
 * added. A cart item references exactly one {@link Product}.</p>
 *
 * <p>Only products in {@code PUBLISHED} status can be added to a cart, and the
 * chosen variant (if any) must be one of the variants offered by the
 * product.</p>
 *
 * <p>Those two offer checks apply when an item is added. An item rebuilt from
 * storage ({@link #rehydrate}) skips them, because the product may have been
 * suspended, discontinued or lost the variant after the item was added; such an
 * item stays in the cart and ConfirmOrderService rejects it at checkout.</p>
 */
public class CartItem {

    private final Product product;
    /** Chosen variant, if applicable. */
    private final Variant variant;
    private int quantity;
    private final BigDecimal unitPrice;

    public CartItem(Product product, Variant variant, int quantity, BigDecimal unitPrice) {
        this(product, variant, quantity, unitPrice, true);
    }

    /**
     * Rehydration factory: rebuilds a stored item without checking that the
     * product is still published or still offers the variant. Product, quantity
     * and unit price are validated as usual.
     */
    public static CartItem rehydrate(Product product, Variant variant, int quantity, BigDecimal unitPrice) {
        return new CartItem(product, variant, quantity, unitPrice, false);
    }

    private CartItem(Product product, Variant variant, int quantity, BigDecimal unitPrice,
                     boolean validateOffer) {
        if (product == null) {
            throw new InvalidCartException("Cart item product is required.");
        }
        if (validateOffer && !product.isPublished()) {
            throw new InvalidProductStatusException("Only published products can be added to a cart.");
        }
        if (validateOffer && variant != null && !product.hasVariant(variant)) {
            throw new InvalidCartException("The variant " + variant + " is not offered by this product.");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new InvalidCartException("Cart item unit price must be zero or positive.");
        }
        this.product = product;
        this.variant = variant;
        this.quantity = requirePositive(quantity);
        this.unitPrice = unitPrice;
    }

    public Product getProduct() {
        return product;
    }

    public Variant getVariant() {
        return variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /** Replaces the selected quantity. */
    public void changeQuantity(int quantity) {
        this.quantity = requirePositive(quantity);
    }

    /** Quantity multiplied by the captured unit price. */
    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /** Indicates whether this item corresponds to the given product and variant. */
    public boolean matches(Product product, Variant variant) {
        return this.product.equals(product) && Objects.equals(this.variant, variant);
    }

    private int requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new InvalidCartException("Cart item quantity must be positive.");
        }
        return quantity;
    }

    @Override
    public String toString() {
        return "CartItem{product=" + product.getId() + ", variant=" + variant
                + ", quantity=" + quantity + ", unitPrice=" + unitPrice + "}";
    }
}
