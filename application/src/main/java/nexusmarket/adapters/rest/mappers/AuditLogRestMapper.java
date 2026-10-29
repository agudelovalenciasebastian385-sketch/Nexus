package nexusmarket.adapters.rest.mappers;

import java.util.List;
import nexusmarket.adapters.rest.dtos.responses.AuditLogResponseDTO;
import nexusmarket.domain.models.AuditLog;

/**
 * Mapper from the AuditLog Domain Model to its REST DTO. The acting user is shown by id and email.
 */
public final class AuditLogRestMapper {

    private AuditLogRestMapper() {
    }

    public static AuditLogResponseDTO toResponse(AuditLog auditLog) {
        AuditLogResponseDTO dto = new AuditLogResponseDTO();
        dto.setId(auditLog.getId());
        dto.setAction(auditLog.getAction().getCode());
        dto.setPerformedById(auditLog.getPerformedBy().getId());
        dto.setPerformedByEmail(auditLog.getPerformedBy().getEmail());
        dto.setRole(auditLog.getRole().getCode());
        dto.setEntityType(auditLog.getEntityType().getCode());
        dto.setEntityId(auditLog.getEntityId());
        dto.setOccurredAt(auditLog.getOccurredAt());
        dto.setDetails(auditLog.getDetails());
        return dto;
    }

    public static List<AuditLogResponseDTO> toResponse(List<AuditLog> auditLogs) {
        return auditLogs.stream().map(AuditLogRestMapper::toResponse).toList();
    }
}
