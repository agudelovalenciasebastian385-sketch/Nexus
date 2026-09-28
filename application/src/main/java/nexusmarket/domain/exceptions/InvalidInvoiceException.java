package nexusmarket.domain.exceptions;

/** Raised when an invoice violates a domain rule. */
public class InvalidInvoiceException extends DomainException {

    public InvalidInvoiceException(String message) {
        super(message);
    }
}
