package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.buyer;
import static nexusmarket.domain.DomainFixtures.publishedPhysical;
import static nexusmarket.domain.DomainFixtures.seller;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import nexusmarket.domain.exceptions.InvalidCartException;
import nexusmarket.domain.valueobjects.Variant;
import org.junit.jupiter.api.Test;

class CartTest {

    private static final Variant BLACK = new Variant("color", "black");
    private static final Variant WHITE = new Variant("color", "white");
    private static final BigDecimal PRICE = new BigDecimal("100.00");

    private final Seller seller = seller();
    private final PhysicalProduct mouse = publishedPhysical(seller, BLACK, WHITE);
    private final Cart cart = new Cart("cart-1", buyer());

    @Test
    void addingAPublishedProductCreatesALine() {
        cart.addItem(mouse, BLACK, 2, PRICE);

        assertThat(cart.getItems()).singleElement().extracting(CartItem::getQuantity).isEqualTo(2);
    }

    @Test
    void addingTheSameProductAndVariantAccumulatesTheQuantity() {
        cart.addItem(mouse, BLACK, 2, PRICE);

        cart.addItem(mouse, BLACK, 3, PRICE);

        assertThat(cart.getItems()).singleElement().extracting(CartItem::getQuantity).isEqualTo(5);
    }

    @Test
    void addingTheSameProductWithAnotherVariantCreatesAnotherLine() {
        cart.addItem(mouse, BLACK, 1, PRICE);

        cart.addItem(mouse, WHITE, 1, PRICE);

        assertThat(cart.getItems()).hasSize(2);
    }

    @Test
    void removingAnItemDeletesItsLine() {
        cart.addItem(mouse, BLACK, 1, PRICE);

        cart.removeItem(mouse, BLACK);

        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void removingAProductThatIsNotInTheCartIsRejected() {
        assertThatThrownBy(() -> cart.removeItem(mouse, BLACK)).isInstanceOf(InvalidCartException.class);
    }

    @Test
    void changingTheQuantityReplacesIt() {
        cart.addItem(mouse, BLACK, 2, PRICE);

        cart.changeItemQuantity(mouse, BLACK, 7);

        assertThat(cart.getItems()).singleElement().extracting(CartItem::getQuantity).isEqualTo(7);
    }

    @Test
    void changingTheQuantityToZeroIsRejected() {
        cart.addItem(mouse, BLACK, 2, PRICE);

        assertThatThrownBy(() -> cart.changeItemQuantity(mouse, BLACK, 0)).isInstanceOf(InvalidCartException.class);
    }

    @Test
    void changingTheQuantityOfAProductThatIsNotInTheCartIsRejected() {
        assertThatThrownBy(() -> cart.changeItemQuantity(mouse, BLACK, 1)).isInstanceOf(InvalidCartException.class);
    }

    @Test
    void clearingEmptiesTheCart() {
        cart.addItem(mouse, BLACK, 2, PRICE);

        cart.clear();

        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void totalAmountIsTheSumOfSubtotals() {
        cart.addItem(mouse, BLACK, 2, PRICE);
        cart.addItem(mouse, WHITE, 1, new BigDecimal("80.00"));

        assertThat(cart.totalAmount()).isEqualByComparingTo(new BigDecimal("280.00"));
    }

    @Test
    void buyerIsRequired() {
        assertThatThrownBy(() -> new Cart("cart-1", null)).isInstanceOf(InvalidCartException.class);
    }
}
