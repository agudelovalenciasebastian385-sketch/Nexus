package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for an InventoryMovement.
 *
 * <p>Movements are append-only and reference their Inventory through (product_id,
 * warehouse_id). The surrogate id keeps the registration order.
 */
@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
public class InventoryMovementJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id", nullable = false, length = 36)
    private String productId;

    @Column(name = "warehouse_id", nullable = false, length = 36)
    private String warehouseId;

    /** MovementType catalog code. */
    @Column(name = "movement_type", nullable = false, length = 40)
    private String movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate;
}
