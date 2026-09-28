package nexusmarket.domain.exceptions;

/** Raised when a refund violates a domain rule. */
public class InvalidRefundException extends DomainException {

    public InvalidRefundException(String message) {
        super(message);
    }
}
