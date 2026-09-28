package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * WarehouseType (business catalog).
 *
 * <p>Classifies a {@code Warehouse} according to its owner, distinguishing
 * Marketplace warehouses from Seller warehouses.</p>
 */
public enum WarehouseType implements DomainCatalog {

    MARKETPLACE("MARKETPLACE", "Marketplace", "Warehouse directly administered by the Marketplace."),
    SELLER("SELLER", "Seller", "Warehouse owned and administered by a Seller.");

    private final String code;
    private final String name;
    private final String description;

    WarehouseType(String code, String name, String description) {
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
    public static WarehouseType fromCode(String code) {
        for (WarehouseType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("WarehouseType", code);
    }
}
