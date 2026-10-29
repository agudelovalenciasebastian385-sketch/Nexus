package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.physicalProduct;
import static nexusmarket.domain.DomainFixtures.publishedPhysical;
import static nexusmarket.domain.DomainFixtures.seller;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import nexusmarket.domain.exceptions.InvalidCartException;
import nexusmarket.domain.exceptions.InvalidProductStatusException;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CartItemTest {

    private static final Variant BLACK = new Variant("color", "black");
    private static final Variant RED = new Variant("color", "red");
    private static final BigDecimal PRICE = new BigDecimal("100.00");

    private final Seller seller = seller();

    @ParameterizedTest
    @EnumSource(value = ProductStatus.class, names = {"DRAFT", "SUSPENDED", "DISCONTINUED"})
    void productThatIsNotPublishedIsRejected(ProductStatus status) {
        PhysicalProduct product = physicalProduct(seller, status);

        assertThatThrownBy(() -> new CartItem(product, null, 1, PRICE))
                .isInstanceOf(InvalidProductStatusException.class);
    }

    @Test
    void variantNotOfferedByTheProductIsRejected() {
        PhysicalProduct product = publishedPhysical(seller, BLACK);

        assertThatThrownBy(() -> new CartItem(product, RED, 1, PRICE)).isInstanceOf(InvalidCartException.class);
    }

    @Test
    void nonPositiveQuantityIsRejected() {
        PhysicalProduct product = publishedPhysical(seller);

        assertThatThrownBy(() -> new CartItem(product, null, 0, PRICE)).isInstanceOf(InvalidCartException.class);
    }

    @Test
    void negativeUnitPriceIsRejected() {
        PhysicalProduct product = publishedPhysical(seller);

        assertThatThrownBy(() -> new CartItem(product, null, 1, new BigDecimal("-1")))
                .isInstanceOf(InvalidCartException.class);
    }

    @Test
    void rehydrateAcceptsAProductThatIsNoLongerPublished() {
        PhysicalProduct suspended = physicalProduct(seller, ProductStatus.SUSPENDED);

        CartItem item = CartItem.rehydrate(suspended, null, 2, PRICE);

        assertThat(item.getProduct()).isSameAs(suspended);
    }

    @Test
    void rehydrateAcceptsAVariantNoLongerOffered() {
        PhysicalProduct product = publishedPhysical(seller, BLACK);

        CartItem item = CartItem.rehydrate(product, RED, 1, PRICE);

        assertThat(item.getVariant()).isEqualTo(RED);
    }

    @Test
    void rehydrateStillRejectsNonPositiveQuantity() {
        PhysicalProduct product = publishedPhysical(seller);

        assertThatThrownBy(() -> CartItem.rehydrate(product, null, 0, PRICE)).isInstanceOf(InvalidCartException.class);
    }

    @Test
    void rehydrateStillRejectsNegativeUnitPrice() {
        PhysicalProduct product = publishedPhysical(seller);

        assertThatThrownBy(() -> CartItem.rehydrate(product, null, 1, new BigDecimal("-1")))
                .isInstanceOf(InvalidCartException.class);
    }

    @Test
    void subtotalIsUnitPriceTimesQuantity() {
        CartItem item = new CartItem(publishedPhysical(seller), null, 3, PRICE);

        assertThat(item.subtotal()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void itemMatchesItsProductAndVariantOnly() {
        PhysicalProduct product = publishedPhysical(seller, BLACK, RED);
        CartItem item = new CartItem(product, BLACK, 1, PRICE);

        assertThat(item.matches(product, BLACK)).isTrue();
        assertThat(item.matches(product, RED)).isFalse();
    }
}
