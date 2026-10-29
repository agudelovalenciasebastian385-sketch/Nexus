package nexusmarket.domain.services.support;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.ports.out.InvoiceRepositoryPort;

/** In-memory InvoiceRepositoryPort. The date range is half-open: [from, toExclusive). */
public class InMemoryInvoiceRepository implements InvoiceRepositoryPort {

    private final Map<String, Invoice> invoices = new LinkedHashMap<>();

    @Override
    public Invoice save(Invoice invoice) {
        invoices.put(invoice.getId(), invoice);
        return invoice;
    }

    @Override
    public Optional<Invoice> findByOrder(Order order) {
        return invoices.values().stream().filter(invoice -> invoice.getOrder().getId().equals(order.getId())).findFirst();
    }

    @Override
    public boolean existsByOrder(Order order) {
        return findByOrder(order).isPresent();
    }

    @Override
    public List<Invoice> findByIssuedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        return invoices.values().stream()
                .filter(invoice -> !invoice.getIssuedAt().isBefore(from) && invoice.getIssuedAt().isBefore(toExclusive))
                .toList();
    }

    public List<Invoice> all() {
        return List.copyOf(invoices.values());
    }
}
