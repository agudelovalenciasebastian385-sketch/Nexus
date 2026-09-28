package nexusmarket.domain.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nexusmarket.domain.exceptions.InsufficientInventoryException;
import nexusmarket.domain.exceptions.InvalidInventoryException;
import nexusmarket.domain.valueobjects.InventoryCondition;
import nexusmarket.domain.valueobjects.MovementType;

/**
 * Inventory.
 *
 * <p>Represents the stock of a {@link PhysicalProduct} in a specific
 * {@link Warehouse}. Inventory is distributed and must be mandatorily linked
 * to a product and a warehouse.</p>
 *
 * <p>The stock is split into the available {@code quantity} (which can be
 * reserved) and the {@code reservedQuantity} held for orders in progress.</p>
 *
 * <p><b>Constraint:</b> negative stock is never allowed under any
 * circumstance; every operation that would produce a negative quantity is
 * rejected. Damaged inventory can never be reserved.</p>
 *
 * <p>It has no id of its own: its identity is the pair (product, warehouse),
 * so equality is determined by that pair.</p>
 */
public class Inventory {

    private final PhysicalProduct product;
    private final Warehouse warehouse;
    /** Available stock. */
    private int quantity;
    /** Stock held for orders in progress. */
    private int reservedQuantity;
    private InventoryCondition condition;
    private final List<InventoryMovement> movements = new ArrayList<>();

    public Inventory(PhysicalProduct product, Warehouse warehouse, int initialQuantity) {
        this(product, warehouse, initialQuantity, 0, InventoryCondition.AVAILABLE, null);
    }

    /** Rehydration constructor used to rebuild a persisted inventory record. */
    public Inventory(PhysicalProduct product, Warehouse warehouse, int quantity, int reservedQuantity,
                     InventoryCondition condition, List<InventoryMovement> movements) {
        if (product == null) {
            throw new InvalidInventoryException("Inventory product is required.");
        }
        if (warehouse == null) {
            throw new InvalidInventoryException("Inventory warehouse is required.");
        }
        if (quantity < 0 || reservedQuantity < 0) {
            throw new InvalidInventoryException("Inventory quantities can never be negative.");
        }
        this.product = product;
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.condition = condition == null ? InventoryCondition.AVAILABLE : condition;
        if (movements != null) {
            this.movements.addAll(movements);
        }
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

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public InventoryCondition getCondition() {
        return condition;
    }

    public void setCondition(InventoryCondition condition) {
        if (condition == null) {
            throw new InvalidInventoryException("Inventory condition is required.");
        }
        this.condition = condition;
    }

    public List<InventoryMovement> getMovements() {
        return Collections.unmodifiableList(movements);
    }

    /** Indicates whether the record can be reserved for the given quantity. */
    public boolean canReserve(int quantity) {
        return condition == InventoryCondition.AVAILABLE && this.quantity >= quantity;
    }

    /**
     * Records a stock movement and applies its effect:
     * <ul>
     *   <li>{@code INBOUND} and {@code RETURN}: available + quantity.</li>
     *   <li>{@code RESERVATION}: available → reserved (never on damaged stock).</li>
     *   <li>{@code RESERVATION_RELEASE}: reserved → available.</li>
     *   <li>{@code SALE_OUTBOUND}: consumes reserved stock.</li>
     *   <li>{@code ADJUSTMENT}: signed manual correction of the available stock.</li>
     * </ul>
     *
     * @return the recorded movement
     * @throws InsufficientInventoryException if the available stock would become negative
     * @throws InvalidInventoryException if the reserved stock would become negative or the stock is damaged
     */
    public InventoryMovement registerMovement(MovementType type, int quantity, LocalDateTime date) {
        InventoryMovement movement = new InventoryMovement(type, quantity, date);
        int available = this.quantity;
        int reserved = this.reservedQuantity;
        switch (type) {
            case INBOUND, RETURN -> available += quantity;
            case ADJUSTMENT -> available += quantity;
            case RESERVATION -> {
                if (condition == InventoryCondition.DAMAGED) {
                    throw new InvalidInventoryException("Damaged inventory can never be reserved.");
                }
                available -= quantity;
                reserved += quantity;
            }
            case RESERVATION_RELEASE -> {
                reserved -= quantity;
                available += quantity;
            }
            case SALE_OUTBOUND -> reserved -= quantity;
        }
        if (available < 0) {
            throw new InsufficientInventoryException(Math.abs(quantity), this.quantity);
        }
        if (reserved < 0) {
            throw new InvalidInventoryException("Not enough reserved stock: requested " + quantity
                    + ", reserved " + this.reservedQuantity + ".");
        }
        this.quantity = available;
        this.reservedQuantity = reserved;
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
                + ", quantity=" + quantity + ", reservedQuantity=" + reservedQuantity
                + ", condition=" + condition + ", movements=" + movements.size() + "}";
    }
}
