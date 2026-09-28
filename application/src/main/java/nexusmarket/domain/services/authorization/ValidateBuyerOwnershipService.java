package nexusmarket.domain.services.authorization;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * RG-03 for buyers: a buyer only accesses its own information.
 *
 * <p>Only applies to users with role {@code BUYER}; for other roles the check
 * is skipped (authorization-services.md).</p>
 */
@Service
@RequiredArgsConstructor
public class ValidateBuyerOwnershipService {

    public void execute(User user, Buyer buyer) {
        if (isBuyer(user) && !buyer.getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("A buyer can only access its own information (RG-03).");
        }
    }

    public void execute(User user, Order order) {
        if (isBuyer(user) && !order.getBuyer().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("Order " + order.getId()
                    + " belongs to another buyer (RG-03).");
        }
    }

    private boolean isBuyer(User user) {
        return user.getRole() == SystemRole.BUYER;
    }
}
