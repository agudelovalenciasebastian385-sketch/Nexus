package nexusmarket.domain.valueobjects;

import java.util.Objects;

import nexusmarket.domain.exceptions.InvalidVariantException;

/**
 * Variant (value object).
 *
 * <p>Represents a specific difference of a product, such as color, size or
 * model (e.g. type "Color", value "Red").</p>
 *
 * <p>Immutable and compared by value.</p>
 */
public final class Variant {

    private final String type;
    private final String value;

    public Variant(String type, String value) {
        if (type == null || type.isBlank()) {
            throw new InvalidVariantException("Variant type cannot be empty.");
        }
        if (value == null || value.isBlank()) {
            throw new InvalidVariantException("Variant value cannot be empty.");
        }
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Variant)) {
            return false;
        }
        Variant otherVariant = (Variant) other;
        return type.equals(otherVariant.type) && value.equals(otherVariant.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return type + ": " + value;
    }
}
