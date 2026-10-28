package nexusmarket.adapters.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable persistence form of the Variant value object.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class VariantEmbeddable {

    @Column(name = "variant_type", length = 60)
    private String type;

    @Column(name = "variant_value", length = 120)
    private String value;
}
