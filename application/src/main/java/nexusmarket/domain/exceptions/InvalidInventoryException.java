package nexusmarket.domain.exceptions;

/** Raised when an inventory record or inventory movement violates a domain rule. */
public class InvalidInventoryException extends DomainException {

    public InvalidInventoryException(String message) {
        super(message);
    }
}
