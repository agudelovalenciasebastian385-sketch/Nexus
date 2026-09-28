package nexusmarket.domain.exceptions;

/** Raised when a variant value object is built with invalid values. */
public class InvalidVariantException extends DomainException {

    public InvalidVariantException(String message) {
        super(message);
    }
}
