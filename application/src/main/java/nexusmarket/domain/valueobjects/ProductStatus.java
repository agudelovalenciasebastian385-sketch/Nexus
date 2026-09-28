package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * ProductStatus (business catalog).
 *
 * <p>Represents the publication status of a product within the catalog.</p>
 */
public enum ProductStatus implements DomainCatalog {

    DRAFT("DRAFT", "Draft", "The product is registered but not yet visible in the public catalog."),
    PUBLISHED("PUBLISHED", "Published", "The product is visible in the public catalog."),
    SUSPENDED("SUSPENDED", "Suspended", "The product is temporarily unavailable."),
    DISCONTINUED("DISCONTINUED", "Discontinued",
            "The product has been permanently withdrawn from the catalog.");

    private final String code;
    private final String name;
    private final String description;

    ProductStatus(String code, String name, String description) {
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
    public static ProductStatus fromCode(String code) {
        for (ProductStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("ProductStatus", code);
    }
}
