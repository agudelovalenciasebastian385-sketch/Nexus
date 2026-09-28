package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * CommercialStatus (business catalog).
 *
 * <p>Represents a {@code Buyer}'s condition for making purchases within the
 * Marketplace. Independent from {@code UserStatus}.</p>
 */
public enum CommercialStatus implements DomainCatalog {

    ENABLED("ENABLED", "Enabled", "The buyer can make purchases normally."),
    RESTRICTED("RESTRICTED", "Restricted", "The buyer has temporary limitations on purchasing."),
    SUSPENDED("SUSPENDED", "Suspended", "The buyer cannot make purchases.");

    private final String code;
    private final String name;
    private final String description;

    CommercialStatus(String code, String name, String description) {
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
    public static CommercialStatus fromCode(String code) {
        for (CommercialStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("CommercialStatus", code);
    }
}
