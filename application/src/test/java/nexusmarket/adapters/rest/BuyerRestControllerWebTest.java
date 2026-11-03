package nexusmarket.adapters.rest;

import static nexusmarket.adapters.rest.RestResponseFixtures.ADDRESS_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.BUYER;
import static nexusmarket.adapters.rest.RestResponseFixtures.BUYER_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.CART;
import static nexusmarket.adapters.rest.RestResponseFixtures.CART_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVOICE;
import static nexusmarket.adapters.rest.RestResponseFixtures.INVOICE_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.ORDER;
import static nexusmarket.adapters.rest.RestResponseFixtures.ORDER_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.PASSWORD_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.REFUND;
import static nexusmarket.adapters.rest.RestResponseFixtures.REFUND_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.REQUEST_ADDRESS;
import static nexusmarket.adapters.rest.RestResponseFixtures.REQUEST_VARIANT;
import static nexusmarket.adapters.rest.RestResponseFixtures.RETURN_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.RETURN_REQUEST;
import static nexusmarket.adapters.rest.RestResponseFixtures.VARIANT_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.userJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.ports.in.BuyerPort;
import org.junit.jupiter.api.Test;

class BuyerRestControllerWebTest extends EndpointWebTestBase<BuyerPort> {

    private static final Buyer USER = DomainFixtures.buyer();
    private static final String OWN_ID = USER.getId();

    private static final EndpointCase.Routes<BuyerPort> AS_BUYER = new EndpointCase.Routes<>(USER);

    @Override
    protected BuyerPort port() {
        return buyerPort;
    }

    static Stream<EndpointCase<BuyerPort>> endpoints() {
        return Stream.of(
                AS_BUYER.get("/api/v1/buyer/account")
                        .stub(port -> when(port.consultUser(any(), any())).thenReturn(USER))
                        .calls(port -> verify(port).consultUser(same(USER), eq(OWN_ID)))
                        .json(userJson(USER)),
                AS_BUYER.put("/api/v1/buyer/account/password")
                        .body(PASSWORD_BODY)
                        .calls(port -> verify(port).changeUserPassword(same(USER), eq("Current123*"), eq("Newpass123*")))
                        .status(204),
                AS_BUYER.get("/api/v1/buyer/profile")
                        .stub(port -> when(port.consultBuyer(any(), any())).thenReturn(BUYER))
                        .calls(port -> verify(port).consultBuyer(same(USER), eq(OWN_ID)))
                        .json(BUYER_JSON),
                AS_BUYER.put("/api/v1/buyer/profile")
                        .body("""
                                {"fullName": "New Name", "primaryAddress": %s, "additionalAddresses": [%s]}"""
                                .formatted(ADDRESS_BODY, ADDRESS_BODY))
                        .stub(port -> when(port.updateBuyerProfile(any(), any(), any(), any())).thenReturn(BUYER))
                        .calls(port -> verify(port).updateBuyerProfile(same(USER), eq("New Name"), eq(REQUEST_ADDRESS),
                                eq(List.of(REQUEST_ADDRESS))))
                        .json(BUYER_JSON),
                AS_BUYER.get("/api/v1/buyer/cart")
                        .stub(port -> when(port.consultCart(any(), any())).thenReturn(CART))
                        .calls(port -> verify(port).consultCart(same(USER), eq(OWN_ID)))
                        .json(CART_JSON),
                AS_BUYER.post("/api/v1/buyer/cart/items")
                        .body("{\"productId\": \"p-1\", \"variant\": " + VARIANT_BODY + ", \"quantity\": 3}")
                        .stub(port -> when(port.addItemToCart(any(), any(), any(), anyInt())).thenReturn(CART))
                        .calls(port -> verify(port).addItemToCart(same(USER), eq("p-1"), eq(REQUEST_VARIANT), eq(3)))
                        .json(CART_JSON),
                AS_BUYER.patch("/api/v1/buyer/cart/items/{productId}", "p-1")
                        .body("{\"variant\": " + VARIANT_BODY + ", \"quantity\": 3}")
                        .stub(port -> when(port.updateCartItemQuantity(any(), any(), any(), anyInt())).thenReturn(CART))
                        .calls(port -> verify(port).updateCartItemQuantity(same(USER), eq("p-1"), eq(REQUEST_VARIANT),
                                eq(3)))
                        .json(CART_JSON),
                removeItem().param("variantType", "Color").param("variantValue", "Black")
                        .calls(port -> verify(port).removeItemFromCart(same(USER), eq("p-1"), eq(REQUEST_VARIANT))),
                AS_BUYER.delete("/api/v1/buyer/cart/items")
                        .stub(port -> when(port.clearCart(any())).thenReturn(CART))
                        .calls(port -> verify(port).clearCart(same(USER)))
                        .json(CART_JSON),
                AS_BUYER.post("/api/v1/buyer/orders")
                        .stub(port -> when(port.confirmOrder(any())).thenReturn(ORDER))
                        .calls(port -> verify(port).confirmOrder(same(USER)))
                        .status(201)
                        .json(ORDER_JSON),
                AS_BUYER.post("/api/v1/buyer/orders/{orderId}/payment", "o-1")
                        .stub(port -> when(port.validatePayment(any(), any())).thenReturn(ORDER))
                        .calls(port -> verify(port).validatePayment(same(USER), eq("o-1")))
                        .json(ORDER_JSON),
                AS_BUYER.get("/api/v1/buyer/orders")
                        .stub(port -> when(port.consultBuyerOrders(any(), any())).thenReturn(List.of(ORDER)))
                        .calls(port -> verify(port).consultBuyerOrders(same(USER), eq(OWN_ID)))
                        .jsonList(ORDER_JSON),
                AS_BUYER.get("/api/v1/buyer/orders/{orderId}", "o-1")
                        .stub(port -> when(port.consultOrder(any(), any())).thenReturn(ORDER))
                        .calls(port -> verify(port).consultOrder(same(USER), eq("o-1")))
                        .json(ORDER_JSON),
                AS_BUYER.get("/api/v1/buyer/orders/{orderId}/invoice", "o-1")
                        .stub(port -> when(port.consultInvoice(any(), any())).thenReturn(INVOICE))
                        .calls(port -> verify(port).consultInvoice(same(USER), eq("o-1")))
                        .json(INVOICE_JSON),
                AS_BUYER.post("/api/v1/buyer/orders/{orderId}/returns", "o-1")
                        .body("{\"reason\": \"Wrong color\"}")
                        .stub(port -> when(port.requestReturn(any(), any(), any())).thenReturn(RETURN_REQUEST))
                        .calls(port -> verify(port).requestReturn(same(USER), eq("o-1"), eq("Wrong color")))
                        .status(201)
                        .json(RETURN_JSON),
                AS_BUYER.get("/api/v1/buyer/returns/{returnRequestId}", "r-1")
                        .stub(port -> when(port.consultReturn(any(), any())).thenReturn(RETURN_REQUEST))
                        .calls(port -> verify(port).consultReturn(same(USER), eq("r-1")))
                        .json(RETURN_JSON),
                AS_BUYER.get("/api/v1/buyer/returns/{returnRequestId}/refund", "r-1")
                        .stub(port -> when(port.consultRefund(any(), any())).thenReturn(REFUND))
                        .calls(port -> verify(port).consultRefund(same(USER), eq("r-1")))
                        .json(REFUND_JSON));
    }

    private static EndpointCase<BuyerPort> removeItem() {
        return AS_BUYER.delete("/api/v1/buyer/cart/items/{productId}", "p-1")
                .stub(port -> when(port.removeItemFromCart(any(), any(), any())).thenReturn(CART))
                .json(CART_JSON);
    }

    /** Second form of the same route, so it stays out of the endpoint table. */
    @Test
    void removingAnItemWithoutVariantParametersPassesNoVariant() throws Exception {
        delegatesToThePortWithExactArguments(
                removeItem().calls(port -> verify(port).removeItemFromCart(same(USER), eq("p-1"), isNull())));
    }
}
