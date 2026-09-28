package nexusmarket.domain.exceptions;

/** RG-01/RG-02/RG-03 violation: unauthenticated or inactive user, role not allowed or information outside the user's responsibilities. */
public class UnauthorizedOperationException extends DomainException {

    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
