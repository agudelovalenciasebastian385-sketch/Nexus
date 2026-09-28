package nexusmarket.domain.valueobjects;

import nexusmarket.domain.exceptions.InvalidCatalogCodeException;

/**
 * AuditAction (business catalog).
 *
 * <p>Classifies the business action recorded by an {@code AuditLog}. Every service that changes information
 * registers one or more of these actions.</p>
 */
public enum AuditAction implements DomainCatalog {

    USER_REGISTERED("USER_REGISTERED", "User Registered", "A buyer self-registered in the Marketplace."),
    STAFF_USER_REGISTERED("STAFF_USER_REGISTERED", "Staff User Registered",
            "An administrator registered a logistics operator or a supervisor."),
    USER_LOGGED_IN("USER_LOGGED_IN", "User Logged In", "A user authenticated successfully."),
    USER_STATUS_CHANGED("USER_STATUS_CHANGED", "User Status Changed", "The operational status of a user changed."),
    USER_PASSWORD_CHANGED("USER_PASSWORD_CHANGED", "User Password Changed", "A user changed its password."),
    SELLER_REGISTERED("SELLER_REGISTERED", "Seller Registered", "An administrator onboarded a seller."),
    SELLER_UPDATED("SELLER_UPDATED", "Seller Updated", "The information of a seller changed."),
    BUYER_PROFILE_UPDATED("BUYER_PROFILE_UPDATED", "Buyer Profile Updated",
            "A buyer updated its profile or addresses."),
    BUYER_COMMERCIAL_STATUS_CHANGED("BUYER_COMMERCIAL_STATUS_CHANGED", "Buyer Commercial Status Changed",
            "The commercial status of a buyer changed."),
    WAREHOUSE_REGISTERED("WAREHOUSE_REGISTERED", "Warehouse Registered", "A warehouse was registered."),
    WAREHOUSE_UPDATED("WAREHOUSE_UPDATED", "Warehouse Updated", "The information of a warehouse changed."),
    PRODUCT_REGISTERED("PRODUCT_REGISTERED", "Product Registered", "A seller registered a product."),
    PRODUCT_UPDATED("PRODUCT_UPDATED", "Product Updated", "The commercial information of a product changed."),
    PRODUCT_PUBLISHED("PRODUCT_PUBLISHED", "Product Published",
            "A product became visible in the public catalog."),
    PRODUCT_SUSPENDED("PRODUCT_SUSPENDED", "Product Suspended",
            "A product was temporarily removed from the catalog."),
    PRODUCT_DISCONTINUED("PRODUCT_DISCONTINUED", "Product Discontinued",
            "A product was permanently withdrawn from the catalog."),
    INVENTORY_INITIAL_STOCK_REGISTERED("INVENTORY_INITIAL_STOCK_REGISTERED", "Inventory Initial Stock Registered",
            "An inventory record was created with its initial stock."),
    INVENTORY_INBOUND_REGISTERED("INVENTORY_INBOUND_REGISTERED", "Inventory Inbound Registered",
            "Stock was added to an inventory record."),
    INVENTORY_ADJUSTED("INVENTORY_ADJUSTED", "Inventory Adjusted",
            "The stock or condition of an inventory record was manually corrected."),
    INVENTORY_RESERVED("INVENTORY_RESERVED", "Inventory Reserved", "Stock was reserved for an order in progress."),
    INVENTORY_RESERVATION_RELEASED("INVENTORY_RESERVATION_RELEASED", "Inventory Reservation Released",
            "Reserved stock returned to the available stock."),
    CART_ITEM_ADDED("CART_ITEM_ADDED", "Cart Item Added", "A product was added to a cart."),
    CART_ITEM_REMOVED("CART_ITEM_REMOVED", "Cart Item Removed", "A product was removed from a cart."),
    CART_ITEM_QUANTITY_UPDATED("CART_ITEM_QUANTITY_UPDATED", "Cart Item Quantity Updated",
            "The quantity of a cart line changed."),
    CART_CLEARED("CART_CLEARED", "Cart Cleared", "Every item of a cart was removed."),
    ORDER_CONFIRMED("ORDER_CONFIRMED", "Order Confirmed", "A cart was converted into an order."),
    PAYMENT_APPROVED("PAYMENT_APPROVED", "Payment Approved", "The payment of an order was approved."),
    PAYMENT_REJECTED("PAYMENT_REJECTED", "Payment Rejected", "The payment of an order was rejected."),
    ORDER_DELIVERED("ORDER_DELIVERED", "Order Delivered", "An order was delivered and finalized."),
    INVOICE_GENERATED("INVOICE_GENERATED", "Invoice Generated", "The invoice of an order was generated."),
    SHIPMENT_CREATED("SHIPMENT_CREATED", "Shipment Created", "A shipment of an order was created."),
    SHIPMENT_PACKED("SHIPMENT_PACKED", "Shipment Packed", "A shipment was packed."),
    SHIPMENT_DISPATCHED("SHIPMENT_DISPATCHED", "Shipment Dispatched", "A shipment left its origin warehouse."),
    SHIPMENT_DELIVERED("SHIPMENT_DELIVERED", "Shipment Delivered", "A shipment was delivered."),
    RETURN_REQUESTED("RETURN_REQUESTED", "Return Requested", "A buyer requested the return of an order."),
    RETURN_APPROVED("RETURN_APPROVED", "Return Approved", "A return request was approved."),
    RETURN_REJECTED("RETURN_REJECTED", "Return Rejected", "A return request was rejected."),
    REFUND_CREATED("REFUND_CREATED", "Refund Created", "A refund was generated from an approved return."),
    REFUND_PROCESSED("REFUND_PROCESSED", "Refund Processed",
            "The amount of a refund was restituted to the buyer.");

    private final String code;
    private final String name;
    private final String description;

    AuditAction(String code, String name, String description) {
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
    public static AuditAction fromCode(String code) {
        for (AuditAction value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new InvalidCatalogCodeException("AuditAction", code);
    }
}
