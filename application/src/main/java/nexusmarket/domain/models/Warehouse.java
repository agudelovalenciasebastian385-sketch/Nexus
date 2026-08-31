package nexusmarket.domain.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Warehouse.
 *
 * <p>Represents a physical location where inventory is managed. Warehouses are
 * classified as Marketplace warehouses or Seller warehouses.</p>
 *
 * <p>Relationships: a Seller-type warehouse belongs to exactly one
 * {@link Seller}; a Marketplace-type warehouse is registered and managed by an
 * Administrator; a warehouse holds zero or more {@link Inventory} instances.</p>
 *
 * <p>The {@code WarehouseType} value object of the Domain Model is modeled as
 * a validated {@code String} in this delivery (the catalog class is out of
 * scope); only the constants {@link #TYPE_MARKETPLACE} and
 * {@link #TYPE_SELLER} are accepted.</p>
 */
public class Warehouse {

    public static final String TYPE_MARKETPLACE = "MARKETPLACE";
    public static final String TYPE_SELLER = "SELLER";

    private final String id;
    private String name;
    /** Physical location of the warehouse (modeled as String in this delivery). */
    private String location;
    private final String type;
    private final Seller seller;
    private final List<Inventory> inventories = new ArrayList<>();

    public Warehouse(String id, String name, String location, String type, Seller seller) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Warehouse id cannot be empty.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Warehouse name cannot be empty.");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Warehouse location cannot be empty.");
        }
        if (!TYPE_MARKETPLACE.equals(type) && !TYPE_SELLER.equals(type)) {
            throw new IllegalArgumentException("Warehouse type must be " + TYPE_MARKETPLACE + " or " + TYPE_SELLER + ".");
        }
        if (TYPE_SELLER.equals(type) && seller == null) {
            throw new IllegalArgumentException("A Seller-type warehouse must belong to exactly one seller.");
        }
        if (TYPE_MARKETPLACE.equals(type) && seller != null) {
            throw new IllegalArgumentException("A Marketplace-type warehouse cannot belong to a seller.");
        }
        this.id = id;
        this.name = name;
        this.location = location;
        this.type = type;
        this.seller = seller;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Warehouse name cannot be empty.");
        }
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Warehouse location cannot be empty.");
        }
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public Seller getSeller() {
        return seller;
    }

    public List<Inventory> getInventories() {
        return Collections.unmodifiableList(inventories);
    }

    /** Holds a new inventory record in this warehouse. */
    public void addInventory(Inventory inventory) {
        Objects.requireNonNull(inventory, "Inventory is required.");
        if (inventory.getWarehouse() != this) {
            throw new IllegalArgumentException("The inventory does not belong to this warehouse.");
        }
        inventories.add(inventory);
    }

    public boolean isMarketplace() {
        return TYPE_MARKETPLACE.equals(type);
    }

    public boolean isSellerOwned() {
        return TYPE_SELLER.equals(type);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Warehouse)) {
            return false;
        }
        return id.equals(((Warehouse) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Warehouse{id='" + id + "', name='" + name + "', type=" + type + ", location='" + location + "'}";
    }
}
