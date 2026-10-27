package nexusmarket.domain.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nexusmarket.domain.exceptions.InvalidProductException;
import nexusmarket.domain.exceptions.InvalidWarehouseException;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;

/**
 * Seller.
 *
 * <p>Represents the entity responsible for commercializing products within the
 * Marketplace.</p>
 *
 * <p>Business rule: sellers cannot self-register; they are onboarded
 * exclusively by an Administrator, who registers the seller together with its
 * first warehouse (enforced at service level, not by this class).</p>
 */
public class Seller extends User {

    /** Own warehouses associated with the seller. At least one upon onboarding. */
    private final List<Warehouse> warehouses = new ArrayList<>();

    /** Products published by the seller. Empty by default. */
    private final List<Product> products = new ArrayList<>();

    /** Creates a new seller starting at token version {@code 1}. */
    public Seller(String id, String identificationNumber, String fullName, String email, String password,
                  UserStatus status) {
        this(id, identificationNumber, fullName, email, password, status, 1);
    }

    /** Rehydration constructor used to rebuild a persisted seller with its stored token version. */
    public Seller(String id, String identificationNumber, String fullName, String email, String password,
                  UserStatus status, Integer authTokenVersion) {
        super(id, identificationNumber, fullName, email, password, SystemRole.SELLER, status,
                authTokenVersion);
    }

    public List<Warehouse> getWarehouses() {
        return Collections.unmodifiableList(warehouses);
    }

    /** Associates one of the seller's own warehouses. */
    public void addWarehouse(Warehouse warehouse) {
        if (warehouse == null) {
            throw new InvalidWarehouseException("Warehouse is required.");
        }
        if (warehouse.getSeller() != this) {
            throw new InvalidWarehouseException("The warehouse does not belong to this seller.");
        }
        warehouses.add(warehouse);
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }

    /** Registers a product published by this seller. */
    public void addProduct(Product product) {
        if (product == null) {
            throw new InvalidProductException("Product is required.");
        }
        if (product.getSeller() != this) {
            throw new InvalidProductException("The product does not belong to this seller.");
        }
        products.add(product);
    }

    @Override
    public String toString() {
        return "Seller{id='" + getId() + "', fullName='" + getFullName() + "', email='" + getEmail()
                + "', warehouses=" + warehouses.size() + ", products=" + products.size() + "}";
    }
}
