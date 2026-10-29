package nexusmarket.adapters.rest.dtos.responses;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

/**
 * One audit record. The acting user is shown by id and email only.
 */
@Data
public class AuditLogResponseDTO {

    private String id;
    /** AuditAction code. */
    private String action;
    private String performedById;
    private String performedByEmail;
    /** SystemRole code of the acting user. */
    private String role;
    /** AuditEntityType code. */
    private String entityType;
    private String entityId;
    private LocalDateTime occurredAt;
    private Map<String, String> details;
}
