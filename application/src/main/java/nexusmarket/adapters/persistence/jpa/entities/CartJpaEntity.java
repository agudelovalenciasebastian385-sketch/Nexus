package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA persistence entity (repository DTO) for the Cart aggregate.
 *
 * <p>A buyer owns exactly one cart, so buyer_id is unique. The items live in the
 * {@code cart_items} table.
 */
@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
public class CartJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    /** Cart.buyer reference. */
    @Column(name = "buyer_id", nullable = false, unique = true, length = 36)
    private String buyerId;

    /**
     * Optimistic lock version. The adapter writes onto the entity loaded at the start of the use case,
     * so the commit fails (409) if another transaction changed the row meanwhile. DEFAULT 0 lets
     * ddl-auto add the column to existing rows.
     */
    @Version
    @Column(name = "version", nullable = false, columnDefinition = "BIGINT NOT NULL DEFAULT 0")
    private Long version;
}
