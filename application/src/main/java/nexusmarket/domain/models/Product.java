package nexusmarket.domain.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import nexusmarket.domain.exceptions.InvalidProductException;
import nexusmarket.domain.exceptions.InvalidProductStatusException;
import nexusmarket.domain.valueobjects.ProductStatus;
import nexusmarket.domain.valueobjects.ProductType;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Product (abstract).
 *
 * <p>Represents a good offered in the Marketplace catalog. The catalog
 * distinguishes between physical products (requiring inventory and dispatch)
 * and digital products (immediate delivery after payment).</p>
 *
 * <p>This class cannot be instantiated directly. A product is published by
 * exactly one {@link Seller}.</p>
 */
public abstract class Product {

    private final String id;
    private String name;
    private String description;
    /** Current unit price; copied to the cart item when the product is added to a cart. */
    private BigDecimal price;
    private final Seller seller;
    /** Differences such as color, size, model, etc. Empty by default. */
    private final List<Variant> variants = new ArrayList<>();
    private ProductStatus status;

    protected Product(String id, String name, String description, BigDecimal price, Seller seller,
                    List<Variant> variants, ProductStatus status) {
        if (id == null || id.isBlank()) {
            throw new InvalidProductException("Product id cannot be empty.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Product name cannot be empty.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidProductException("Product description cannot be empty.");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = requirePrice(price);
        if (seller == null) {
            throw new InvalidProductException("Product seller is required.");
        }
        this.seller = seller;
        if (status == null) {
            throw new InvalidProductException("Product status is required.");
        }
        this.status = status;
        if (variants != null) {
            for (Variant variant : variants) {
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

    public BigDecimal getPrice() {
        return price;
    }

    public Seller getSeller() {
        return seller;
    }

    public List<Variant> getVariants() {
        return List.copyOf(variants);
    }

    public ProductStatus getStatus() {
        return status;
    }

    /** Type of the product, determined by its specialization (physical or digital). */
    public abstract ProductType getType();

    /** Updates the commercial information of the product. */
    public void updateInfo(String name, String description, BigDecimal price, List<Variant> variants) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Product name cannot be empty.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidProductException("Product description cannot be empty.");
        }
        this.name = name;
        this.description = description;
        this.price = requirePrice(price);
        this.variants.clear();
        if (variants != null) {
            for (Variant variant : variants) {
                addVariant(variant);
            }
        }
    }

    public void addVariant(Variant variant) {
        if (variant == null) {
            throw new InvalidProductException("Product variant is required.");
        }
        if (variants.contains(variant)) {
            throw new InvalidProductException("The variant is already registered for this product.");
        }
        variants.add(variant);
    }

    private static BigDecimal requirePrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new InvalidProductException("Product price must be zero or positive.");
        }
        return price;
    }

    /** Makes a draft or suspended product visible in the public catalog. */
    public void publish() {
        if (status != ProductStatus.DRAFT && status != ProductStatus.SUSPENDED) {
            throw new InvalidProductStatusException("Only a draft or suspended product can be published; current status is "
                    + status.getCode() + ".");
        }
        this.status = ProductStatus.PUBLISHED;
    }

    /** Temporarily unavailable in the catalog. Only a published product can be suspended. */
    public void suspend() {
        if (status != ProductStatus.PUBLISHED) {
            throw new InvalidProductStatusException("Only a published product can be suspended; current status is "
                    + status.getCode() + ".");
        }
        this.status = ProductStatus.SUSPENDED;
    }

    /** Permanently withdrawn from the catalog. A discontinued product cannot change status again. */
    public void discontinue() {
        if (status == ProductStatus.DISCONTINUED) {
            throw new InvalidProductStatusException("The product is already discontinued.");
        }
        this.status = ProductStatus.DISCONTINUED;
    }

    /** Indicates whether the given variant is offered by this product. */
    public boolean hasVariant(Variant variant) {
        return variants.contains(variant);
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
