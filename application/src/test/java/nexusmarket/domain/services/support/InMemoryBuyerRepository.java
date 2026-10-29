package nexusmarket.domain.services.support;

import java.util.Optional;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;

/** In-memory BuyerRepositoryPort over the shared user store. */
public class InMemoryBuyerRepository implements BuyerRepositoryPort {

    private final InMemoryUserRepository users;

    public InMemoryBuyerRepository(InMemoryUserRepository users) {
        this.users = users;
    }

    @Override
    public Buyer save(Buyer buyer) {
        users.save(buyer);
        return buyer;
    }

    @Override
    public void update(Buyer buyer) {
        users.update(buyer);
    }

    @Override
    public Optional<Buyer> findById(String id) {
        return users.findById(id).filter(Buyer.class::isInstance).map(Buyer.class::cast);
    }
}
