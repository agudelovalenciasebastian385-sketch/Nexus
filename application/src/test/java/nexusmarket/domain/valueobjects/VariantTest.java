package nexusmarket.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.InvalidVariantException;
import org.junit.jupiter.api.Test;

class VariantTest {

    @Test
    void typeIsRequired() {
        assertThatThrownBy(() -> new Variant(" ", "black")).isInstanceOf(InvalidVariantException.class);
    }

    @Test
    void valueIsRequired() {
        assertThatThrownBy(() -> new Variant("color", null)).isInstanceOf(InvalidVariantException.class);
    }

    @Test
    void variantsWithTheSameTypeAndValueAreEqual() {
        assertThat(new Variant("color", "black")).isEqualTo(new Variant("color", "black"));
    }

    @Test
    void variantsWithDifferentValuesAreDifferent() {
        assertThat(new Variant("color", "black")).isNotEqualTo(new Variant("color", "white"));
    }
}
