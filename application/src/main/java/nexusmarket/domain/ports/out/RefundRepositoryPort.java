package nexusmarket.domain.ports.out;

import java.util.Optional;

import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;

/**
 * Output port: Persistence and queries of {@code Refund}.
 */
public interface RefundRepositoryPort {

    Refund save(Refund refund);

    void update(Refund refund);

    Optional<Refund> findById(String id);

    Optional<Refund> findByReturnRequest(ReturnRequest returnRequest);
}
