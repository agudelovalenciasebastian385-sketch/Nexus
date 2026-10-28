package nexusmarket.adapters.persistence.mongodb.documents;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB persistence document (repository DTO) for the AuditLog aggregate.
 *
 * <p>Stored in the {@code audit_logs} collection. The acting user is kept as a reference
 * (performedById) plus denormalized display values (email and role at the time of the event),
 * so the trail stays readable in the database without joining MySQL.
 */
@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogDocument {

    @Id
    private String id;

    /** AuditAction catalog code. */
    private String action;

    /** AuditLog.performedBy reference (users.id in MySQL). */
    private String performedById;

    /** Denormalized email of the acting user. */
    private String performedByEmail;

    /** SystemRole catalog code of the acting user (AuditLog.role). */
    private String role;

    /** AuditEntityType catalog code. */
    private String entityType;

    private String entityId;

    private LocalDateTime occurredAt;

    /** AuditLog.details free-form payload. */
    private Map<String, String> details;
}
