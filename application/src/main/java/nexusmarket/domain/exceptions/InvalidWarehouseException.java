package nexusmarket.domain.exceptions;

/** Raised when a warehouse violates a domain rule. */
public class InvalidWarehouseException extends DomainException {

    public InvalidWarehouseException(String message) {
        super(message);
    }
}
