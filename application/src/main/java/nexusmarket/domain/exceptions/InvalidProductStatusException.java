package nexusmarket.domain.exceptions;

/** Raised when an operation is not allowed for the current publication status of a product. */
public class InvalidProductStatusException extends DomainException {

    public InvalidProductStatusException(String message) {
        super(message);
    }
}
