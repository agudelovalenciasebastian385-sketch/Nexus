package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable persistence form of the Address value object.
 *
 * <p>Every owner overrides the column names with its own prefix (primary_address_, location_,
 * delivery_) so the address sits next to the owner's other columns.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class AddressEmbeddable {

    @Column(name = "line1", length = 200)
    private String line1;

    @Column(name = "line2", length = 200)
    private String line2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;
}
