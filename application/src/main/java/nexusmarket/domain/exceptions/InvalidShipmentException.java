package nexusmarket.domain.exceptions;

/** Raised when a shipment violates a domain rule. */
public class InvalidShipmentException extends DomainException {

    public InvalidShipmentException(String message) {
        super(message);
    }
}
