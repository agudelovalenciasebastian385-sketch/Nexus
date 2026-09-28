package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * UserStatus (business catalog).
 *
 * <p>Represents the operational condition of a user within the platform.</p>
 */
public enum UserStatus implements DomainCatalog {

    ACTIVE("ACTIVE", "Active", "The user can operate normally within the system."),
    BLOCKED("BLOCKED", "Blocked", "The user's access has been suspended."),
    INACTIVE("INACTIVE", "Inactive", "The user exists but is not currently enabled to operate.");

    private final String code;
    private final String name;
    private final String description;

    UserStatus(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /** Resolves a catalog value from its unique business code. */
    public static UserStatus fromCode(String code) {
        for (UserStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("UserStatus", code);
    }
}
