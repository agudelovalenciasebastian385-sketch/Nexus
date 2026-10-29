package nexusmarket.adapters.rest.controllers;

import jakarta.validation.Valid;
import java.util.List;
import nexusmarket.adapters.rest.dtos.requests.AddCartItemRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.ChangePasswordRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.RequestReturnRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.UpdateBuyerProfileRequestDTO;
import nexusmarket.adapters.rest.dtos.requests.UpdateCartItemQuantityRequestDTO;
import nexusmarket.adapters.rest.dtos.responses.BuyerResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.CartResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.InvoiceResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.OrderResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.RefundResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.ReturnRequestResponseDTO;
import nexusmarket.adapters.rest.dtos.responses.UserResponseDTO;
import nexusmarket.adapters.rest.mappers.AddressRestMapper;
import nexusmarket.adapters.rest.mappers.BuyerRestMapper;
import nexusmarket.adapters.rest.mappers.CartRestMapper;
import nexusmarket.adapters.rest.mappers.InvoiceRestMapper;
import nexusmarket.adapters.rest.mappers.OrderRestMapper;
import nexusmarket.adapters.rest.mappers.ReturnRestMapper;
import nexusmarket.adapters.rest.mappers.UserRestMapper;
import nexusmarket.adapters.rest.mappers.VariantRestMapper;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.BuyerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Buyer endpoints (BuyerPort, role BUYER). Own resources (account, profile, cart, orders list) take
 * no id in the path: the controller passes the authenticated user's id and the services still
 * validate ownership (RG-03). Transactions are opened by BuyerUseCaseImpl.
 */
@RestController
@RequestMapping("/api/v1/buyer")
public class BuyerRestController {

    private final BuyerPort buyerPort;

    public BuyerRestController(BuyerPort buyerPort) {
        this.buyerPort = buyerPort;
    }

    // ------------------------------------------------------------------ account and profile

    @GetMapping("/account")
    public ResponseEntity<UserResponseDTO> consultAccount(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(UserRestMapper.toResponse(buyerPort.consultUser(user, user.getId())));
    }

    @PutMapping("/account/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal(expression = "user") User user,
                                               @Valid @RequestBody ChangePasswordRequestDTO request) {
        buyerPort.changeUserPassword(user, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<BuyerResponseDTO> consultProfile(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(BuyerRestMapper.toResponse(buyerPort.consultBuyer(user, user.getId())));
    }

    @PutMapping("/profile")
    public ResponseEntity<BuyerResponseDTO> updateProfile(@AuthenticationPrincipal(expression = "user") User user,
                                                          @Valid @RequestBody UpdateBuyerProfileRequestDTO request) {
        return ResponseEntity.ok(BuyerRestMapper.toResponse(buyerPort.updateBuyerProfile(user, request.getFullName(),
                AddressRestMapper.toDomain(request.getPrimaryAddress()),
                AddressRestMapper.toDomain(request.getAdditionalAddresses()))));
    }

    // ------------------------------------------------------------------ cart

    @GetMapping("/cart")
    public ResponseEntity<CartResponseDTO> consultCart(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(CartRestMapper.toResponse(buyerPort.consultCart(user, user.getId())));
    }

    @PostMapping("/cart/items")
    public ResponseEntity<CartResponseDTO> addItem(@AuthenticationPrincipal(expression = "user") User user,
                                                   @Valid @RequestBody AddCartItemRequestDTO request) {
        return ResponseEntity.ok(CartRestMapper.toResponse(buyerPort.addItemToCart(user, request.getProductId(),
                VariantRestMapper.toDomain(request.getVariant()), request.getQuantity())));
    }

    @PatchMapping("/cart/items/{productId}")
    public ResponseEntity<CartResponseDTO> updateItemQuantity(@AuthenticationPrincipal(expression = "user") User user,
                                                              @PathVariable String productId,
                                                              @Valid @RequestBody UpdateCartItemQuantityRequestDTO request) {
        return ResponseEntity.ok(CartRestMapper.toResponse(buyerPort.updateCartItemQuantity(user, productId,
                VariantRestMapper.toDomain(request.getVariant()), request.getQuantity())));
    }

    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<CartResponseDTO> removeItem(@AuthenticationPrincipal(expression = "user") User user,
                                                      @PathVariable String productId,
                                                      @RequestParam(required = false) String variantType,
                                                      @RequestParam(required = false) String variantValue) {
        return ResponseEntity.ok(CartRestMapper.toResponse(buyerPort.removeItemFromCart(user, productId,
                VariantRestMapper.fromQuery(variantType, variantValue))));
    }

    @DeleteMapping("/cart/items")
    public ResponseEntity<CartResponseDTO> clearCart(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(CartRestMapper.toResponse(buyerPort.clearCart(user)));
    }

    // ------------------------------------------------------------------ orders

    @PostMapping("/orders")
    public ResponseEntity<OrderResponseDTO> confirmOrder(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderRestMapper.toResponse(buyerPort.confirmOrder(user)));
    }

    @PostMapping("/orders/{orderId}/payment")
    public ResponseEntity<OrderResponseDTO> payOrder(@AuthenticationPrincipal(expression = "user") User user,
                                                     @PathVariable String orderId) {
        return ResponseEntity.ok(OrderRestMapper.toResponse(buyerPort.validatePayment(user, orderId)));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponseDTO>> consultOrders(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(OrderRestMapper.toResponse(buyerPort.consultBuyerOrders(user, user.getId())));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> consultOrder(@AuthenticationPrincipal(expression = "user") User user,
                                                         @PathVariable String orderId) {
        return ResponseEntity.ok(OrderRestMapper.toResponse(buyerPort.consultOrder(user, orderId)));
    }

    @GetMapping("/orders/{orderId}/invoice")
    public ResponseEntity<InvoiceResponseDTO> consultInvoice(@AuthenticationPrincipal(expression = "user") User user,
                                                             @PathVariable String orderId) {
        return ResponseEntity.ok(InvoiceRestMapper.toResponse(buyerPort.consultInvoice(user, orderId)));
    }

    // ------------------------------------------------------------------ returns

    @PostMapping("/orders/{orderId}/returns")
    public ResponseEntity<ReturnRequestResponseDTO> requestReturn(@AuthenticationPrincipal(expression = "user") User user,
                                                                  @PathVariable String orderId,
                                                                  @Valid @RequestBody RequestReturnRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReturnRestMapper.toResponse(buyerPort.requestReturn(user, orderId, request.getReason())));
    }

    @GetMapping("/returns/{returnRequestId}")
    public ResponseEntity<ReturnRequestResponseDTO> consultReturn(@AuthenticationPrincipal(expression = "user") User user,
                                                                  @PathVariable String returnRequestId) {
        return ResponseEntity.ok(ReturnRestMapper.toResponse(buyerPort.consultReturn(user, returnRequestId)));
    }

    @GetMapping("/returns/{returnRequestId}/refund")
    public ResponseEntity<RefundResponseDTO> consultRefund(@AuthenticationPrincipal(expression = "user") User user,
                                                           @PathVariable String returnRequestId) {
        return ResponseEntity.ok(ReturnRestMapper.toResponse(buyerPort.consultRefund(user, returnRequestId)));
    }
}
