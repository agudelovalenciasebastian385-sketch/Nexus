package nexusmarket.domain.services.support;

import java.util.Optional;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.ports.out.SellerRepositoryPort;

/** In-memory SellerRepositoryPort over the shared user store. */
public class InMemorySellerRepository implements SellerRepositoryPort {

    private final InMemoryUserRepository users;

    public InMemorySellerRepository(InMemoryUserRepository users) {
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
