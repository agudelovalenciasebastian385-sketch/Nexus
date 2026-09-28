package nexusmarket.domain.services.audit;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import org.springframework.stereotype.Service;

/**
 * Registers an immutable audit record of a significant business operation.
 *
 * <p>The record gets a generated id and the current date-time; its role is
 * taken from {@code performedBy}. Audit records are append-only
 * (audit-services.md).</p>
 */
@Service
@RequiredArgsConstructor
public class RegisterAuditLogService {

    private final AuditLogRepositoryPort auditLogRepositoryPort;

    public AuditLog execute(User performedBy, AuditAction action, AuditEntityType entityType, String entityId,
                            Map<String, String> details) {
        AuditLog auditLog = new AuditLog(UUID.randomUUID().toString(), action, performedBy, entityType, entityId,
                LocalDateTime.now(), details);
        return auditLogRepositoryPort.save(auditLog);
    }
}
