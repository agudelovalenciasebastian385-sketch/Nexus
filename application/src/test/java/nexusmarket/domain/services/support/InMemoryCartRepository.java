package nexusmarket.domain.services.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.ports.out.CartRepositoryPort;

/** In-memory CartRepositoryPort. */
public class InMemoryCartRepository implements CartRepositoryPort {

    private final Map<String, Cart> carts = new LinkedHashMap<>();

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
        return carts.values().stream().filter(cart -> cart.getBuyer().getId().equals(buyer.getId())).findFirst();
    }
}
