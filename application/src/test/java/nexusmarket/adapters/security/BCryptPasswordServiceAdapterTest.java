package nexusmarket.adapters.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptPasswordServiceAdapterTest {

    private static final String RAW = "Secret123*";

    private final BCryptPasswordServiceAdapter adapter = new BCryptPasswordServiceAdapter(new BCryptPasswordEncoder());

    @Test
    void hashIsDifferentFromThePlainText() {
        assertThat(adapter.encrypt(RAW)).isNotEqualTo(RAW);
    }

    @Test
    void hashHasTheBcryptFormat() {
        assertThat(adapter.encrypt(RAW)).startsWith("$2a$");
    }

    @Test
    void theCorrectPasswordMatches() {
        assertThat(adapter.matches(RAW, adapter.encrypt(RAW))).isTrue();
    }

    @Test
    void aWrongPasswordDoesNotMatch() {
        assertThat(adapter.matches("Wrong123*", adapter.encrypt(RAW))).isFalse();
    }

    @Test
    void aNullPasswordDoesNotMatch() {
        assertThat(adapter.matches(null, adapter.encrypt(RAW))).isFalse();
    }

    @Test
    void aNullHashDoesNotMatch() {
        assertThat(adapter.matches(RAW, null)).isFalse();
    }

    @Test
    void twoHashesOfTheSamePasswordAreDifferent() {
        assertThat(adapter.encrypt(RAW)).isNotEqualTo(adapter.encrypt(RAW));
    }

    @Test
    void bothHashesOfTheSamePasswordVerify() {
        String first = adapter.encrypt(RAW);
        String second = adapter.encrypt(RAW);

        assertThat(adapter.matches(RAW, first) && adapter.matches(RAW, second)).isTrue();
    }
}
