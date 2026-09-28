package nexusmarket.domain.services.invoice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.InvalidOrderException;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.GenerateInvoiceUseCase;
import nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.OrderStatus;
import org.springframework.stereotype.Service;

/**
 * Generates the unique invoice of a {@code PAID} order and attaches it to the
 * order (invoice-services.md - GenerateInvoice). Invoked by
 * {@code ValidatePaymentService}, which persists the order.
 */
@Service
@RequiredArgsConstructor
public class GenerateInvoiceService implements GenerateInvoiceUseCase {

    private final InvoiceRepositoryPort invoiceRepositoryPort;
    private final RegisterAuditLogService registerAuditLogService;

    @Override
    public Invoice generateInvoice(Order order, User performedBy) {
        if (order.getStatus() != OrderStatus.PAID) {
            throw new InvalidOrderException("Only a paid order can be invoiced; order " + order.getId() + " is "
                    + order.getStatus().getCode() + ".");
        }
        if (order.getInvoice() != null || invoiceRepositoryPort.existsByOrder(order)) {
            throw new InvalidOrderException("Order " + order.getId()
                    + " already has an invoice. An order generates exactly one invoice.");
        }

        Invoice invoice = new Invoice(UUID.randomUUID().toString(), order, LocalDateTime.now());
        order.attachInvoice(invoice);
        Invoice saved = invoiceRepositoryPort.save(invoice);
        registerAuditLogService.execute(performedBy, AuditAction.INVOICE_GENERATED, AuditEntityType.INVOICE,
                saved.getId(), Map.of("order", order.getId(), "total", saved.getTotalAmount().toPlainString()));
        return saved;
    }
}
