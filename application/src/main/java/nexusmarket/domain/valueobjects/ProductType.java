package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * ProductType (business catalog).
 *
 * <p>Classifies a {@code Product} of the catalog according to how it is
 * delivered: physical products require inventory and logistics dispatch,
 * digital products are delivered immediately after payment.</p>
 */
public enum ProductType implements DomainCatalog {

    PHYSICAL("PHYSICAL", "Physical", "Tangible good that requires inventory and logistics dispatch."),
    DIGITAL("DIGITAL", "Digital", "Intangible good delivered immediately after payment confirmation.");

    private final String code;
    private final String name;
    private final String description;

    ProductType(String code, String name, String description) {
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
    public static ProductType fromCode(String code) {
        for (ProductType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("ProductType", code);
    }
}
