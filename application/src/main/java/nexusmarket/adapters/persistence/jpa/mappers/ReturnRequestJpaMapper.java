package nexusmarket.adapters.persistence.jpa.mappers;

import nexusmarket.adapters.persistence.jpa.entities.ReturnRequestJpaEntity;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.valueobjects.ReturnStatus;

/**
 * Bidirectional mapper between the ReturnRequest Domain Model and its JPA entity.
 *
 * <p>The order is stored as a reference identifier; the resolved Order is supplied by the
 * adapter when reading.
 */
public final class ReturnRequestJpaMapper {

    private ReturnRequestJpaMapper() {
    }

    public static ReturnRequestJpaEntity toEntity(ReturnRequest domain) {
        if (domain == null) {
            return null;
        }
        ReturnRequestJpaEntity entity = new ReturnRequestJpaEntity();
        entity.setId(domain.getId());
        entity.setOrderId(domain.getOrder().getId());
        entity.setReason(domain.getReason());
        entity.setStatus(domain.getStatus().getCode());
        return entity;
    }

    public static ReturnRequest toDomain(ReturnRequestJpaEntity entity, Order order) {
        if (entity == null) {
            return null;
        }
        return new ReturnRequest(entity.getId(), order, entity.getReason(),
                ReturnStatus.fromCode(entity.getStatus()));
    }
}
