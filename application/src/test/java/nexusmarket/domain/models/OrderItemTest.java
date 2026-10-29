package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.publishedDigital;
import static nexusmarket.domain.DomainFixtures.publishedPhysical;
import static nexusmarket.domain.DomainFixtures.seller;
import static nexusmarket.domain.DomainFixtures.sellerWarehouse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import nexusmarket.domain.exceptions.InvalidOrderException;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    private final Seller seller = seller();

    @Test
    void physicalItemWithoutOriginWarehouseIsRejected() {
        PhysicalProduct product = publishedPhysical(seller);

        assertThatThrownBy(() -> new OrderItem(product, null, 1, product.getPrice(), null))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void digitalItemWithOriginWarehouseIsRejected() {
        DigitalProduct product = publishedDigital(seller);

        assertThatThrownBy(() -> new OrderItem(product, null, 1, product.getPrice(), sellerWarehouse(seller)))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void nonPositiveQuantityIsRejected() {
        DigitalProduct product = publishedDigital(seller);

        assertThatThrownBy(() -> new OrderItem(product, null, 0, product.getPrice(), null))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void subtotalIsUnitPriceTimesQuantity() {
        DigitalProduct product = publishedDigital(seller);

        OrderItem item = new OrderItem(product, null, 3, new BigDecimal("50.00"), null);

        assertThat(item.subtotal()).isEqualByComparingTo(new BigDecimal("150.00"));
    }
}
