package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * RefundStatus (business catalog).
 *
 * <p>Represents the status of a {@code Refund} process derived from an
 * approved return.</p>
 */
public enum RefundStatus implements DomainCatalog {

    PENDING("PENDING", "Pending", "The refund was generated and is being processed."),
    PROCESSED("PROCESSED", "Processed", "The amount was restituted to the buyer."),
    REJECTED("REJECTED", "Rejected", "The refund did not proceed.");

    private final String code;
    private final String name;
    private final String description;

    RefundStatus(String code, String name, String description) {
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
    public static RefundStatus fromCode(String code) {
        for (RefundStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("RefundStatus", code);
    }
}
