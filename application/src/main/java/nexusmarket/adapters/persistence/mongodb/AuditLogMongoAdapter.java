package nexusmarket.adapters.persistence.mongodb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nexusmarket.adapters.persistence.jpa.JpaAggregateLoader;
import nexusmarket.adapters.persistence.mongodb.documents.AuditLogDocument;
import nexusmarket.adapters.persistence.mongodb.mappers.AuditLogMongoMapper;
import nexusmarket.adapters.persistence.mongodb.repositories.AuditLogMongoRepository;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.AuditLogRepositoryPort;
import nexusmarket.domain.valueobjects.AuditEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Output persistence adapter for the audit trail backed by Spring Data MongoDB
 * (audit_logs collection). Append-only: there is no update or delete.
 *
 * <p>AuditLog requires a fully built User as performedBy, which cannot be rebuilt from the
 * document alone (User validates identification, email and password). The acting user is
 * therefore loaded from the MySQL users table through {@link JpaAggregateLoader}, once per
 * user and per query. findByPerformedBy reuses the User supplied by the caller.
 */
@Repository
public class AuditLogMongoAdapter implements AuditLogRepositoryPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogMongoAdapter.class);

    private final AuditLogMongoRepository auditLogRepository;
    private final JpaAggregateLoader loader;

    public AuditLogMongoAdapter(AuditLogMongoRepository auditLogRepository, JpaAggregateLoader loader) {
        this.auditLogRepository = auditLogRepository;
        this.loader = loader;
    }

    /**
     * The JPA transaction does not cover MongoDB, so inside a transaction the record is written only
     * after the commit: a rolled-back operation leaves no audit record. A failure of MongoDB at that
     * point is logged (with the data needed to rebuild the record) without failing the already
     * committed operation. Without a transaction the record is written immediately.
     */
    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogDocument document = AuditLogMongoMapper.toDocument(auditLog);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            auditLogRepository.save(document);
            return auditLog;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    auditLogRepository.save(document);
                } catch (RuntimeException e) {
                    LOGGER.error("Audit record lost after commit: id={} action={} entityType={} entityId={} "
                                    + "performedBy={} occurredAt={} details={}", document.getId(),
                            document.getAction(), document.getEntityType(), document.getEntityId(),
                            document.getPerformedById(), document.getOccurredAt(), document.getDetails(), e);
                }
            }
        });
        return auditLog;
    }

    @Override
    public List<AuditLog> findAll() {
        return toDomain(auditLogRepository.findAllByOrderByOccurredAtAsc());
    }

    @Override
    public List<AuditLog> findByPerformedBy(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        return auditLogRepository.findByPerformedByIdOrderByOccurredAtAsc(user.getId()).stream()
                .map(document -> AuditLogMongoMapper.toDomain(document, user))
                .toList();
    }

    @Override
    public List<AuditLog> findByEntity(AuditEntityType entityType, String entityId) {
        if (entityType == null || entityId == null) {
            return List.of();
        }
        return toDomain(auditLogRepository.findByEntityTypeAndEntityIdOrderByOccurredAtAsc(entityType.getCode(),
                entityId));
    }

    private List<AuditLog> toDomain(List<AuditLogDocument> documents) {
        Map<String, User> users = new HashMap<>();
        return documents.stream()
                .map(document -> AuditLogMongoMapper.toDomain(document, users.computeIfAbsent(
                        document.getPerformedById(), id -> loader.requireUser(id, User.class))))
                .toList();
    }
}
