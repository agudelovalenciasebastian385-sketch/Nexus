package nexusmarket.domain.services.cart;

import static nexusmarket.domain.DomainFixtures.buyer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidCartException;
import nexusmarket.domain.exceptions.InvalidProductStatusException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.CartItem;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.Variant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CartServicesTest {

    private static final Variant BLACK = new Variant("color", "black");

    private final ServiceHarness h = new ServiceHarness();
    private final Seller seller = h.seller();
    private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00", BLACK);
    private final Buyer buyer = h.buyer();

    @Nested
    class AddItem {

        @Test
        void addingAPublishedProductStoresALineAtItsCurrentPrice() {
            Cart cart = h.addItemToCart.addItemToCart(buyer, mouse.getId(), BLACK, 2);

            assertThat(cart.getItems()).singleElement().satisfies(item -> {
                assertThat(item.getQuantity()).isEqualTo(2);
                assertThat(item.getUnitPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
            });
        }

        @Test
        void addingAnItemIsAudited() {
            h.addItemToCart.addItemToCart(buyer, mouse.getId(), BLACK, 2);

            assertThat(h.audit.last(AuditAction.CART_ITEM_ADDED).getDetails()).containsEntry("quantity", "2");
        }

        @Test
        void addingAnUnpublishedProductIsRejected() {
            PhysicalProduct draft = h.productWithStatus(seller, ProductStatus.DRAFT);

            assertThatThrownBy(() -> h.addItemToCart.addItemToCart(buyer, draft.getId(), null, 1))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        void addingAVariantNotOfferedIsRejected() {
            assertThatThrownBy(() -> h.addItemToCart.addItemToCart(buyer, mouse.getId(), new Variant("color", "red"), 1))
                    .isInstanceOf(InvalidCartException.class);
        }

        @Test
        void addingAnUnknownProductIsReported() {
            assertThatThrownBy(() -> h.addItemToCart.addItemToCart(buyer, "missing", null, 1))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void sellerCannotUseACart() {
            assertThatThrownBy(() -> h.addItemToCart.addItemToCart(seller, mouse.getId(), BLACK, 1))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void blockedBuyerCannotUseItsCart() {
            h.blocked(buyer);

            assertThatThrownBy(() -> h.addItemToCart.addItemToCart(buyer, mouse.getId(), BLACK, 1))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void buyerWithoutCartIsReported() {
            Buyer withoutCart = h.buyers.save(buyer());

            assertThatThrownBy(() -> h.addItemToCart.addItemToCart(withoutCart, mouse.getId(), BLACK, 1))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ChangeItems {

        @Test
        void removingAnItemDeletesItsLineAndIsAudited() {
            h.addItemToCart.addItemToCart(buyer, mouse.getId(), BLACK, 2);

            Cart cart = h.removeItemFromCart.removeItemFromCart(buyer, mouse.getId(), BLACK);

            assertThat(cart.isEmpty()).isTrue();
            assertThat(h.audit.actions()).contains(AuditAction.CART_ITEM_REMOVED);
        }

        @Test
        void removingAProductThatIsNotInTheCartIsRejected() {
            assertThatThrownBy(() -> h.removeItemFromCart.removeItemFromCart(buyer, mouse.getId(), BLACK))
                    .isInstanceOf(InvalidCartException.class);
        }

        @Test
        void removingAnUnknownProductIsReported() {
            assertThatThrownBy(() -> h.removeItemFromCart.removeItemFromCart(buyer, "missing", null))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void changingTheQuantityReplacesItAndIsAudited() {
            h.addItemToCart.addItemToCart(buyer, mouse.getId(), BLACK, 2);

            Cart cart = h.updateCartItemQuantity.updateCartItemQuantity(buyer, mouse.getId(), BLACK, 5);

            assertThat(cart.getItems()).extracting(CartItem::getQuantity).containsExactly(5);
            assertThat(h.audit.actions()).contains(AuditAction.CART_ITEM_QUANTITY_UPDATED);
        }

        @Test
        void changingTheQuantityToZeroIsRejected() {
            h.addItemToCart.addItemToCart(buyer, mouse.getId(), BLACK, 2);

            assertThatThrownBy(() -> h.updateCartItemQuantity.updateCartItemQuantity(buyer, mouse.getId(), BLACK, 0))
                    .isInstanceOf(InvalidCartException.class);
        }

        @Test
        void changingTheQuantityOfAnUnknownProductIsReported() {
            assertThatThrownBy(() -> h.updateCartItemQuantity.updateCartItemQuantity(buyer, "missing", null, 1))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void clearingEmptiesTheCartAndIsAudited() {
            h.addItemToCart.addItemToCart(buyer, mouse.getId(), BLACK, 2);

            Cart cart = h.clearCart.clearCart(buyer);

            assertThat(cart.isEmpty()).isTrue();
            assertThat(h.audit.actions()).contains(AuditAction.CART_CLEARED);
        }

        @Test
        void sellerCannotClearACart() {
            assertThatThrownBy(() -> h.clearCart.clearCart(seller)).isInstanceOf(UnauthorizedOperationException.class);
        }
    }

    @Nested
    class ConsultCart {

        @Test
        void buyerConsultsItsOwnCart() {
            h.addItemToCart.addItemToCart(buyer, mouse.getId(), BLACK, 2);

            assertThat(h.consultCart.consultCart(buyer, buyer.getId()).getItems()).hasSize(1);
        }

        @Test
        void buyerCannotConsultAnotherBuyersCart() {
            assertThatThrownBy(() -> h.consultCart.consultCart(h.buyer(), buyer.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void supervisorConsultsAnyCart() {
            assertThat(h.consultCart.consultCart(h.supervisor(), buyer.getId()).getBuyer()).isEqualTo(buyer);
        }

        @Test
        void unknownBuyerIsReported() {
            assertThatThrownBy(() -> h.consultCart.consultCart(h.supervisor(), "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void missingCartIsReported() {
            Buyer withoutCart = h.buyers.save(buyer());

            assertThatThrownBy(() -> h.consultCart.consultCart(withoutCart, withoutCart.getId()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
