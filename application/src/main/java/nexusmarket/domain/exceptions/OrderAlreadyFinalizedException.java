package nexusmarket.domain.exceptions;

/** Raised when trying to modify an order that is already finalized (DELIVERED). */
public class OrderAlreadyFinalizedException extends DomainException {

    public OrderAlreadyFinalizedException(String orderId) {
        super("Order " + orderId + " is finalized and can never be modified.");
    }
}
