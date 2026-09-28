package nexusmarket.domain.services.audit;

import java.util.List;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.ConsultAuditLogsUseCase;
import nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Retrieves audit records. Read-only; restricted to administrators and
 * supervisors (audit-services.md).
 */
@Service
@RequiredArgsConstructor
public class ConsultAuditLogsService implements ConsultAuditLogsUseCase {

    private final AuditLogRepositoryPort auditLogRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    @Override
    public List<AuditLog> consultAuditLogs(User requestingUser) {
        authorize(requestingUser);
        return auditLogRepositoryPort.findAll();
    }

    @Override
    public List<AuditLog> consultAuditLogsByUser(User requestingUser, String userId) {
        authorize(requestingUser);
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        return auditLogRepositoryPort.findByPerformedBy(user);
    }

    @Override
    public List<AuditLog> consultAuditLogsByEntity(User requestingUser, AuditEntityType entityType, String entityId) {
        authorize(requestingUser);
        return auditLogRepositoryPort.findByEntity(entityType, entityId);
    }

    private void authorize(User requestingUser) {
        validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR, SystemRole.SUPERVISOR);
    }
}
