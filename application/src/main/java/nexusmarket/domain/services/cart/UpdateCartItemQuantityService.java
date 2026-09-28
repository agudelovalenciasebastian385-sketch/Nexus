package nexusmarket.domain.services.cart;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.UpdateCartItemQuantityUseCase;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;
import nexusmarket.domain.ports.out.CartRepositoryPort;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.Variant;
import org.springframework.stereotype.Service;

/**
 * Replaces the quantity of a product/variant line of the requesting buyer's cart (cart-services.md - UpdateCartItemQuantity).
 */
@Service
@RequiredArgsConstructor
public class UpdateCartItemQuantityService implements UpdateCartItemQuantityUseCase {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final CartRepositoryPort cartRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    @Override
    public Cart updateCartItemQuantity(User requestingUser, String productId, Variant variant, int quantity) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.BUYER);
        Cart cart = loadOwnCart(requester);
        Product product = loadProduct(productId);

        cart.changeItemQuantity(product, variant, quantity);
        cartRepositoryPort.update(cart);
        registerAuditLogService.execute(requester, AuditAction.CART_ITEM_QUANTITY_UPDATED, AuditEntityType.CART,
                cart.getId(), Map.of("product", productId, "quantity", String.valueOf(quantity)));
        return cart;
    }

    private Cart loadOwnCart(User requester) {
        Buyer buyer = buyerRepositoryPort.findById(requester.getId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer", requester.getId()));
        return cartRepositoryPort.findByBuyer(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Cart", "buyer " + buyer.getId()));
    }

    private Product loadProduct(String productId) {
        return productRepositoryPort.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", productId));
    }

}
