package nexusmarket.domain.models;

import java.time.LocalDateTime;

import nexusmarket.domain.exceptions.InvalidInventoryException;
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
        if (type == null) {
            throw new InvalidInventoryException("Movement type is required.");
        }
        this.type = type;
        if (type == MovementType.ADJUSTMENT) {
            if (quantity == 0) {
                throw new InvalidInventoryException("Adjustment quantity cannot be zero.");
            }
        } else if (quantity <= 0) {
            throw new InvalidInventoryException("Movement quantity must be positive for " + type + ".");
        }
        this.quantity = quantity;
        if (date == null) {
            throw new InvalidInventoryException("Movement date is required.");
        }
        this.date = date;
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
