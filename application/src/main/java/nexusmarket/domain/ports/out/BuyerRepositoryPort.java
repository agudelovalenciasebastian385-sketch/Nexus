package nexusmarket.domain.ports.out;

import java.util.Optional;

import nexusmarket.domain.models.Buyer;

/**
 * Output port: Persistence and queries of {@code Buyer}.
 */
public interface BuyerRepositoryPort {

    Buyer save(Buyer buyer);

    void update(Buyer buyer);

    Optional<Buyer> findById(String id);
}
