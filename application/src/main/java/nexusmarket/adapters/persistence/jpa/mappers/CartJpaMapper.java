package nexusmarket.adapters.persistence.jpa.mappers;

import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.CartJpaEntity;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.CartItem;

/**
 * Bidirectional mapper between the Cart Domain Model and its JPA entity.
 *
 * <p>The buyer is stored as a reference identifier; the resolved Buyer and the items are
 * supplied by the adapter when reading.
 */
public final class CartJpaMapper {

    private CartJpaMapper() {
    }

    public static CartJpaEntity toEntity(Cart domain) {
        if (domain == null) {
            return null;
        }
        CartJpaEntity entity = new CartJpaEntity();
        entity.setId(domain.getId());
        entity.setBuyerId(domain.getBuyer().getId());
        return entity;
    }

    public static Cart toDomain(CartJpaEntity entity, Buyer buyer, List<CartItem> items) {
        if (entity == null) {
            return null;
        }
        return new Cart(entity.getId(), buyer, items);
    }
}
