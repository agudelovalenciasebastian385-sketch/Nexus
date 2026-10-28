package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the Order aggregate.
 *
 * <p>Order.buyer is stored as an identifier reference. The items live in the
 * {@code order_items} table; the invoice and the shipments reference the order from their
 * own tables, so no column here points back to them.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /** Order.buyer reference. */
    @Column(name = "buyer_id", nullable = false, length = 36)
    private String buyerId;

    /** OrderStatus catalog code. */
    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
