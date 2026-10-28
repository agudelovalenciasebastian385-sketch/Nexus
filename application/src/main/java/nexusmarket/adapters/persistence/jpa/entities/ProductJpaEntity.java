package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the Product hierarchy.
 *
 * <p>PhysicalProduct and DigitalProduct share the single {@code products} table and are told
 * apart by the {@code product_type} column (ProductType code). Product.seller is stored as an
 * identifier reference and the variants live in the {@code product_variants} collection table.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class ProductJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /** ProductType catalog code; discriminates the Product subclass. */
    @Column(name = "product_type", nullable = false, length = 40)
    private String productType;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    /** Product.seller reference. */
    @Column(name = "seller_id", nullable = false, length = 36)
    private String sellerId;

    /** ProductStatus catalog code. */
    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_variants", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "variant_order")
    private List<VariantEmbeddable> variants = new ArrayList<>();
}
