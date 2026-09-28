package nexusmarket.domain.exceptions;

/** Raised when an order or order item violates a domain rule. */
public class InvalidOrderException extends DomainException {

    public InvalidOrderException(String message) {
        super(message);
    }
}
