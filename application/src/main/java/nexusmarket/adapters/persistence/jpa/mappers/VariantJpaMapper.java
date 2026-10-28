package nexusmarket.adapters.persistence.jpa.mappers;

import nexusmarket.adapters.persistence.jpa.entities.VariantEmbeddable;
import nexusmarket.domain.valueobjects.Variant;

/**
 * Bidirectional mapper between the Variant value object and its embeddable.
 *
 * <p>An item without a variant is stored with both variant columns null.
 */
public final class VariantJpaMapper {

    private VariantJpaMapper() {
    }

    public static VariantEmbeddable toEmbeddable(Variant domain) {
        if (domain == null) {
            return null;
        }
        VariantEmbeddable embeddable = new VariantEmbeddable();
        embeddable.setType(domain.getType());
        embeddable.setValue(domain.getValue());
        return embeddable;
    }

    public static Variant toDomain(VariantEmbeddable embeddable) {
        if (embeddable == null || embeddable.getType() == null) {
            return null;
        }
        return new Variant(embeddable.getType(), embeddable.getValue());
    }
}
