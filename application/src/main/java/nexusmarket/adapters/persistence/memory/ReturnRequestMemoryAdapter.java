package nexusmarket.adapters.persistence.memory;

import java.util.List;
import java.util.Optional;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.ports.out.ReturnRequestRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Return requests in memory. */
@Repository
@Profile("memory")
public class ReturnRequestMemoryAdapter implements ReturnRequestRepositoryPort {

    private final MemoryStore<String, ReturnRequest> returnRequests = new MemoryStore<>();

    @Override
    public ReturnRequest save(ReturnRequest returnRequest) {
        returnRequests.put(returnRequest.getId(), returnRequest);
        return returnRequest;
    }

    @Override
    public void update(ReturnRequest returnRequest) {
        returnRequests.put(returnRequest.getId(), returnRequest);
    }

    @Override
    public Optional<ReturnRequest> findById(String id) {
        return returnRequests.get(id);
    }

    @Override
    public List<ReturnRequest> findByOrder(Order order) {
        if (order == null || order.getId() == null) {
            return List.of();
        }
        return returnRequests.values().stream()
                .filter(returnRequest -> returnRequest.getOrder().getId().equals(order.getId())).toList();
    }
}
