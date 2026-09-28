package nexusmarket.domain.exceptions;

/** The payment gateway rejected the payment of an order. */
public class PaymentRejectedException extends DomainException {

    public PaymentRejectedException(String orderId) {
        super("The payment of order " + orderId + " was rejected. The order remains pending payment.");
    }
}
