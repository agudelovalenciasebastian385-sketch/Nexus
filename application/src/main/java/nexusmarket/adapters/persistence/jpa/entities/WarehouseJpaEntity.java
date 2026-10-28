package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the Warehouse aggregate.
 *
 * <p>Warehouse.seller is stored as an identifier reference and is null for a Marketplace
 * warehouse. Warehouse.inventories is not stored here: each Inventory row references its
 * warehouse.
 */
@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
public class WarehouseJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "line1", column = @Column(name = "location_line1", length = 200)),
            @AttributeOverride(name = "line2", column = @Column(name = "location_line2", length = 200)),
            @AttributeOverride(name = "city", column = @Column(name = "location_city", length = 100)),
            @AttributeOverride(name = "state", column = @Column(name = "location_state", length = 100)),
            @AttributeOverride(name = "country", column = @Column(name = "location_country", length = 100))
    })
    private AddressEmbeddable location;

    /** WarehouseType catalog code. */
    @Column(name = "type", nullable = false, length = 40)
    private String type;

    /** Warehouse.seller reference (null for a Marketplace warehouse). */
    @Column(name = "seller_id", length = 36)
    private String sellerId;
}
