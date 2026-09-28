package nexusmarket.domain.ports.out;

import java.util.List;

import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.AuditEntityType;

/**
 * Output port: Append-only persistence and queries of {@code AuditLog} records (intended for MongoDB).
 */
public interface AuditLogRepositoryPort {

    AuditLog save(AuditLog auditLog);

    List<AuditLog> findAll();

    List<AuditLog> findByPerformedBy(User user);

    List<AuditLog> findByEntity(AuditEntityType entityType, String entityId);
}
