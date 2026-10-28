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
import org.springframework.stereotype.Repository;

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

    private final AuditLogMongoRepository auditLogRepository;
    private final JpaAggregateLoader loader;

    public AuditLogMongoAdapter(AuditLogMongoRepository auditLogRepository, JpaAggregateLoader loader) {
        this.auditLogRepository = auditLogRepository;
        this.loader = loader;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        auditLogRepository.save(AuditLogMongoMapper.toDocument(auditLog));
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
