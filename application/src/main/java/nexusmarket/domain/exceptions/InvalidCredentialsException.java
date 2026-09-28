package nexusmarket.domain.exceptions;

/** Unknown email or wrong password. */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Invalid credentials.");
    }
}
