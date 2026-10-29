package nexusmarket.domain.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.InvalidAddressException;
import org.junit.jupiter.api.Test;

class AddressTest {

    @Test
    void line1IsRequired() {
        assertThatThrownBy(() -> new Address(" ", null, "Medellin", "Antioquia", "Colombia"))
                .isInstanceOf(InvalidAddressException.class);
    }

    @Test
    void cityIsRequired() {
        assertThatThrownBy(() -> new Address("Calle 1", null, null, "Antioquia", "Colombia"))
                .isInstanceOf(InvalidAddressException.class);
    }

    @Test
    void countryIsRequired() {
        assertThatThrownBy(() -> new Address("Calle 1", null, "Medellin", "Antioquia", ""))
                .isInstanceOf(InvalidAddressException.class);
    }

    @Test
    void blankLine2BecomesNull() {
        assertThat(new Address("Calle 1", "  ", "Medellin", "Antioquia", "Colombia").getLine2()).isNull();
    }

    @Test
    void addressesWithTheSameValuesAreEqual() {
        assertThat(new Address("Calle 1", "Apto 2", "Medellin", "Antioquia", "Colombia"))
                .isEqualTo(new Address("Calle 1", "Apto 2", "Medellin", "Antioquia", "Colombia"));
    }
}
