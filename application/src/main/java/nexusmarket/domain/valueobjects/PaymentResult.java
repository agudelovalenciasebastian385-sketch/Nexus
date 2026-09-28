package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * PaymentResult (business catalog).
 *
 * <p>Result returned by {@code PaymentGatewayPort.processPayment(order)}.</p>
 */
public enum PaymentResult implements DomainCatalog {

    APPROVED("APPROVED", "Approved", "The payment gateway approved the payment."),
    REJECTED("REJECTED", "Rejected", "The payment gateway rejected the payment.");

    private final String code;
    private final String name;
    private final String description;

    PaymentResult(String code, String name, String description) {
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
    public static PaymentResult fromCode(String code) {
        for (PaymentResult value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("PaymentResult", code);
    }
}
