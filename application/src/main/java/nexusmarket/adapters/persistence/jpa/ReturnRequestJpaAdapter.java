package nexusmarket.adapters.persistence.jpa;

import java.util.List;
import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.mappers.ReturnRequestJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaReturnRequestRepository;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.ports.out.ReturnRequestRepositoryPort;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for ReturnRequest backed by Spring Data JPA (MySQL).
 */
@Repository
public class ReturnRequestJpaAdapter implements ReturnRequestRepositoryPort {

    private final SpringDataJpaReturnRequestRepository returnRequestRepository;
    private final JpaAggregateLoader loader;

    public ReturnRequestJpaAdapter(SpringDataJpaReturnRequestRepository returnRequestRepository,
                                   JpaAggregateLoader loader) {
        this.returnRequestRepository = returnRequestRepository;
        this.loader = loader;
    }

    @Override
    public ReturnRequest save(ReturnRequest returnRequest) {
        returnRequestRepository.save(ReturnRequestJpaMapper.toEntity(returnRequest));
        return returnRequest;
    }

    @Override
    public void update(ReturnRequest returnRequest) {
        returnRequestRepository.save(ReturnRequestJpaMapper.toEntity(returnRequest));
    }

    @Override
    public Optional<ReturnRequest> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return returnRequestRepository.findById(id).map(loader::toReturnRequest);
    }

    @Override
    public List<ReturnRequest> findByOrder(Order order) {
        if (order == null || order.getId() == null) {
            return List.of();
        }
        return returnRequestRepository.findByOrderId(order.getId()).stream()
                .map(entity -> ReturnRequestJpaMapper.toDomain(entity, order))
                .toList();
    }
}
