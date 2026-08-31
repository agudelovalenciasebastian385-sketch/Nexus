package nexusmarket.domain.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import nexusmarket.domain.valueobjects.InventoryCondition;
import nexusmarket.domain.valueobjects.MovementType;

/**
 * Inventory.
 *
 * <p>Represents the available stock of a {@link PhysicalProduct} in a specific
 * {@link Warehouse}. Inventory is distributed and must be mandatorily linked
 * to a product and a warehouse.</p>
 *
 * <p><b>Constraint:</b> negative stock is never allowed under any
 * circumstance; every operation that would produce a negative quantity is
 * rejected.</p>
 *
 * <p>It has no id of its own: its identity is the pair (product, warehouse),
 * so equality is determined by that pair.</p>
 */
public class Inventory {

    private final PhysicalProduct product;
    private final Warehouse warehouse;
    private int quantity;
    private InventoryCondition condition;
    private final List<InventoryMovement> movements = new ArrayList<>();

    public Inventory(PhysicalProduct product, Warehouse warehouse, int initialQuantity) {
        this(product, warehouse, initialQuantity, InventoryCondition.AVAILABLE);
    }

    public Inventory(PhysicalProduct product, Warehouse warehouse, int initialQuantity,
                     InventoryCondition condition) {
        this.product = Objects.requireNonNull(product, "Inventory product is required.");
        this.warehouse = Objects.requireNonNull(warehouse, "Inventory warehouse is required.");
        if (initialQuantity < 0) {
            throw new IllegalArgumentException("Inventory quantity can never be negative.");
        }
        this.quantity = initialQuantity;
        this.condition = condition == null ? InventoryCondition.AVAILABLE : condition;
    }

    public PhysicalProduct getProduct() {
        return product;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public int getQuantity() {
        return quantity;
    }

    public InventoryCondition getCondition() {
        return condition;
    }

    public void setCondition(InventoryCondition condition) {
        this.condition = Objects.requireNonNull(condition, "Inventory condition is required.");
    }

    public List<InventoryMovement> getMovements() {
        return Collections.unmodifiableList(movements);
    }

    /**
     * Records a stock movement and applies its effect to the current quantity:
     * <ul>
     *   <li>{@code INBOUND} and {@code RETURN} increase the stock.</li>
     *   <li>{@code RESERVATION} and {@code SALE_OUTBOUND} decrease the stock
     *       (rejected if there is not enough available stock).</li>
     *   <li>{@code ADJUSTMENT} applies a signed manual correction.</li>
     * </ul>
     *
     * @return the recorded movement
     * @throws IllegalStateException if the movement would produce negative stock
     */
    public InventoryMovement registerMovement(MovementType type, int quantity, LocalDateTime date) {
        Objects.requireNonNull(type, "Movement type is required.");
        Objects.requireNonNull(date, "Movement date is required.");
        if (type != MovementType.ADJUSTMENT && quantity <= 0) {
            throw new IllegalArgumentException("Movement quantity must be positive for " + type + ".");
        }
        if (type == MovementType.ADJUSTMENT && quantity == 0) {
            throw new IllegalArgumentException("Adjustment quantity cannot be zero.");
        }
        int effective = switch (type) {
            case INBOUND, RETURN -> this.quantity + quantity;
            case RESERVATION, SALE_OUTBOUND -> this.quantity - quantity;
            case ADJUSTMENT -> this.quantity + quantity;
        };
        if (effective < 0) {
            throw new IllegalStateException("Insufficient inventory: requested " + quantity
                    + ", available " + this.quantity + ". Negative stock is never allowed.");
        }
        this.quantity = effective;
        InventoryMovement movement = new InventoryMovement(type, quantity, date);
        movements.add(movement);
        return movement;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Inventory)) {
            return false;
        }
        Inventory otherInventory = (Inventory) other;
        return product.getId().equals(otherInventory.product.getId())
                && warehouse.getId().equals(otherInventory.warehouse.getId());
    }

    @Override
    public int hashCode() {
        return 31 * product.getId().hashCode() + warehouse.getId().hashCode();
    }

    @Override
    public String toString() {
        return "Inventory{product=" + product.getId() + ", warehouse=" + warehouse.getId()
                + ", quantity=" + quantity + ", condition=" + condition + ", movements=" + movements.size() + "}";
    }
}
