package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * OrderStatus (business catalog).
 *
 * <p>Represents the lifecycle stage of an {@code Order}. The {@code DELIVERED}
 * stage means successful completion of the delivery: a finalized order cannot
 * be modified afterward.</p>
 */
public enum OrderStatus implements DomainCatalog {

    CART("CART", "Cart", "Provisional product selection, not yet confirmed."),
    PENDING_PAYMENT("PENDING_PAYMENT", "Pending Payment", "Awaiting financial confirmation."),
    PAID("PAID", "Paid", "Fulfillment process begins after payment validation."),
    SHIPPED("SHIPPED", "Shipped", "The order has physically left the warehouse."),
    DELIVERED("DELIVERED", "Delivered / Finalized",
            "Successful completion of the delivery. Cannot be modified afterward.");

    private final String code;
    private final String name;
    private final String description;

    OrderStatus(String code, String name, String description) {
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
    public static OrderStatus fromCode(String code) {
        for (OrderStatus value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("OrderStatus", code);
    }
}
