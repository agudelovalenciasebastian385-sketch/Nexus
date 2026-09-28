package nexusmarket.domain.models;

import java.time.LocalDateTime;
import java.util.Map;

import nexusmarket.domain.exceptions.InvalidAuditLogException;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;

/**
 * AuditLog.
 *
 * <p>Represents an immutable record of a significant business operation.
 * Audit records are append-only: they are never modified or deleted. An audit
 * record is performed by exactly one {@link User}; the role is taken from that
 * user when the record is created.</p>
 */
public class AuditLog {

    private final String id;
    private final AuditAction action;
    private final User performedBy;
    private final SystemRole role;
    private final AuditEntityType entityType;
    private final String entityId;
    private final LocalDateTime occurredAt;
    private final Map<String, String> details;

    public AuditLog(String id, AuditAction action, User performedBy, AuditEntityType entityType,
                    String entityId, LocalDateTime occurredAt, Map<String, String> details) {
        if (id == null || id.isBlank()) {
            throw new InvalidAuditLogException("Audit log id cannot be empty.");
        }
        if (action == null) {
            throw new InvalidAuditLogException("Audit log action is required.");
        }
        if (performedBy == null) {
            throw new InvalidAuditLogException("Audit log performedBy is required.");
        }
        if (entityType == null) {
            throw new InvalidAuditLogException("Audit log entity type is required.");
        }
        if (entityId == null || entityId.isBlank()) {
            throw new InvalidAuditLogException("Audit log entity id cannot be empty.");
        }
        if (occurredAt == null) {
            throw new InvalidAuditLogException("Audit log occurredAt is required.");
        }
        this.id = id;
        this.action = action;
        this.performedBy = performedBy;
        this.role = performedBy.getRole();
        this.entityType = entityType;
        this.entityId = entityId;
        this.occurredAt = occurredAt;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    public String getId() {
        return id;
    }

    public AuditAction getAction() {
        return action;
    }

    public User getPerformedBy() {
        return performedBy;
    }

    public SystemRole getRole() {
        return role;
    }

    public AuditEntityType getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AuditLog)) {
            return false;
        }
        return id.equals(((AuditLog) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "AuditLog{id='" + id + "', action=" + action + ", performedBy=" + performedBy.getId()
                + ", role=" + role + ", entityType=" + entityType + ", entityId='" + entityId
                + "', occurredAt=" + occurredAt + "}";
    }
}
