package nexusmarket.adapters.persistence.memory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Invoices in memory. */
@Repository
@Profile("memory")
public class InvoiceMemoryAdapter implements InvoiceRepositoryPort {

    private final MemoryStore<String, Invoice> invoices = new MemoryStore<>();

    @Override
    public Invoice save(Invoice invoice) {
        invoices.put(invoice.getId(), invoice);
        return invoice;
    }

    @Override
    public Optional<Invoice> findByOrder(Order order) {
        if (order == null || order.getId() == null) {
            return Optional.empty();
        }
        return invoices.values().stream().filter(invoice -> invoice.getOrder().getId().equals(order.getId())).findFirst();
    }

    @Override
    public boolean existsByOrder(Order order) {
        return findByOrder(order).isPresent();
    }

    @Override
    public List<Invoice> findByIssuedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        if (from == null || toExclusive == null) {
            return List.of();
        }
        return invoices.values().stream()
                .filter(invoice -> MemoryStore.inRange(invoice.getIssuedAt(), from, toExclusive)).toList();
    }
}
