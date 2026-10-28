package nexusmarket.adapters.persistence.jpa;

import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.mappers.CartItemJpaMapper;
import nexusmarket.adapters.persistence.jpa.mappers.CartJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaCartItemRepository;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaCartRepository;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.CartItem;
import nexusmarket.domain.ports.out.CartRepositoryPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Output persistence adapter for Cart backed by Spring Data JPA (MySQL).
 *
 * <p>Cart items change freely (add, remove, change quantity, clear), so writing a cart replaces
 * its rows in cart_items.
 */
@Repository
public class CartJpaAdapter implements CartRepositoryPort {

    private final SpringDataJpaCartRepository cartRepository;
    private final SpringDataJpaCartItemRepository cartItemRepository;
    private final JpaAggregateLoader loader;

    public CartJpaAdapter(SpringDataJpaCartRepository cartRepository,
                          SpringDataJpaCartItemRepository cartItemRepository,
                          JpaAggregateLoader loader) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.loader = loader;
    }

    @Override
    @Transactional
    public Cart save(Cart cart) {
        persist(cart);
        return cart;
    }

    @Override
    @Transactional
    public void update(Cart cart) {
        persist(cart);
    }

    @Override
    public Optional<Cart> findByBuyer(Buyer buyer) {
        if (buyer == null || buyer.getId() == null) {
            return Optional.empty();
        }
        return cartRepository.findByBuyerId(buyer.getId())
                .map(entity -> CartJpaMapper.toDomain(entity, buyer, loader.loadCartItems(entity.getId())));
    }

    private void persist(Cart cart) {
        cartRepository.save(CartJpaMapper.toEntity(cart));
        cartItemRepository.deleteByCartId(cart.getId());
        for (CartItem item : cart.getItems()) {
            cartItemRepository.save(CartItemJpaMapper.toEntity(item, cart.getId()));
        }
    }
}
