package nexusmarket.domain.models;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * OrderItem.
 *
 * <p>Represents a confirmed product within an order, along with its quantity
 * and the unit price in effect at the time of purchase. An order item
 * references exactly one {@link Product}.</p>
 */
public class OrderItem {

    private final Product product;
    /** Purchased variant, if applicable (modeled as String in this delivery). */
    private final String variant;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(Product product, String variant, int quantity, BigDecimal unitPrice) {
        this.product = Objects.requireNonNull(product, "Order item product is required.");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Order item quantity must be positive.");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Order item unit price must be zero or positive.");
        }
        this.variant = variant;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Product getProduct() {
        return product;
    }

    public String getVariant() {
        return variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /** Quantity multiplied by the unit price in effect at the time of purchase. */
    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "OrderItem{product=" + product.getId() + ", variant='" + variant
                + "', quantity=" + quantity + ", unitPrice=" + unitPrice + "}";
    }
}
