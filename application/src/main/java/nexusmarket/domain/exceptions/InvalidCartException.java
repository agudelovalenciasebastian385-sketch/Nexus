package nexusmarket.domain.exceptions;

/** Raised when a cart or cart item violates a domain rule. */
public class InvalidCartException extends DomainException {

    public InvalidCartException(String message) {
        super(message);
    }
}
