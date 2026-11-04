package nexusmarket.adapters.persistence.memory;

import java.util.Optional;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.ports.out.CartRepositoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Carts in memory. */
@Repository
@Profile("memory")
public class CartMemoryAdapter implements CartRepositoryPort {

    private final MemoryStore<String, Cart> carts = new MemoryStore<>();

    @Override
    public Cart save(Cart cart) {
        carts.put(cart.getId(), cart);
        return cart;
    }

    @Override
    public void update(Cart cart) {
        carts.put(cart.getId(), cart);
    }

    @Override
    public Optional<Cart> findByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getId() == null) {
            return Optional.empty();
        }
        return carts.values().stream().filter(cart -> cart.getBuyer().getId().equals(buyer.getId())).findFirst();
    }
}
