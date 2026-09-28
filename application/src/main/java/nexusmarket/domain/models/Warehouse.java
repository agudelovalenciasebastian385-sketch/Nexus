package nexusmarket.domain.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nexusmarket.domain.exceptions.InvalidInventoryException;
import nexusmarket.domain.exceptions.InvalidWarehouseException;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.WarehouseType;

/**
 * Warehouse.
 *
 * <p>Represents a physical location where inventory is managed. Warehouses are
 * classified as Marketplace warehouses or Seller warehouses.</p>
 *
 * <p>Relationships: a Seller-type warehouse belongs to exactly one
 * {@link Seller}; a Marketplace-type warehouse is registered and managed by an
 * Administrator; a warehouse holds zero or more {@link Inventory} instances.</p>
 */
public class Warehouse {

    private final String id;
    private String name;
    /** Physical location of the warehouse. */
    private Address location;
    private final WarehouseType type;
    private final Seller seller;
    private final List<Inventory> inventories = new ArrayList<>();

    public Warehouse(String id, String name, Address location, WarehouseType type, Seller seller) {
        if (id == null || id.isBlank()) {
            throw new InvalidWarehouseException("Warehouse id cannot be empty.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidWarehouseException("Warehouse name cannot be empty.");
        }
        if (location == null) {
            throw new InvalidWarehouseException("Warehouse location is required.");
        }
        if (type == null) {
            throw new InvalidWarehouseException("Warehouse type is required.");
        }
        if (type == WarehouseType.SELLER && seller == null) {
            throw new InvalidWarehouseException("A Seller-type warehouse must belong to exactly one seller.");
        }
        if (type == WarehouseType.MARKETPLACE && seller != null) {
            throw new InvalidWarehouseException("A Marketplace-type warehouse cannot belong to a seller.");
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
            throw new InvalidWarehouseException("Warehouse name cannot be empty.");
        }
        this.name = name;
    }

    public Address getLocation() {
        return location;
    }

    public void setLocation(Address location) {
        if (location == null) {
            throw new InvalidWarehouseException("Warehouse location is required.");
        }
        this.location = location;
    }

    public WarehouseType getType() {
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
        if (inventory == null) {
            throw new InvalidInventoryException("Inventory is required.");
        }
        if (inventory.getWarehouse() != this) {
            throw new InvalidInventoryException("The inventory does not belong to this warehouse.");
        }
        inventories.add(inventory);
    }

    public boolean isMarketplace() {
        return type == WarehouseType.MARKETPLACE;
    }

    public boolean isSellerOwned() {
        return type == WarehouseType.SELLER;
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
        return "Warehouse{id='" + id + "', name='" + name + "', type=" + type + ", location=" + location + "}";
    }
}
