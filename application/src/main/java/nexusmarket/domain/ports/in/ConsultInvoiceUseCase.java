package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.User;

/**
 * Input port: ConsultInvoice use case.
 *
 * <p>Retrieves the invoice of an order.</p>
 */
public interface ConsultInvoiceUseCase {

    Invoice consultInvoice(User requestingUser, String orderId);
}
