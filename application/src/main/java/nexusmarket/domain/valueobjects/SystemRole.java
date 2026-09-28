package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * SystemRole (business catalog "UserRole" in the Domain Model).
 *
 * <p>Represents the responsibilities and permissions assigned to a user
 * within NexusMarket. The role is a characteristic of {@code User} because it
 * defines what that participant means within the system; each user holds
 * exactly one role.</p>
 */
public enum SystemRole implements DomainCatalog {

    BUYER("BUYER", "Buyer", "Person who purchases published products."),
    SELLER("SELLER", "Seller", "Responsible for registering and managing their own products."),
    LOGISTICS_OPERATOR("LOGISTICS_OPERATOR", "Logistics Operator",
            "In charge of the physical operation of warehouses and dispatches."),
    ADMINISTRATOR("ADMINISTRATOR", "Administrator",
            "Responsible for the administration of sellers and warehouses."),
    SUPERVISOR("SUPERVISOR", "Supervisor",
            "Read-only profile for oversight and operational monitoring.");

    private final String code;
    private final String name;
    private final String description;

    SystemRole(String code, String name, String description) {
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
    public static SystemRole fromCode(String code) {
        for (SystemRole value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("SystemRole", code);
    }
}
