package nexusmarket.domain.ports.in;

import java.util.List;

import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.AuditEntityType;

/**
 * Input port: ConsultAuditLogs use case.
 *
 * <p>Retrieves audit records: all of them, those performed by a user, or those of an entity.</p>
 */
public interface ConsultAuditLogsUseCase {

    List<AuditLog> consultAuditLogs(User requestingUser);

    List<AuditLog> consultAuditLogsByUser(User requestingUser, String userId);

    List<AuditLog> consultAuditLogsByEntity(User requestingUser, AuditEntityType entityType, String entityId);
}
