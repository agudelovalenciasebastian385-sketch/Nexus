package nexusmarket.adapters.persistence.memory;

import java.util.Optional;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.ports.out.SellerRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Sellers in memory, over the table of UserMemoryAdapter. */
@Repository
@Profile("memory")
public class SellerMemoryAdapter implements SellerRepositoryPort {

    private final UserMemoryAdapter users;

    public SellerMemoryAdapter(UserMemoryAdapter users) {
        this.users = users;
    }

    @Override
    public Seller save(Seller seller) {
        users.save(seller);
        return seller;
    }

    @Override
    public void update(Seller seller) {
        users.update(seller);
    }

    @Override
    public Optional<Seller> findById(String id) {
        return users.findById(id).filter(Seller.class::isInstance).map(Seller.class::cast);
    }
}
