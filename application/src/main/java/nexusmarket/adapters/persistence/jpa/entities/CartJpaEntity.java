package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
}
