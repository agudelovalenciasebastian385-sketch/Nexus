package nexusmarket.domain.ports.out;

import java.util.Optional;

import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;

/**
 * Output port: Persistence and queries of {@code Cart} and its items.
 */
public interface CartRepositoryPort {

    Cart save(Cart cart);

    void update(Cart cart);

    Optional<Cart> findByBuyer(Buyer buyer);
}
