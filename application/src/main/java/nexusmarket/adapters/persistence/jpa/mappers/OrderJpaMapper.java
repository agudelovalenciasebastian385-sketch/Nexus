package nexusmarket.adapters.persistence.jpa.mappers;

import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.OrderJpaEntity;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.Shipment;
import nexusmarket.domain.valueobjects.OrderStatus;

/**
 * Bidirectional mapper between the Order Domain Model and its JPA entity.
 *
 * <p>The buyer, items and shipments are supplied by the adapter when reading. The invoice is
 * never attached on load: the Order constructor checks the invoice by object identity, which a
 * freshly loaded invoice cannot satisfy, and attachInvoice rejects a DELIVERED order. The
 * invoice is read through InvoiceRepositoryPort.findByOrder.
 */
public final class OrderJpaMapper {

    private OrderJpaMapper() {
    }

    public static OrderJpaEntity toEntity(Order domain) {
        if (domain == null) {
            return null;
        }
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(domain.getId());
        entity.setBuyerId(domain.getBuyer().getId());
        entity.setStatus(domain.getStatus().getCode());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    /**
     * Copies the mutable domain state onto the managed entity loaded in the current transaction, so
     * Hibernate keeps the version read at the start of the use case (optimistic locking).
     */
    public static OrderJpaEntity copyToEntity(Order domain, OrderJpaEntity entity) {
        entity.setStatus(domain.getStatus().getCode());
        return entity;
    }

    public static Order toDomain(OrderJpaEntity entity, Buyer buyer, List<OrderItem> items,
                                 List<Shipment> shipments) {
        if (entity == null) {
            return null;
        }
        return new Order(entity.getId(), buyer, items, OrderStatus.fromCode(entity.getStatus()),
                entity.getCreatedAt(), null, shipments);
    }
}
