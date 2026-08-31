package nexusmarket.domain.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import nexusmarket.domain.valueobjects.ProductStatus;

/**
 * Product (abstract).
 *
 * <p>Represents a good offered in the Marketplace catalog. The catalog
 * distinguishes between physical products (requiring inventory and dispatch)
 * and digital products (immediate delivery after payment).</p>
 *
 * <p>This class cannot be instantiated directly. A product is published by
 * exactly one {@link Seller}.</p>
 *
 * <p>The {@code Variant} value objects of the Domain Model are modeled as
 * Strings in this delivery (the value object class is out of scope).</p>
 */
public abstract class Product {

    private final String id;
    private String name;
    private String description;
    private final Seller seller;
    /** Differences such as color, size, model, etc. Empty by default. */
    private final List<String> variants = new ArrayList<>();
    private ProductStatus status;

    protected Product(String id, String name, String description, Seller seller,
                    List<String> variants, ProductStatus status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product id cannot be empty.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Product description cannot be empty.");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.seller = Objects.requireNonNull(seller, "Product seller is required.");
        this.status = Objects.requireNonNull(status, "Product status is required.");
        if (variants != null) {
            for (String variant : variants) {
                addVariant(variant);
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Seller getSeller() {
        return seller;
    }

    public List<String> getVariants() {
        return List.copyOf(variants);
    }

    public ProductStatus getStatus() {
        return status;
    }

    /** Updates the commercial information of the product. */
    public void updateInfo(String name, String description, List<String> variants) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Product description cannot be empty.");
        }
        this.name = name;
        this.description = description;
        this.variants.clear();
        if (variants != null) {
            for (String variant : variants) {
                addVariant(variant);
            }
        }
    }

    public void addVariant(String variant) {
        if (variant == null || variant.isBlank()) {
            throw new IllegalArgumentException("Product variant cannot be empty.");
        }
        if (variants.contains(variant)) {
            throw new IllegalArgumentException("The variant is already registered for this product.");
        }
        variants.add(variant);
    }

    /** Temporarily unavailable in the catalog. */
    public void suspend() {
        this.status = ProductStatus.SUSPENDED;
    }

    /** Permanently withdrawn from the catalog. */
    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
    }

    /** Makes the product visible again in the public catalog. */
    public void republish() {
        if (status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("A discontinued product cannot be republished.");
        }
        this.status = ProductStatus.PUBLISHED;
    }

    public boolean isPublished() {
        return status == ProductStatus.PUBLISHED;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Product)) {
            return false;
        }
        return id.equals(((Product) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "', name='" + name + "', status=" + status + "}";
    }
}
