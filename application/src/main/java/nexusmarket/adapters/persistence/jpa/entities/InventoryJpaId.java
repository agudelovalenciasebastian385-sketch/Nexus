package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Composite key of an Inventory row: one record per (product, warehouse) pair.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class InventoryJpaId implements Serializable {

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "warehouse_id", nullable = false, length = 36)
    private String warehouseId;
}
