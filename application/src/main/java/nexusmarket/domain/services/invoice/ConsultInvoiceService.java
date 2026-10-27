package nexusmarket.domain.services.invoice;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.services.authorization.ValidateBuyerOwnershipService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Retrieves the invoice of an order (invoice-services.md - ConsultInvoice). A
 * buyer only consults invoices of its own orders.
 */
@Service
@RequiredArgsConstructor
public class ConsultInvoiceService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public Invoice consultInvoice(User requestingUser, String orderId) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.BUYER,
                SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        validateBuyerOwnershipService.execute(requester, order);
        return invoiceRepositoryPort.findByOrder(order)
                .orElseThrow(() -> new EntityNotFoundException("Invoice", "order " + orderId));
    }
}
