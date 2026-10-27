package nexusmarket.domain.services.seller;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.SellerRepositoryPort;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.authorization.ValidateSellerOwnershipService;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Retrieves the information of a seller (seller-services.md - ConsultSeller).
 */
@Service
@RequiredArgsConstructor
public class ConsultSellerService {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;

    public Seller consultSeller(User requestingUser, String sellerId) {
        User requester = validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.SELLER, SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
        Seller seller = sellerRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller", sellerId));
        validateSellerOwnershipService.execute(requester, seller);
        return seller;
    }
}
