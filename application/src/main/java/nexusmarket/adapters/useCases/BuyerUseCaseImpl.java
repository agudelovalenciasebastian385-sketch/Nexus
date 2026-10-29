package nexusmarket.adapters.useCases;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.PaymentRejectedException;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case adapter of {@link BuyerPort}: delegates every operation to the
 * corresponding domain service (Use-cases-adapters.md). It contains no business
 * rules.
 *
 * <p>It is the transaction boundary of every use case: each operation runs in one
 * transaction (all or nothing); queries and reports are read-only.</p>
 */
@Service
@Transactional
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
    @Transactional(readOnly = true)
    public User consultUser(User requestingUser, String userId) {
        return consultUserService.consultUser(requestingUser, userId);
    }

    @Override
    public void changeUserPassword(User requestingUser, String currentPassword, String newPassword) {
        changeUserPasswordService.changeUserPassword(requestingUser, currentPassword, newPassword);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(noRollbackFor = PaymentRejectedException.class)
    public Order validatePayment(User requestingUser, String orderId) {
        return validatePaymentService.validatePayment(requestingUser, orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Order consultOrder(User requestingUser, String orderId) {
        return consultOrderService.consultOrder(requestingUser, orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> consultBuyerOrders(User requestingUser, String buyerId) {
        return consultBuyerOrdersService.consultBuyerOrders(requestingUser, buyerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice consultInvoice(User requestingUser, String orderId) {
        return consultInvoiceService.consultInvoice(requestingUser, orderId);
    }

    @Override
    public ReturnRequest requestReturn(User requestingUser, String orderId, String reason) {
        return requestReturnService.requestReturn(requestingUser, orderId, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public ReturnRequest consultReturn(User requestingUser, String returnRequestId) {
        return consultReturnService.consultReturn(requestingUser, returnRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public Refund consultRefund(User requestingUser, String returnRequestId) {
        return consultReturnService.consultRefund(requestingUser, returnRequestId);
    }
}
