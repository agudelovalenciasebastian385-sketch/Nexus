package nexusmarket.domain.ports.out;

import java.util.List;
import java.util.Optional;

import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.ReturnRequest;

/**
 * Output port: Persistence and queries of {@code ReturnRequest}.
 */
public interface ReturnRequestRepositoryPort {

    ReturnRequest save(ReturnRequest returnRequest);

    void update(ReturnRequest returnRequest);

    Optional<ReturnRequest> findById(String id);

    List<ReturnRequest> findByOrder(Order order);
}
