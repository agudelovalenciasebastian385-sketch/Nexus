package nexusmarket.domain.services.cart;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Cart;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;
import nexusmarket.domain.ports.out.CartRepositoryPort;
import nexusmarket.domain.services.authorization.ValidateBuyerOwnershipService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Retrieves the cart of a buyer with its items and total (cart-services.md -
 * ConsultCart). A buyer only consults its own cart.
 */
@Service
@RequiredArgsConstructor
public class ConsultCartService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final CartRepositoryPort cartRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public Cart consultCart(User requestingUser, String buyerId) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.BUYER,
                SystemRole.SUPERVISOR);
        Buyer buyer = buyerRepositoryPort.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer", buyerId));
        validateBuyerOwnershipService.execute(requester, buyer);
        return cartRepositoryPort.findByBuyer(buyer)
                .orElseThrow(() -> new EntityNotFoundException("Cart", "buyer " + buyerId));
    }
}
