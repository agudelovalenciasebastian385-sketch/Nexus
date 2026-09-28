package nexusmarket.domain.exceptions;

/** Raised when an order is moved to a status not allowed by its lifecycle. */
public class InvalidOrderStatusTransitionException extends DomainException {

    public InvalidOrderStatusTransitionException(String from, String to) {
        super("Invalid order status transition from " + from + " to " + to + ".");
    }
}
