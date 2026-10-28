package nexusmarket.adapters.persistence.jpa.mappers;

import nexusmarket.adapters.persistence.jpa.entities.RefundJpaEntity;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.valueobjects.RefundStatus;

/**
 * Bidirectional mapper between the Refund Domain Model and its JPA entity.
 *
 * <p>The return request is stored as a reference identifier; the resolved ReturnRequest is
 * supplied by the adapter when reading. processedAt is restored as stored.
 */
public final class RefundJpaMapper {

    private RefundJpaMapper() {
    }

    public static RefundJpaEntity toEntity(Refund domain) {
        if (domain == null) {
            return null;
        }
        RefundJpaEntity entity = new RefundJpaEntity();
        entity.setId(domain.getId());
        entity.setReturnRequestId(domain.getReturnRequest().getId());
        entity.setAmount(domain.getAmount());
        entity.setStatus(domain.getStatus().getCode());
        entity.setProcessedAt(domain.getProcessedAt());
        return entity;
    }

    public static Refund toDomain(RefundJpaEntity entity, ReturnRequest returnRequest) {
        if (entity == null) {
            return null;
        }
        return new Refund(entity.getId(), returnRequest, entity.getAmount(),
                RefundStatus.fromCode(entity.getStatus()), entity.getProcessedAt());
    }
}
