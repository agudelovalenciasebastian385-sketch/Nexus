package nexusmarket.domain.services.authorization;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * RG-03 for sellers: a seller only manages its own information.
 *
 * <p>Only applies to users with role {@code SELLER}; for other roles the check
 * is skipped because role authorization is done by
 * {@link ValidateRoleAuthorizationService} (authorization-services.md).</p>
 */
@Service
@RequiredArgsConstructor
public class ValidateSellerOwnershipService {

    public void execute(User user, Seller seller) {
        if (isSeller(user) && !seller.getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("A seller can only access its own information (RG-03).");
        }
    }

    public void execute(User user, Product product) {
        if (isSeller(user) && !product.getSeller().getId().equals(user.getId())) {
            throw new UnauthorizedOperationException("Product " + product.getId()
                    + " belongs to another seller (RG-03).");
        }
    }

    public void execute(User user, Warehouse warehouse) {
        if (isSeller(user) && (!warehouse.isSellerOwned() || !warehouse.getSeller().getId().equals(user.getId()))) {
            throw new UnauthorizedOperationException("Warehouse " + warehouse.getId()
                    + " does not belong to the requesting seller (RG-03).");
        }
    }

    private boolean isSeller(User user) {
        return user.getRole() == SystemRole.SELLER;
    }
}
