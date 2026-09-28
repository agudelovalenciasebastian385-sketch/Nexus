package nexusmarket.domain.services.returns;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.ReturnRequest;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.RejectReturnUseCase;
import nexusmarket.domain.ports.out.ReturnRequestRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Rejects a return request that does not meet the required conditions
 * (returns-services.md - RejectReturn). Restricted to administrators.
 */
@Service
@RequiredArgsConstructor
public class RejectReturnService implements RejectReturnUseCase {

    private final ReturnRequestRepositoryPort returnRequestRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    @Override
    public ReturnRequest rejectReturn(User requestingUser, String returnRequestId) {
        User administrator = validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR);
        ReturnRequest returnRequest = returnRequestRepositoryPort.findById(returnRequestId)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest", returnRequestId));

        returnRequest.reject();
        returnRequestRepositoryPort.update(returnRequest);
        registerAuditLogService.execute(administrator, AuditAction.RETURN_REJECTED, AuditEntityType.RETURN_REQUEST,
                returnRequestId, Map.of("order", returnRequest.getOrder().getId()));
        return returnRequest;
    }
}
