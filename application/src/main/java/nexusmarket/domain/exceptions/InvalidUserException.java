package nexusmarket.domain.exceptions;

/** Raised when a user (or one of its specializations) violates a domain rule. */
public class InvalidUserException extends DomainException {

    public InvalidUserException(String message) {
        super(message);
    }
}
