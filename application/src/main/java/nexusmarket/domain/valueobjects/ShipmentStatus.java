package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * ShipmentStatus (business catalog).
 *
 * <p>Represents the logistics status of a {@code Shipment} associated with an
 * order containing physical products.</p>
 */
public enum ShipmentStatus implements DomainCatalog {

    PREPARING("PREPARING", "Preparing", "The shipment is being packed at the origin warehouse."),
    PACKED("PACKED", "Packed", "The shipment is packed and ready to be dispatched."),
    IN_TRANSIT("IN_TRANSIT", "In Transit", "The shipment is in transport toward its destination."),
    DELIVERED("DELIVERED", "Delivered", "The shipment was successfully delivered.");

    private final String code;
    private final String name;
    private final String description;

    ShipmentStatus(String code, String name, String description) {
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
    public static ShipmentStatus fromCode(String code) {
        for (ShipmentStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("ShipmentStatus", code);
    }
}
