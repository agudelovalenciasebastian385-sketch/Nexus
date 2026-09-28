package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * InventoryCondition (business catalog).
 *
 * <p>Represents the operational condition of an {@code Inventory} record, that
 * is, how its current stock can be used by the marketplace processes. This
 * catalog is not defined in the Domain Value Objects document; the allowed
 * values below were agreed for the current delivery.</p>
 */
public enum InventoryCondition implements DomainCatalog {

    AVAILABLE("AVAILABLE", "Available", "The stock of the record can be sold and dispatched normally."),
    RESERVED("RESERVED", "Reserved", "The stock of the record is temporarily held for orders in progress."),
    OUT_OF_STOCK("OUT_OF_STOCK", "Out of Stock", "The record has no available stock at the moment."),
    DAMAGED("DAMAGED", "Damaged", "The stock of the record is damaged and can never be reserved or sold.");

    private final String code;
    private final String name;
    private final String description;

    InventoryCondition(String code, String name, String description) {
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
    public static InventoryCondition fromCode(String code) {
        for (InventoryCondition value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("InventoryCondition", code);
    }
}
