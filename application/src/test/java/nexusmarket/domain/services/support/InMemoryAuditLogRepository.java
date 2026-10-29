package nexusmarket.domain.services.support;

import java.util.ArrayList;
import java.util.List;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;

/** In-memory AuditLogRepositoryPort (append-only), with helpers to assert the recorded actions. */
public class InMemoryAuditLogRepository implements AuditLogRepositoryPort {

    private final List<AuditLog> auditLogs = new ArrayList<>();

    @Override
    public AuditLog save(AuditLog auditLog) {
        auditLogs.add(auditLog);
        return auditLog;
    }

    @Override
    public List<AuditLog> findAll() {
        return List.copyOf(auditLogs);
    }

    @Override
    public List<AuditLog> findByPerformedBy(User user) {
        return auditLogs.stream().filter(log -> log.getPerformedBy().getId().equals(user.getId())).toList();
    }

    @Override
    public List<AuditLog> findByEntity(AuditEntityType entityType, String entityId) {
        return auditLogs.stream()
                .filter(log -> log.getEntityType() == entityType && log.getEntityId().equals(entityId)).toList();
    }

    /** Actions recorded so far, in order. */
    public List<AuditAction> actions() {
        return auditLogs.stream().map(AuditLog::getAction).toList();
    }

    /** The last record with the given action. */
    public AuditLog last(AuditAction action) {
        return auditLogs.stream().filter(log -> log.getAction() == action).reduce((first, second) -> second)
                .orElseThrow(() -> new AssertionError("No audit record " + action));
    }

    public void clear() {
        auditLogs.clear();
    }
}
