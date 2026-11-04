package nexusmarket.adapters.persistence.memory;

import java.util.Comparator;
import java.util.List;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import nexusmarket.domain.valueobjects.AuditEntityType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Audit trail in memory, oldest first like AuditLogMongoAdapter. */
@Repository
@Profile("memory")
public class AuditLogMemoryAdapter implements AuditLogRepositoryPort {

    private final MemoryStore<String, AuditLog> auditLogs = new MemoryStore<>();

    /** Writes after the commit, like AuditLogMongoAdapter: an operation that fails leaves no audit record. */
    @Override
    public AuditLog save(AuditLog auditLog) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            auditLogs.put(auditLog.getId(), auditLog);
            return auditLog;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                auditLogs.put(auditLog.getId(), auditLog);
            }
        });
        return auditLog;
    }

    @Override
    public List<AuditLog> findAll() {
        return auditLogs.values().stream().sorted(Comparator.comparing(AuditLog::getOccurredAt)).toList();
    }

    @Override
    public List<AuditLog> findByPerformedBy(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        return findAll().stream().filter(log -> log.getPerformedBy().getId().equals(user.getId())).toList();
    }

    @Override
    public List<AuditLog> findByEntity(AuditEntityType entityType, String entityId) {
        if (entityType == null || entityId == null) {
            return List.of();
        }
        return findAll().stream()
                .filter(log -> log.getEntityType() == entityType && log.getEntityId().equals(entityId)).toList();
    }
}
