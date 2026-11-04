package nexusmarket.adapters.persistence.memory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.ports.out.RefundRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Refunds in memory. */
@Repository
@Profile("memory")
public class RefundMemoryAdapter implements RefundRepositoryPort {

    private final MemoryStore<String, Refund> refunds = new MemoryStore<>();

    @Override
    public Refund save(Refund refund) {
        refunds.put(refund.getId(), refund);
        return refund;
    }

    @Override
    public void update(Refund refund) {
        refunds.put(refund.getId(), refund);
    }

    @Override
    public Optional<Refund> findById(String id) {
        return refunds.get(id);
    }

    @Override
    public Optional<Refund> findByReturnRequest(ReturnRequest returnRequest) {
        if (returnRequest == null || returnRequest.getId() == null) {
            return Optional.empty();
        }
        return refunds.values().stream()
                .filter(refund -> refund.getReturnRequest().getId().equals(returnRequest.getId())).findFirst();
    }

    @Override
    public List<Refund> findByProcessedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        if (from == null || toExclusive == null) {
            return List.of();
        }
        return refunds.values().stream()
                .filter(refund -> MemoryStore.inRange(refund.getProcessedAt(), from, toExclusive)).toList();
    }
}
