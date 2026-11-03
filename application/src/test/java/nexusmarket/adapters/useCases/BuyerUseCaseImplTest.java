package nexusmarket.adapters.useCases;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.services.support.FakePasswordService;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import org.junit.jupiter.api.Test;

class BuyerUseCaseImplTest {

    private final ServiceHarness h = new ServiceHarness();
    private final BuyerUseCaseImpl useCase = UseCaseFactory.buyer(h);
    private final Seller seller = h.seller();
    private final PhysicalProduct mouse = h.physicalProduct(seller, "100.00");
    private final Buyer buyer = h.buyer();

    BuyerUseCaseImplTest() {
        h.stock(mouse, h.firstWarehouse(seller), 10);
    }

    private ReturnRequest requestedReturn() {
        Order delivered = h.deliveredOrder(buyer, mouse, 1, h.operator());
        return h.requestReturn.requestReturn(buyer, delivered.getId(), "Wrong color");
    }

    @Test
    void consultUserReturnsTheOwnUser() {
        assertThat(useCase.consultUser(buyer, buyer.getId())).isSameAs(buyer);
    }

    @Test
    void changeUserPasswordStoresTheNewHash() {
        Buyer registered = h.registerBuyerUser.registerBuyerUser("1030", "Maria", "maria@gmail.com", "Current123*", ADDRESS);

        useCase.changeUserPassword(registered, "Current123*", "Newpass123*");

        assertThat(registered.getPassword()).isEqualTo(FakePasswordService.hash("Newpass123*"));
    }

    @Test
    void consultBuyerReturnsTheOwnProfile() {
        assertThat(useCase.consultBuyer(buyer, buyer.getId())).isSameAs(buyer);
    }

    @Test
    void updateBuyerProfileChangesTheName() {
        useCase.updateBuyerProfile(buyer, "New Name", ADDRESS, List.of());

        assertThat(buyer.getFullName()).isEqualTo("New Name");
    }

    @Test
    void addItemToCartAddsTheLine() {
        Cart cart = useCase.addItemToCart(buyer, mouse.getId(), null, 2);

        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    void removeItemFromCartRemovesTheLine() {
        h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 2);

        Cart cart = useCase.removeItemFromCart(buyer, mouse.getId(), null);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void updateCartItemQuantityChangesTheQuantity() {
        h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 2);

        Cart cart = useCase.updateCartItemQuantity(buyer, mouse.getId(), null, 5);

        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void consultCartReturnsTheOwnCart() {
        h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 2);

        assertThat(useCase.consultCart(buyer, buyer.getId()).getItems()).hasSize(1);
    }

    @Test
    void clearCartIsAudited() {
        h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 2);

        useCase.clearCart(buyer);

        assertThat(h.audit.actions()).contains(AuditAction.CART_CLEARED);
    }

    @Test
    void confirmOrderStoresTheOrder() {
        h.addItemToCart.addItemToCart(buyer, mouse.getId(), null, 2);

        Order order = useCase.confirmOrder(buyer);

        assertThat(h.orders.findById(order.getId())).contains(order);
    }

    @Test
    void validatePaymentProcessesTheOrderThroughTheGateway() {
        Order order = h.confirmedOrder(buyer, mouse, 1);

        useCase.validatePayment(buyer, order.getId());

        assertThat(h.payments.processedOrders()).containsExactly(order.getId());
    }

    @Test
    void consultOrderReturnsTheOrder() {
        Order order = h.confirmedOrder(buyer, mouse, 1);

        assertThat(useCase.consultOrder(buyer, order.getId())).isSameAs(order);
    }

    @Test
    void consultBuyerOrdersReturnsTheOwnOrders() {
        Order order = h.confirmedOrder(buyer, mouse, 1);

        assertThat(useCase.consultBuyerOrders(buyer, buyer.getId())).containsExactly(order);
    }

    @Test
    void consultInvoiceReturnsTheInvoiceOfThePaidOrder() {
        Order order = h.paidOrder(buyer, mouse, 1);

        assertThat(useCase.consultInvoice(buyer, order.getId()).getOrder()).isSameAs(order);
    }

    @Test
    void requestReturnStoresTheRequest() {
        Order delivered = h.deliveredOrder(buyer, mouse, 1, h.operator());

        ReturnRequest returnRequest = useCase.requestReturn(buyer, delivered.getId(), "Wrong color");

        assertThat(h.returnRequests.findById(returnRequest.getId())).contains(returnRequest);
    }

    @Test
    void consultReturnReturnsTheRequest() {
        ReturnRequest returnRequest = requestedReturn();

        assertThat(useCase.consultReturn(buyer, returnRequest.getId())).isSameAs(returnRequest);
    }

    @Test
    void consultRefundReturnsTheRefundOfTheApprovedReturn() {
        ReturnRequest returnRequest = requestedReturn();
        Administrator admin = h.admin();
        h.approveReturn.approveReturn(admin, returnRequest.getId());

        assertThat(useCase.consultRefund(buyer, returnRequest.getId()).getReturnRequest()).isSameAs(returnRequest);
    }
}
