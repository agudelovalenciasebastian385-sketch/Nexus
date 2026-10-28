package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the Shipment aggregate.
 *
 * <p>Order, origin warehouse and logistics operator are stored as identifier references.
 */
@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
public class ShipmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /** Shipment.order reference. */
    @Column(name = "order_id", nullable = false, length = 36)
    private String orderId;

    /** Shipment.originWarehouse reference. */
    @Column(name = "origin_warehouse_id", nullable = false, length = 36)
    private String originWarehouseId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "line1", column = @Column(name = "delivery_line1", length = 200)),
            @AttributeOverride(name = "line2", column = @Column(name = "delivery_line2", length = 200)),
            @AttributeOverride(name = "city", column = @Column(name = "delivery_city", length = 100)),
            @AttributeOverride(name = "state", column = @Column(name = "delivery_state", length = 100)),
            @AttributeOverride(name = "country", column = @Column(name = "delivery_country", length = 100))
    })
    private AddressEmbeddable deliveryAddress;

    /** Shipment.logisticsOperator reference. */
    @Column(name = "logistics_operator_id", nullable = false, length = 36)
    private String logisticsOperatorId;

    /** ShipmentStatus catalog code. */
    @Column(name = "status", nullable = false, length = 40)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
