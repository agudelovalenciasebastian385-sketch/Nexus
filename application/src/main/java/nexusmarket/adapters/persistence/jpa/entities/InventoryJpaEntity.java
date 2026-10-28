package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the Inventory aggregate.
 *
 * <p>Keyed by (product_id, warehouse_id). The movement history lives in the
 * {@code inventory_movements} table, which references the same pair.
 */
@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
public class InventoryJpaEntity {

    @EmbeddedId
    private InventoryJpaId id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    /** InventoryCondition catalog code. CONDITION is a reserved word in MySQL. */
    @Column(name = "inventory_condition", nullable = false, length = 40)
    private String condition;
}
