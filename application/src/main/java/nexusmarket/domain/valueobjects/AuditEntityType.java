package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * AuditEntityType (business catalog).
 *
 * <p>Classifies the type of entity affected by an audited operation.</p>
 */
public enum AuditEntityType implements DomainCatalog {

    USER("USER", "User", "A user of any role."),
    SELLER("SELLER", "Seller", "A seller."),
    BUYER("BUYER", "Buyer", "A buyer."),
    WAREHOUSE("WAREHOUSE", "Warehouse", "A warehouse."),
    PRODUCT("PRODUCT", "Product", "A catalog product."),
    INVENTORY("INVENTORY", "Inventory", "An inventory record (identified by product and warehouse)."),
    CART("CART", "Cart", "A shopping cart."),
    ORDER("ORDER", "Order", "An order."),
    INVOICE("INVOICE", "Invoice", "An invoice."),
    SHIPMENT("SHIPMENT", "Shipment", "A shipment."),
    RETURN_REQUEST("RETURN_REQUEST", "Return Request", "A return request."),
    REFUND("REFUND", "Refund", "A refund.");

    private final String code;
    private final String name;
    private final String description;

    AuditEntityType(String code, String name, String description) {
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
    public static AuditEntityType fromCode(String code) {
        for (AuditEntityType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("AuditEntityType", code);
    }
}
