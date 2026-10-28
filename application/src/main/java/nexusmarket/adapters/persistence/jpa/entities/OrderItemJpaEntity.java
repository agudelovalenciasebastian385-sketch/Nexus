package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for an OrderItem, stored with its order id.
 *
 * <p>origin_warehouse_id is set for physical products and null for digital ones.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    /** OrderItem.product reference. */
    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    /** Optional variant (both columns null when the item has no variant). */
    @Embedded
    private VariantEmbeddable variant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    /** OrderItem.originWarehouse reference (physical products only). */
    @Column(name = "origin_warehouse_id", length = 36)
    private String originWarehouseId;
}
