package nexusmarket.adapters.persistence.memory;

import java.util.Optional;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Buyers in memory, over the table of UserMemoryAdapter. */
@Repository
@Profile("memory")
public class BuyerMemoryAdapter implements BuyerRepositoryPort {

    private final UserMemoryAdapter users;

    public BuyerMemoryAdapter(UserMemoryAdapter users) {
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
