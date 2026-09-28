package nexusmarket.domain.services.product;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.ConsultSellerProductsUseCase;
import nexusmarket.domain.ports.out.ProductRepositoryPort;
import nexusmarket.domain.ports.out.SellerRepositoryPort;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.authorization.ValidateSellerOwnershipService;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Retrieves all the products of a seller, in any status (product-services.md -
 * ConsultSellerProducts). A seller only consults its own products.
 */
@Service
@RequiredArgsConstructor
public class ConsultSellerProductsService implements ConsultSellerProductsUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    @Override
    public List<Product> consultSellerProducts(User requestingUser, String sellerId) {
        User requester = validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.SELLER, SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
        Seller seller = sellerRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller", sellerId));
        validateSellerOwnershipService.execute(requester, seller);
        return productRepositoryPort.findBySeller(seller);
    }
}
