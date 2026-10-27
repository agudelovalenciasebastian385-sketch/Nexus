package nexusmarket.domain.services.order;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.services.authorization.ValidateBuyerOwnershipService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Retrieves the historical and active orders of a buyer (order-services.md -
 * ConsultBuyerOrders). A buyer only consults its own orders.
 */
@Service
@RequiredArgsConstructor
public class ConsultBuyerOrdersService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public List<Order> consultBuyerOrders(User requestingUser, String buyerId) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.BUYER,
                SystemRole.SUPERVISOR);
        Buyer buyer = buyerRepositoryPort.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer", buyerId));
        validateBuyerOwnershipService.execute(requester, buyer);
        return orderRepositoryPort.findByBuyer(buyer);
    }
}
