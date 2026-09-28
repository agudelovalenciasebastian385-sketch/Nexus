package nexusmarket.domain.models;

import java.math.BigDecimal;

import nexusmarket.domain.exceptions.InvalidOrderException;
import nexusmarket.domain.valueobjects.Variant;

/**
 * OrderItem.
 *
 * <p>Represents a confirmed product within an order, along with its quantity
 * and the unit price in effect at the time of purchase. An order item
 * references exactly one {@link Product}.</p>
 *
 * <p>A physical item records the {@link Warehouse} where its stock was
 * reserved ({@code originWarehouse}); a digital item has none.</p>
 */
public class OrderItem {

    private final Product product;
    /** Purchased variant, if applicable. */
    private final Variant variant;
    private final int quantity;
    private final BigDecimal unitPrice;
    /** Warehouse where the stock was reserved; {@code null} for digital products. */
    private final Warehouse originWarehouse;

    public OrderItem(Product product, Variant variant, int quantity, BigDecimal unitPrice,
                     Warehouse originWarehouse) {
        if (product == null) {
            throw new InvalidOrderException("Order item product is required.");
        }
        this.product = product;
        if (quantity <= 0) {
            throw new InvalidOrderException("Order item quantity must be positive.");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new InvalidOrderException("Order item unit price must be zero or positive.");
        }
        if (product instanceof PhysicalProduct && originWarehouse == null) {
            throw new InvalidOrderException("A physical order item requires an origin warehouse.");
        }
        if (product instanceof DigitalProduct && originWarehouse != null) {
            throw new InvalidOrderException("A digital order item cannot have an origin warehouse.");
        }
        this.variant = variant;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.originWarehouse = originWarehouse;
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

    public Warehouse getOriginWarehouse() {
        return originWarehouse;
    }

    public boolean isPhysical() {
        return product instanceof PhysicalProduct;
    }

    /** Quantity multiplied by the unit price in effect at the time of purchase. */
    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public String toString() {
        return "OrderItem{product=" + product.getId() + ", variant=" + variant
                + ", quantity=" + quantity + ", unitPrice=" + unitPrice + "}";
    }
}
