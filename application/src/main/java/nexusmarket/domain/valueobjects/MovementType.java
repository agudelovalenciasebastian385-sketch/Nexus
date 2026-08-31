package nexusmarket.domain.valueobjects;

/**
 * MovementType (business catalog "InventoryMovementType" in the Domain Model).
 *
 * <p>Classifies the movements that an {@code Inventory} record can undergo.</p>
 */
public enum MovementType implements DomainCatalog {

    INBOUND("INBOUND", "Inbound", "Stock addition to the warehouse."),
    RESERVATION("RESERVATION", "Reservation", "Temporary stock hold for an order in progress."),
    SALE_OUTBOUND("SALE_OUTBOUND", "Sale Outbound", "Stock deduction upon confirming a sale."),
    ADJUSTMENT("ADJUSTMENT", "Adjustment", "Manual correction of stock."),
    RETURN("RETURN", "Return", "Stock re-entry resulting from an approved return.");

    private final String code;
    private final String name;
    private final String description;

    MovementType(String code, String name, String description) {
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
    public static MovementType fromCode(String code) {
        for (MovementType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown MovementType code: " + code);
    }
}
