package nexusmarket.domain.services.seller;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.UpdateSellerUseCase;
import nexusmarket.domain.ports.out.SellerRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.services.authorization.ValidateSellerOwnershipService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Updates the full name of a seller (seller-services.md - UpdateSeller).
 * Identification number and email are immutable.
 */
@Service
@RequiredArgsConstructor
public class UpdateSellerService implements UpdateSellerUseCase {

    private final SellerRepositoryPort sellerRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final ValidateSellerOwnershipService validateSellerOwnershipService;
    private final RegisterAuditLogService registerAuditLogService;

    @Override
    public Seller updateSeller(User requestingUser, String sellerId, String fullName) {
        User requester = validateRoleAuthorizationService.execute(requestingUser,
                SystemRole.SELLER, SystemRole.ADMINISTRATOR);
        Seller seller = sellerRepositoryPort.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller", sellerId));
        validateSellerOwnershipService.execute(requester, seller);

        seller.updateFullName(fullName);
        sellerRepositoryPort.update(seller);
        registerAuditLogService.execute(requester, AuditAction.SELLER_UPDATED, AuditEntityType.SELLER,
                seller.getId(), Map.of("fullName", fullName));
        return seller;
    }
}
