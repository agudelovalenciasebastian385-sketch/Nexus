package nexusmarket.domain.services.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.ports.out.ReturnRequestRepositoryPort;

/** In-memory ReturnRequestRepositoryPort. */
public class InMemoryReturnRequestRepository implements ReturnRequestRepositoryPort {

    private final Map<String, ReturnRequest> returnRequests = new LinkedHashMap<>();

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
        return Optional.ofNullable(returnRequests.get(id));
    }

    @Override
    public List<ReturnRequest> findByOrder(Order order) {
        return returnRequests.values().stream()
                .filter(returnRequest -> returnRequest.getOrder().getId().equals(order.getId())).toList();
    }
}
