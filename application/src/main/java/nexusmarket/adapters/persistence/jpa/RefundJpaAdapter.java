package nexusmarket.adapters.persistence.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.mappers.RefundJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaRefundRepository;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.ports.out.RefundRepositoryPort;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for Refund backed by Spring Data JPA (MySQL).
 */
@Repository
public class RefundJpaAdapter implements RefundRepositoryPort {

    private final SpringDataJpaRefundRepository refundRepository;
    private final JpaAggregateLoader loader;

    public RefundJpaAdapter(SpringDataJpaRefundRepository refundRepository, JpaAggregateLoader loader) {
        this.refundRepository = refundRepository;
        this.loader = loader;
    }

    @Override
    public Refund save(Refund refund) {
        refundRepository.save(RefundJpaMapper.toEntity(refund));
        return refund;
    }

    @Override
    public void update(Refund refund) {
        refundRepository.save(RefundJpaMapper.toEntity(refund));
    }

    @Override
    public Optional<Refund> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return refundRepository.findById(id)
                .map(entity -> RefundJpaMapper.toDomain(entity,
                        loader.requireReturnRequest(entity.getReturnRequestId())));
    }

    @Override
    public Optional<Refund> findByReturnRequest(ReturnRequest returnRequest) {
        if (returnRequest == null || returnRequest.getId() == null) {
            return Optional.empty();
        }
        return refundRepository.findByReturnRequestId(returnRequest.getId())
                .map(entity -> RefundJpaMapper.toDomain(entity, returnRequest));
    }

    @Override
    public List<Refund> findByProcessedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        if (from == null || toExclusive == null) {
            return List.of();
        }
        return refundRepository.findByProcessedAtGreaterThanEqualAndProcessedAtLessThan(from, toExclusive)
                .stream()
                .map(entity -> RefundJpaMapper.toDomain(entity,
                        loader.requireReturnRequest(entity.getReturnRequestId())))
                .toList();
    }
}
