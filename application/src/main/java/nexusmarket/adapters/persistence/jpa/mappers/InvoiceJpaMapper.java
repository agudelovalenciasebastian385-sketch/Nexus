package nexusmarket.adapters.persistence.jpa.mappers;

import nexusmarket.adapters.persistence.jpa.entities.InvoiceJpaEntity;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;

/**
 * Bidirectional mapper between the Invoice Domain Model and its JPA entity.
 *
 * <p>The order is stored as a reference identifier; the resolved Order is supplied by the
 * adapter when reading. The stored total is kept as issued.
 */
public final class InvoiceJpaMapper {

    private InvoiceJpaMapper() {
    }

    public static InvoiceJpaEntity toEntity(Invoice domain) {
        if (domain == null) {
            return null;
        }
        InvoiceJpaEntity entity = new InvoiceJpaEntity();
        entity.setId(domain.getId());
        entity.setOrderId(domain.getOrder().getId());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setIssuedAt(domain.getIssuedAt());
        return entity;
    }

    public static Invoice toDomain(InvoiceJpaEntity entity, Order order) {
        if (entity == null) {
            return null;
        }
        return new Invoice(entity.getId(), order, entity.getTotalAmount(), entity.getIssuedAt());
    }
}
