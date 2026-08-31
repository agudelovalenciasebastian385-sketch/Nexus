package nexusmarket.domain.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import nexusmarket.domain.valueobjects.ProductStatus;

/**
 * PhysicalProduct.
 *
 * <p>Represents a tangible good that requires inventory and logistics
 * dispatch for delivery. A physical product is mandatorily linked to one or
 * more {@link Inventory} instances.</p>
 */
public class PhysicalProduct extends Product {

    /** Distributed stock of the product per warehouse. Empty by default. */
    private final List<Inventory> inventories = new ArrayList<>();

    public PhysicalProduct(String id, String name, String description, Seller seller,
                           List<String> variants, ProductStatus status) {
        super(id, name, description, seller, variants, status);
    }

    public List<Inventory> getInventories() {
        return Collections.unmodifiableList(inventories);
    }

    /** Links a new inventory record of this product. */
    public void addInventory(Inventory inventory) {
        Objects.requireNonNull(inventory, "Inventory is required.");
        if (inventory.getProduct() != this) {
            throw new IllegalArgumentException("The inventory does not belong to this product.");
        }
        inventories.add(inventory);
    }

    /** Total stock of the product across all warehouses. */
    public int totalStock() {
        int total = 0;
        for (Inventory inventory : inventories) {
            total += inventory.getQuantity();
        }
        return total;
    }
}
