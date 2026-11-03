package nexusmarket.adapters.persistence.jpa.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.DigitalProduct;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.Variant;
import org.junit.jupiter.api.Test;

class ProductJpaMapperTest {

    private static final Variant BLACK = new Variant("color", "black");
    private static final Variant WHITE = new Variant("color", "white");

    private final Seller seller = DomainFixtures.seller();

    private Product roundTrip(Product product) {
        return ProductJpaMapper.toDomain(ProductJpaMapper.toEntity(product), seller);
    }

    private PhysicalProduct physical() {
        return new PhysicalProduct("p-1", "Mouse", "Wireless mouse", new BigDecimal("89900.50"), seller,
                List.of(BLACK, WHITE), ProductStatus.SUSPENDED);
    }

    @Test
    void physicalProductComesBackAsPhysical() {
        assertThat(roundTrip(physical())).isInstanceOf(PhysicalProduct.class);
    }

    @Test
    void digitalProductComesBackAsDigital() {
        DigitalProduct digital = new DigitalProduct("p-2", "E-book", "Guide", BigDecimal.TEN, seller, List.of(),
                ProductStatus.DRAFT);

        assertThat(roundTrip(digital)).isInstanceOf(DigitalProduct.class);
    }

    @Test
    void dataSurvivesTheRoundTrip() {
        assertThat(roundTrip(physical()))
                .usingRecursiveComparison()
                .comparingOnlyFields("id", "name", "description", "price", "status")
                .isEqualTo(physical());
    }

    @Test
    void variantsSurviveInOrder() {
        assertThat(roundTrip(physical()).getVariants()).containsExactly(BLACK, WHITE);
    }

    @Test
    void sellerIsStoredAsAnIdentifier() {
        assertThat(ProductJpaMapper.toEntity(physical()).getSellerId()).isEqualTo(seller.getId());
    }

    @Test
    void productTypeIsStoredAsItsCode() {
        assertThat(ProductJpaMapper.toEntity(physical()).getProductType()).isEqualTo("PHYSICAL");
    }
}
