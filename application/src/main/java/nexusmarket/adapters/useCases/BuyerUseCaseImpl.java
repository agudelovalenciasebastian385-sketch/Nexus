package nexusmarket.adapters.useCases;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.BuyerPort;
import nexusmarket.domain.services.buyer.ConsultBuyerService;
import nexusmarket.domain.services.buyer.UpdateBuyerProfileService;
import nexusmarket.domain.services.cart.AddItemToCartService;
import nexusmarket.domain.services.cart.ClearCartService;
import nexusmarket.domain.services.cart.ConsultCartService;
import nexusmarket.domain.services.cart.RemoveItemFromCartService;
import nexusmarket.domain.services.cart.UpdateCartItemQuantityService;
import nexusmarket.domain.services.invoice.ConsultInvoiceService;
import nexusmarket.domain.services.order.ConfirmOrderService;
import nexusmarket.domain.services.order.ConsultBuyerOrdersService;
import nexusmarket.domain.services.order.ConsultOrderService;
import nexusmarket.domain.services.order.ValidatePaymentService;
import nexusmarket.domain.services.returns.ConsultReturnService;
import nexusmarket.domain.services.returns.RequestReturnService;
import nexusmarket.domain.services.user.ChangeUserPasswordService;
import nexusmarket.domain.services.user.ConsultUserService;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.Variant;
import org.springframework.stereotype.Service;

/**
 * Use case adapter of {@link BuyerPort}: delegates every operation to the
 * corresponding domain service (Use-cases-adapters.md). It contains no business
 * rules.
 */
@Service
@RequiredArgsConstructor
public class BuyerUseCaseImpl implements BuyerPort {

    private final ConsultUserService consultUserService;
    private final ChangeUserPasswordService changeUserPasswordService;
    private final ConsultBuyerService consultBuyerService;
    private final UpdateBuyerProfileService updateBuyerProfileService;
    private final AddItemToCartService addItemToCartService;
    private final RemoveItemFromCartService removeItemFromCartService;
    private final UpdateCartItemQuantityService updateCartItemQuantityService;
    private final ConsultCartService consultCartService;
    private final ClearCartService clearCartService;
    private final ConfirmOrderService confirmOrderService;
    private final ValidatePaymentService validatePaymentService;
    private final ConsultOrderService consultOrderService;
    private final ConsultBuyerOrdersService consultBuyerOrdersService;
    private final ConsultInvoiceService consultInvoiceService;
    private final RequestReturnService requestReturnService;
    private final ConsultReturnService consultReturnService;

    @Override
    public User consultUser(User requestingUser, String userId) {
        return consultUserService.consultUser(requestingUser, userId);
    }

    @Override
    public void changeUserPassword(User requestingUser, String currentPassword, String newPassword) {
        changeUserPasswordService.changeUserPassword(requestingUser, currentPassword, newPassword);
    }

    @Override
    public Buyer consultBuyer(User requestingUser, String buyerId) {
        return consultBuyerService.consultBuyer(requestingUser, buyerId);
    }

    @Override
    public Buyer updateBuyerProfile(User requestingUser, String fullName, Address primaryAddress,
                                    List<Address> additionalAddresses) {
        return updateBuyerProfileService.updateBuyerProfile(requestingUser, fullName, primaryAddress, additionalAddresses);
    }

    @Override
    public Cart addItemToCart(User requestingUser, String productId, Variant variant, int quantity) {
        return addItemToCartService.addItemToCart(requestingUser, productId, variant, quantity);
    }

    @Override
    public Cart removeItemFromCart(User requestingUser, String productId, Variant variant) {
        return removeItemFromCartService.removeItemFromCart(requestingUser, productId, variant);
    }

    @Override
    public Cart updateCartItemQuantity(User requestingUser, String productId, Variant variant, int quantity) {
        return updateCartItemQuantityService.updateCartItemQuantity(requestingUser, productId, variant, quantity);
    }

    @Override
    public Cart consultCart(User requestingUser, String buyerId) {
        return consultCartService.consultCart(requestingUser, buyerId);
    }

    @Override
    public Cart clearCart(User requestingUser) {
        return clearCartService.clearCart(requestingUser);
    }

    @Override
    public Order confirmOrder(User requestingUser) {
        return confirmOrderService.confirmOrder(requestingUser);
    }

    @Override
    public Order validatePayment(User requestingUser, String orderId) {
        return validatePaymentService.validatePayment(requestingUser, orderId);
    }

    @Override
    public Order consultOrder(User requestingUser, String orderId) {
        return consultOrderService.consultOrder(requestingUser, orderId);
    }

    @Override
    public List<Order> consultBuyerOrders(User requestingUser, String buyerId) {
        return consultBuyerOrdersService.consultBuyerOrders(requestingUser, buyerId);
    }

    @Override
    public Invoice consultInvoice(User requestingUser, String orderId) {
        return consultInvoiceService.consultInvoice(requestingUser, orderId);
    }

    @Override
    public ReturnRequest requestReturn(User requestingUser, String orderId, String reason) {
        return requestReturnService.requestReturn(requestingUser, orderId, reason);
    }

    @Override
    public ReturnRequest consultReturn(User requestingUser, String returnRequestId) {
        return consultReturnService.consultReturn(requestingUser, returnRequestId);
    }

    @Override
    public Refund consultRefund(User requestingUser, String returnRequestId) {
        return consultReturnService.consultRefund(requestingUser, returnRequestId);
    }
}
