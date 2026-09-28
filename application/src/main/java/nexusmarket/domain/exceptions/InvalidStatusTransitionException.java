package nexusmarket.domain.exceptions;

/**
 * Raised when a shipment, return request or refund is moved to a status not
 * allowed by its lifecycle.
 */
public class InvalidStatusTransitionException extends DomainException {

    public InvalidStatusTransitionException(String entity, String from, String to) {
        super("Invalid " + entity + " status transition from " + from + " to " + to + ".");
    }
}
