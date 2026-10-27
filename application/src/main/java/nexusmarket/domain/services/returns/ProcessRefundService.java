package nexusmarket.domain.services.returns;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.Refund;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.RefundRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Processes a pending refund: the amount is restituted to the buyer
 * ({@code PENDING} → {@code PROCESSED}) (returns-services.md - ProcessRefund).
 * Restricted to administrators.
 */
@Service
@RequiredArgsConstructor
public class ProcessRefundService {

    private final RefundRepositoryPort refundRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public Refund processRefund(User requestingUser, String refundId) {
        User administrator = validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR);
        Refund refund = refundRepositoryPort.findById(refundId)
                .orElseThrow(() -> new EntityNotFoundException("Refund", refundId));

        refund.markProcessed(LocalDateTime.now());
        refundRepositoryPort.update(refund);
        registerAuditLogService.execute(administrator, AuditAction.REFUND_PROCESSED, AuditEntityType.REFUND,
                refundId, Map.of("amount", refund.getAmount().toPlainString()));
        return refund;
    }
}
