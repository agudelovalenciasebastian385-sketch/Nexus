package nexusmarket.domain.exceptions;

/** An audit record is missing required data. */
public class InvalidAuditLogException extends DomainException {

    public InvalidAuditLogException(String message) {
        super(message);
    }
}
