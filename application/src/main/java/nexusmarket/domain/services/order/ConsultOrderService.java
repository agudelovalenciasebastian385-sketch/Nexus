package nexusmarket.domain.services.order;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.OrderItem;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.ConsultOrderUseCase;
import nexusmarket.domain.ports.out.OrderRepositoryPort;
import nexusmarket.domain.services.authorization.ValidateBuyerOwnershipService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Retrieves an order (order-services.md - ConsultOrder). A buyer only consults
 * its own orders and a seller only orders that contain at least one of its
 * products.
 */
@Service
@RequiredArgsConstructor
public class ConsultOrderService implements ConsultOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    @Override
    public Order consultOrder(User requestingUser, String orderId) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.BUYER,
                SystemRole.SELLER, SystemRole.LOGISTICS_OPERATOR, SystemRole.SUPERVISOR);
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
        validateBuyerOwnershipService.execute(requester, order);
        if (requester.getRole() == SystemRole.SELLER && !containsProductOf(order, requester)) {
            throw new UnauthorizedOperationException("Order " + orderId
                    + " does not contain products of the requesting seller (RG-03).");
        }
        return order;
    }

    private boolean containsProductOf(Order order, User seller) {
        for (OrderItem item : order.getItems()) {
            if (item.getProduct().getSeller().getId().equals(seller.getId())) {
                return true;
            }
        }
        return false;
    }
}
