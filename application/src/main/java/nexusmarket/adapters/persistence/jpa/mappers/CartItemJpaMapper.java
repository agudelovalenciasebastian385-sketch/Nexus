package nexusmarket.adapters.persistence.jpa.mappers;

import nexusmarket.adapters.persistence.jpa.entities.CartItemJpaEntity;
import nexusmarket.domain.models.CartItem;
import nexusmarket.domain.models.Product;

/**
 * Bidirectional mapper between the CartItem Domain Model and its JPA entity.
 *
 * <p>The product is stored as a reference identifier; the resolved Product is supplied by the
 * adapter when reading. Items are rebuilt with {@link CartItem#rehydrate}, so an item whose
 * product is no longer published (or no longer offers the variant) is kept in the cart.
 */
public final class CartItemJpaMapper {

    private CartItemJpaMapper() {
    }

    public static CartItemJpaEntity toEntity(CartItem domain, String cartId) {
        if (domain == null) {
            return null;
        }
        CartItemJpaEntity entity = new CartItemJpaEntity();
        entity.setCartId(cartId);
        entity.setProductId(domain.getProduct().getId());
        entity.setVariant(VariantJpaMapper.toEmbeddable(domain.getVariant()));
        entity.setQuantity(domain.getQuantity());
        entity.setUnitPrice(domain.getUnitPrice());
        return entity;
    }

    public static CartItem toDomain(CartItemJpaEntity entity, Product product) {
        if (entity == null) {
            return null;
        }
        return CartItem.rehydrate(product, VariantJpaMapper.toDomain(entity.getVariant()), entity.getQuantity(),
                entity.getUnitPrice());
    }
}
