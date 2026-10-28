package nexusmarket.adapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.mappers.InvoiceJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaInvoiceRepository;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.ports.out.InvoiceRepositoryPort;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for Invoice backed by Spring Data JPA (MySQL).
 */
@Repository
public class InvoiceJpaAdapter implements InvoiceRepositoryPort {

    private final SpringDataJpaInvoiceRepository invoiceRepository;
    private final JpaAggregateLoader loader;

    public InvoiceJpaAdapter(SpringDataJpaInvoiceRepository invoiceRepository, JpaAggregateLoader loader) {
        this.invoiceRepository = invoiceRepository;
        this.loader = loader;
    }

    @Override
    public Invoice save(Invoice invoice) {
        invoiceRepository.save(InvoiceJpaMapper.toEntity(invoice));
        return invoice;
    }

    @Override
    public Optional<Invoice> findByOrder(Order order) {
        if (order == null || order.getId() == null) {
            return Optional.empty();
        }
        return invoiceRepository.findByOrderId(order.getId())
                .map(entity -> InvoiceJpaMapper.toDomain(entity, order));
    }

    @Override
    public boolean existsByOrder(Order order) {
        return order != null && order.getId() != null && invoiceRepository.existsByOrderId(order.getId());
    }

    @Override
    public List<Invoice> findByIssuedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        if (from == null || toExclusive == null) {
            return List.of();
        }
        return invoiceRepository.findByIssuedAtGreaterThanEqualAndIssuedAtLessThan(from, toExclusive).stream()
                .map(entity -> InvoiceJpaMapper.toDomain(entity, loader.requireOrder(entity.getOrderId())))
                .toList();
    }
}
