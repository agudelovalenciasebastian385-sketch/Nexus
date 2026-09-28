package nexusmarket.domain.exceptions;

/** Raised when a return request violates a domain rule. */
public class InvalidReturnRequestException extends DomainException {

    public InvalidReturnRequestException(String message) {
        super(message);
    }
}
