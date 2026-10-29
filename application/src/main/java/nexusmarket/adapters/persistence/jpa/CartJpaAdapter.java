package nexusmarket.adapters.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.entities.CartJpaEntity;
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
 *
 * <p>Optimistic locking: an existing cart is written onto the managed entity loaded at the start of
 * the use case with a forced version increment ({@code OPTIMISTIC_FORCE_INCREMENT}), because its own
 * row never changes (the items live in cart_items). Two concurrent changes of the same cart, or a
 * change racing with ConfirmOrderService, cannot both commit.
 */
@Repository
public class CartJpaAdapter implements CartRepositoryPort {

    private final SpringDataJpaCartRepository cartRepository;
    private final SpringDataJpaCartItemRepository cartItemRepository;
    private final JpaAggregateLoader loader;
    private final EntityManager entityManager;

    public CartJpaAdapter(SpringDataJpaCartRepository cartRepository,
                          SpringDataJpaCartItemRepository cartItemRepository,
                          JpaAggregateLoader loader,
                          EntityManager entityManager) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.loader = loader;
        this.entityManager = entityManager;
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
        Optional<CartJpaEntity> managed = cartRepository.findById(cart.getId());
        if (managed.isPresent()) {
            CartJpaMapper.copyToEntity(cart, managed.get());
            entityManager.lock(managed.get(), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        } else {
            cartRepository.save(CartJpaMapper.toEntity(cart));
        }
        cartItemRepository.deleteByCartId(cart.getId());
        for (CartItem item : cart.getItems()) {
            cartItemRepository.save(CartItemJpaMapper.toEntity(item, cart.getId()));
        }
    }
}
