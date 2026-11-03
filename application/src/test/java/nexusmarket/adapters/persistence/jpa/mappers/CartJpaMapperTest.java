package nexusmarket.adapters.persistence.jpa.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import nexusmarket.adapters.persistence.jpa.entities.CartItemJpaEntity;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.CartItem;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.Variant;
import org.junit.jupiter.api.Test;

class CartJpaMapperTest {

    private static final Variant BLACK = new Variant("color", "black");
    private static final Variant RED = new Variant("color", "red");
    private static final BigDecimal PRICE = new BigDecimal("100.00");

    private final Seller seller = DomainFixtures.seller();
    private final PhysicalProduct mouse = DomainFixtures.publishedPhysical(seller, BLACK);

    private CartItemJpaEntity entityOf(Variant variant) {
        return CartItemJpaMapper.toEntity(CartItem.rehydrate(mouse, variant, 3, PRICE), "cart-1");
    }

    @Test
    void itemSurvivesTheRoundTrip() {
        CartItem item = new CartItem(mouse, BLACK, 3, PRICE);

        CartItem restored = CartItemJpaMapper.toDomain(CartItemJpaMapper.toEntity(item, "cart-1"), mouse);

        assertThat(restored).usingRecursiveComparison().isEqualTo(item);
    }

    @Test
    void itemReferencesItsCart() {
        assertThat(entityOf(BLACK).getCartId()).isEqualTo("cart-1");
    }

    @Test
    void itemIsRehydratedWhenItsProductIsNoLongerPublished() {
        PhysicalProduct suspended = DomainFixtures.physicalProduct(seller, ProductStatus.SUSPENDED, BLACK);

        assertThat(CartItemJpaMapper.toDomain(entityOf(BLACK), suspended).getProduct()).isSameAs(suspended);
    }

    @Test
    void itemIsRehydratedWhenItsVariantIsNoLongerOffered() {
        assertThat(CartItemJpaMapper.toDomain(entityOf(RED), mouse).getVariant()).isEqualTo(RED);
    }

    @Test
    void itemWithoutVariantStaysWithoutVariant() {
        assertThat(CartItemJpaMapper.toDomain(entityOf(null), mouse).getVariant()).isNull();
    }

    @Test
    void cartStoresItsBuyerAsAnIdentifier() {
        Buyer buyer = DomainFixtures.buyer();

        assertThat(CartJpaMapper.toEntity(new Cart("cart-1", buyer)).getBuyerId()).isEqualTo(buyer.getId());
    }

    @Test
    void cartIsRestoredWithItsItems() {
        Buyer buyer = DomainFixtures.buyer();
        CartItem item = CartItem.rehydrate(mouse, BLACK, 2, PRICE);

        Cart restored = CartJpaMapper.toDomain(CartJpaMapper.toEntity(new Cart("cart-1", buyer)), buyer, List.of(item));

        assertThat(restored.getItems()).containsExactly(item);
    }

    @Test
    void cartIsRestoredWithAnItemWhoseProductIsNoLongerPublished() {
        Buyer buyer = DomainFixtures.buyer();
        PhysicalProduct suspended = DomainFixtures.physicalProduct(seller, ProductStatus.SUSPENDED);
        CartItem item = CartItem.rehydrate(suspended, null, 2, PRICE);

        Cart restored = CartJpaMapper.toDomain(CartJpaMapper.toEntity(new Cart("cart-1", buyer)), buyer, List.of(item));

        assertThat(restored.getItems()).containsExactly(item);
    }
}
