package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.User;

/**
 * Input port: GenerateInvoice use case.
 *
 * <p>Generates the unique invoice of a {@code PAID} order. Invoked by {@code ValidatePaymentService}; it is not exposed in any role port.</p>
 */
public interface GenerateInvoiceUseCase {

    Invoice generateInvoice(Order order, User performedBy);
}
