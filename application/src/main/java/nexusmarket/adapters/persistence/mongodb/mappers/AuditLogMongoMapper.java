package nexusmarket.adapters.persistence.mongodb.mappers;

import nexusmarket.adapters.persistence.mongodb.documents.AuditLogDocument;
import nexusmarket.domain.models.AuditLog;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;

/**
 * Bidirectional mapper between the AuditLog Domain Model and its MongoDB document.
 *
 * <p>The acting user is stored as a reference plus denormalized email and role. When reading,
 * the adapter supplies the resolved User; AuditLog derives its role from that user.
 */
public final class AuditLogMongoMapper {

    private AuditLogMongoMapper() {
    }

    public static AuditLogDocument toDocument(AuditLog domain) {
        if (domain == null) {
            return null;
        }
        AuditLogDocument document = new AuditLogDocument();
        document.setId(domain.getId());
        document.setAction(domain.getAction().getCode());
        document.setPerformedById(domain.getPerformedBy().getId());
        document.setPerformedByEmail(domain.getPerformedBy().getEmail());
        document.setRole(domain.getRole().getCode());
        document.setEntityType(domain.getEntityType().getCode());
        document.setEntityId(domain.getEntityId());
        document.setOccurredAt(domain.getOccurredAt());
        document.setDetails(domain.getDetails());
        return document;
    }

    public static AuditLog toDomain(AuditLogDocument document, User performedBy) {
        if (document == null) {
            return null;
        }
        return new AuditLog(document.getId(), AuditAction.fromCode(document.getAction()), performedBy,
                AuditEntityType.fromCode(document.getEntityType()), document.getEntityId(),
                document.getOccurredAt(), document.getDetails());
    }
}
