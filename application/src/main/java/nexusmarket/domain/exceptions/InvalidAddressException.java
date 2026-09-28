package nexusmarket.domain.exceptions;

/** Raised when an address value object is built with invalid values. */
public class InvalidAddressException extends DomainException {

    public InvalidAddressException(String message) {
        super(message);
    }
}
