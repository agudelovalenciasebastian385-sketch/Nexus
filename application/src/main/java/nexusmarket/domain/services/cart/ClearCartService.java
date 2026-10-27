package nexusmarket.domain.services.cart;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;
import nexusmarket.domain.ports.out.CartRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Removes every item from the requesting buyer's cart (cart-services.md - ClearCart).
 */
@Service
@RequiredArgsConstructor
public class ClearCartService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final CartRepositoryPort cartRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public Cart clearCart(User requestingUser) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.BUYER);
        Cart cart = loadOwnCart(requester);

        cart.clear();
        cartRepositoryPort.update(cart);
        registerAuditLogService.execute(requester, AuditAction.CART_CLEARED, AuditEntityType.CART, cart.getId(),
                Map.of());
        return cart;
    }

    private Cart loadOwnCart(User requester) {
        Buyer buyer = buyerRepositoryPort.findById(requester.getId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer", requester.getId()));
        return cartRepositoryPort.findByBuyer(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Cart", "buyer " + buyer.getId()));
    }

}
