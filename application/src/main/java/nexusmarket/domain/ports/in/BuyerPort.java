package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.Invoice;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Role input port ({@code BUYER}).
 *
 * <p>Operations of a buyer over its own profile, cart, orders, invoices and return requests.</p>
 */
public interface BuyerPort {

    User consultUser(User requestingUser, String userId);

    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);

    Buyer consultBuyer(User requestingUser, String buyerId);

    Buyer updateBuyerProfile(User requestingUser, String fullName, Address primaryAddress,
                             List<Address> additionalAddresses);

    Cart addItemToCart(User requestingUser, String productId, Variant variant, int quantity);

    Cart removeItemFromCart(User requestingUser, String productId, Variant variant);

    Cart updateCartItemQuantity(User requestingUser, String productId, Variant variant, int quantity);

    Cart consultCart(User requestingUser, String buyerId);

    Cart clearCart(User requestingUser);

    Order confirmOrder(User requestingUser);

    Order validatePayment(User requestingUser, String orderId);

    Order consultOrder(User requestingUser, String orderId);

    List<Order> consultBuyerOrders(User requestingUser, String buyerId);

    Invoice consultInvoice(User requestingUser, String orderId);

    ReturnRequest requestReturn(User requestingUser, String orderId, String reason);

    ReturnRequest consultReturn(User requestingUser, String returnRequestId);

    Refund consultRefund(User requestingUser, String returnRequestId);
}
