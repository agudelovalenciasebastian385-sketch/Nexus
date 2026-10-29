package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.physicalProduct;
import static nexusmarket.domain.DomainFixtures.publishedDigital;
import static nexusmarket.domain.DomainFixtures.seller;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import nexusmarket.domain.exceptions.InvalidProductException;
import nexusmarket.domain.exceptions.InvalidProductStatusException;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.Variant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ProductTest {

    private static final Variant BLACK = new Variant("color", "black");

    private final Seller seller = seller();

    private Product product(ProductStatus status) {
        return physicalProduct(seller, status);
    }

    // ------------------------------------------------------------------ allowed transitions

    @Test
    void draftProductCanBePublished() {
        Product product = product(ProductStatus.DRAFT);

        product.publish();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
    }

    @Test
    void publishedProductCanBeSuspended() {
        Product product = product(ProductStatus.PUBLISHED);

        product.suspend();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.SUSPENDED);
    }

    @Test
    void suspendedProductCanBePublishedAgain() {
        Product product = product(ProductStatus.SUSPENDED);

        product.publish();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.PUBLISHED);
    }

    @ParameterizedTest
    @EnumSource(value = ProductStatus.class, names = {"DRAFT", "PUBLISHED", "SUSPENDED"})
    void productThatIsNotDiscontinuedCanBeDiscontinued(ProductStatus status) {
        Product product = product(status);

        product.discontinue();

        assertThat(product.getStatus()).isEqualTo(ProductStatus.DISCONTINUED);
    }

    // ------------------------------------------------------------------ forbidden transitions

    @ParameterizedTest
    @EnumSource(value = ProductStatus.class, names = {"PUBLISHED", "DISCONTINUED"})
    void publishingAProductThatIsNotDraftOrSuspendedIsRejected(ProductStatus status) {
        Product product = product(status);

        assertThatThrownBy(product::publish).isInstanceOf(InvalidProductStatusException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ProductStatus.class, names = {"DRAFT", "SUSPENDED", "DISCONTINUED"})
    void suspendingAProductThatIsNotPublishedIsRejected(ProductStatus status) {
        Product product = product(status);

        assertThatThrownBy(product::suspend).isInstanceOf(InvalidProductStatusException.class);
    }

    @Test
    void discontinuingTwiceIsRejected() {
        Product product = product(ProductStatus.DISCONTINUED);

        assertThatThrownBy(product::discontinue).isInstanceOf(InvalidProductStatusException.class);
    }

    // ------------------------------------------------------------------ data

    @Test
    void duplicateVariantsAreRejected() {
        assertThatThrownBy(() -> physicalProduct(seller, ProductStatus.DRAFT, BLACK, new Variant("color", "black")))
                .isInstanceOf(InvalidProductException.class);
    }

    @Test
    void negativePriceIsRejected() {
        assertThatThrownBy(() -> new PhysicalProduct("p-1", "Mouse", "Mouse", new BigDecimal("-0.01"), seller, List.of(),
                ProductStatus.DRAFT)).isInstanceOf(InvalidProductException.class);
    }

    @Test
    void sellerIsRequired() {
        assertThatThrownBy(() -> new PhysicalProduct("p-1", "Mouse", "Mouse", BigDecimal.ONE, null, List.of(),
                ProductStatus.DRAFT)).isInstanceOf(InvalidProductException.class);
    }

    @Test
    void updateInfoReplacesTheVariants() {
        Product product = physicalProduct(seller, ProductStatus.DRAFT, BLACK);
        Variant red = new Variant("color", "red");

        product.updateInfo("Mouse 2", "New description", BigDecimal.TEN, List.of(red));

        assertThat(product.getVariants()).containsExactly(red);
    }

    @Test
    void physicalProductReportsPhysicalType() {
        assertThat(product(ProductStatus.DRAFT).getType()).isEqualTo(ProductType.PHYSICAL);
    }

    @Test
    void digitalProductReportsDigitalType() {
        assertThat(publishedDigital(seller).getType()).isEqualTo(ProductType.DIGITAL);
    }
}
