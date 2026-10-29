package nexusmarket.domain.services.support;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.ports.out.RefundRepositoryPort;

/** In-memory RefundRepositoryPort. The date range is half-open and only matches processed refunds. */
public class InMemoryRefundRepository implements RefundRepositoryPort {

    private final Map<String, Refund> refunds = new LinkedHashMap<>();

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
        return Optional.ofNullable(refunds.get(id));
    }

    @Override
    public Optional<Refund> findByReturnRequest(ReturnRequest returnRequest) {
        return refunds.values().stream()
                .filter(refund -> refund.getReturnRequest().getId().equals(returnRequest.getId())).findFirst();
    }

    @Override
    public List<Refund> findByProcessedAtBetween(LocalDateTime from, LocalDateTime toExclusive) {
        return refunds.values().stream()
                .filter(refund -> refund.getProcessedAt() != null && !refund.getProcessedAt().isBefore(from)
                        && refund.getProcessedAt().isBefore(toExclusive))
                .toList();
    }
}
