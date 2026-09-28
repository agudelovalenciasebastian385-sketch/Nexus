package nexusmarket.domain.exceptions;

/** The identification number or the email is already registered. */
public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
