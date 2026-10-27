package nexusmarket.domain.services.buyer;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;
import nexusmarket.domain.services.authorization.ValidateBuyerOwnershipService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Retrieves the information of a buyer (buyer-services.md - ConsultBuyer). A
 * buyer never consults information of other buyers.
 */
@Service
@RequiredArgsConstructor
public class ConsultBuyerService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateBuyerOwnershipService validateBuyerOwnershipService;

    public Buyer consultBuyer(User requestingUser, String buyerId) {
        User requester = validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.BUYER, SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
        Buyer buyer = buyerRepositoryPort.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer", buyerId));
        validateBuyerOwnershipService.execute(requester, buyer);
        return buyer;
    }
}
