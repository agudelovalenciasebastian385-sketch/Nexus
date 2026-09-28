package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * ReturnStatus (business catalog).
 *
 * <p>Represents the status of a {@code ReturnRequest} process.</p>
 */
public enum ReturnStatus implements DomainCatalog {

    REQUESTED("REQUESTED", "Requested", "The return was registered by the buyer."),
    APPROVED("APPROVED", "Approved", "The return was validated and triggers inventory re-entry."),
    REJECTED("REJECTED", "Rejected", "The return did not meet the required conditions.");

    private final String code;
    private final String name;
    private final String description;

    ReturnStatus(String code, String name, String description) {
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
    public static ReturnStatus fromCode(String code) {
        for (ReturnStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("ReturnStatus", code);
    }
}
