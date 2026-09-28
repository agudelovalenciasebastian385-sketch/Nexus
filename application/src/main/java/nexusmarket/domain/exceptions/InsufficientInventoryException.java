package nexusmarket.domain.exceptions;

/** Raised when a movement would leave an inventory record with negative stock. */
public class InsufficientInventoryException extends DomainException {

    public InsufficientInventoryException(int requested, int available) {
        super("Insufficient inventory: requested " + requested + ", available " + available
                + ". Negative stock is never allowed.");
    }
}
