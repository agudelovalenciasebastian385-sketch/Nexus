package nexusmarket.domain.exceptions;

/** Raised when a product violates a catalog rule. */
public class InvalidProductException extends DomainException {

    public InvalidProductException(String message) {
        super(message);
    }
}
