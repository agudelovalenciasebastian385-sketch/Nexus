package nexusmarket.domain.models;

import java.time.LocalDateTime;
import java.util.Objects;

import nexusmarket.domain.valueobjects.MovementType;

/**
 * InventoryMovement.
 *
 * <p>Represents a change to the stock of an {@link Inventory} record. A
 * movement belongs to exactly one inventory record and is immutable once
 * recorded.</p>
 */
public class InventoryMovement {

    private final MovementType type;
    private final int quantity;
    private final LocalDateTime date;

    public InventoryMovement(MovementType type, int quantity, LocalDateTime date) {
        this.type = Objects.requireNonNull(type, "Movement type is required.");
        if (type == MovementType.ADJUSTMENT) {
            if (quantity == 0) {
                throw new IllegalArgumentException("Adjustment quantity cannot be zero.");
            }
        } else if (quantity <= 0) {
            throw new IllegalArgumentException("Movement quantity must be positive for " + type + ".");
        }
        this.quantity = quantity;
        this.date = Objects.requireNonNull(date, "Movement date is required.");
    }

    public MovementType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "InventoryMovement{type=" + type + ", quantity=" + quantity + ", date=" + date + "}";
    }
}
