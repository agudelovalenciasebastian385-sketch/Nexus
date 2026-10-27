package nexusmarket.domain.services.buyer;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.BuyerRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Changes the commercial status of a buyer (buyer-services.md -
 * ChangeBuyerCommercialStatus). Restricted to administrators.
 */
@Service
@RequiredArgsConstructor
public class ChangeBuyerCommercialStatusService {

    private final BuyerRepositoryPort buyerRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public Buyer changeBuyerCommercialStatus(User requestingUser, String buyerId, CommercialStatus newStatus) {
        User administrator = validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR);
        Buyer buyer = buyerRepositoryPort.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("Buyer", buyerId));

        CommercialStatus previousStatus = buyer.getCommercialStatus();
        buyer.setCommercialStatus(newStatus);
        buyerRepositoryPort.update(buyer);
        registerAuditLogService.execute(administrator, AuditAction.BUYER_COMMERCIAL_STATUS_CHANGED,
                AuditEntityType.BUYER, buyer.getId(),
                Map.of("from", previousStatus.getCode(), "to", newStatus.getCode()));
        return buyer;
    }
}
