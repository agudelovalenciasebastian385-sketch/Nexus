package nexusmarket.domain.services.buyer;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Updates the profile of the requesting buyer: full name, primary address and
 * additional addresses (buyer-services.md - UpdateBuyerProfile). The list of
 * additional addresses replaces the current one.
 */
@Service
@RequiredArgsConstructor
public class UpdateBuyerProfileService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public Buyer updateBuyerProfile(User requestingUser, String fullName, Address primaryAddress,
                                    List<Address> additionalAddresses) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.BUYER);
        Buyer buyer = buyerRepositoryPort.findById(requester.getId())
                .orElseThrow(() -> new EntityNotFoundException("Buyer", requester.getId()));

        buyer.updateFullName(fullName);
        buyer.setPrimaryAddress(primaryAddress);
        for (Address address : List.copyOf(buyer.getAdditionalAddresses())) {
            buyer.removeAdditionalAddress(address);
        }
        if (additionalAddresses != null) {
            for (Address address : additionalAddresses) {
                buyer.addAdditionalAddress(address);
            }
        }

        buyerRepositoryPort.update(buyer);
        registerAuditLogService.execute(requester, AuditAction.BUYER_PROFILE_UPDATED, AuditEntityType.BUYER,
                buyer.getId(), Map.of("additionalAddresses", String.valueOf(buyer.getAdditionalAddresses().size())));
        return buyer;
    }
}
