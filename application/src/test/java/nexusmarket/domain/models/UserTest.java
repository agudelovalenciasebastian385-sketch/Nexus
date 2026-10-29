package nexusmarket.domain.models;

import static nexusmarket.domain.DomainFixtures.PASSWORD_HASH;
import static nexusmarket.domain.DomainFixtures.administrator;
import static nexusmarket.domain.DomainFixtures.buyer;
import static nexusmarket.domain.DomainFixtures.operator;
import static nexusmarket.domain.DomainFixtures.seller;
import static nexusmarket.domain.DomainFixtures.supervisor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Supplier;
import java.util.stream.Stream;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

    private static Administrator administratorWith(String email, String password, Integer tokenVersion) {
        return new Administrator("admin-1", "1000", "Admin", email, password, UserStatus.ACTIVE, tokenVersion);
    }

    // ------------------------------------------------------------------ email

    @Test
    void normalizeEmailTrimsAndLowerCases() {
        assertThat(User.normalizeEmail("  Juan.Perez@Gmail.COM ")).isEqualTo("juan.perez@gmail.com");
    }

    @Test
    void normalizeEmailOfNullIsNull() {
        assertThat(User.normalizeEmail(null)).isNull();
    }

    @Test
    void constructorStoresTheNormalizedEmail() {
        assertThat(administratorWith(" Admin@NexusMarket.com", PASSWORD_HASH, 1).getEmail())
                .isEqualTo("admin@nexusmarket.com");
    }

    @Test
    void invalidEmailIsRejected() {
        assertThatThrownBy(() -> administratorWith("not-an-email", PASSWORD_HASH, 1))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void emptyPasswordIsRejected() {
        assertThatThrownBy(() -> administratorWith("admin@nexusmarket.com", " ", 1))
                .isInstanceOf(InvalidUserException.class);
    }

    // ------------------------------------------------------------------ token version

    @Test
    void newUserStartsAtTokenVersionOne() {
        assertThat(administrator().getAuthTokenVersion()).isEqualTo(1);
    }

    @Test
    void changingTheStatusBumpsTheTokenVersion() {
        User user = administrator();

        user.changeStatus(UserStatus.BLOCKED);

        assertThat(user.getStatus()).isEqualTo(UserStatus.BLOCKED);
        assertThat(user.getAuthTokenVersion()).isEqualTo(2);
    }

    @Test
    void changingThePasswordBumpsTheTokenVersion() {
        User user = administrator();

        user.changePassword("$2a$10$newHash");

        assertThat(user.getPassword()).isEqualTo("$2a$10$newHash");
        assertThat(user.getAuthTokenVersion()).isEqualTo(2);
    }

    @Test
    void bumpAuthTokenVersionIncrementsByOne() {
        User user = administratorWith("admin@nexusmarket.com", PASSWORD_HASH, 7);

        user.bumpAuthTokenVersion();

        assertThat(user.getAuthTokenVersion()).isEqualTo(8);
    }

    @Test
    void rehydrationWithoutTokenVersionIsRejected() {
        assertThatThrownBy(() -> administratorWith("admin@nexusmarket.com", PASSWORD_HASH, null))
                .isInstanceOf(InvalidUserException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rehydrationWithTokenVersionBelowOneIsRejected(int version) {
        assertThatThrownBy(() -> administratorWith("admin@nexusmarket.com", PASSWORD_HASH, version))
                .isInstanceOf(InvalidUserException.class);
    }

    // ------------------------------------------------------------------ status and role

    @Test
    void userIsActiveOnlyWhenItsStatusIsActive() {
        User user = administrator();
        user.changeStatus(UserStatus.INACTIVE);

        assertThat(user.isActive()).isFalse();
    }

    static Stream<Arguments> subclasses() {
        return Stream.of(
                Arguments.of((Supplier<User>) () -> buyer(), SystemRole.BUYER),
                Arguments.of((Supplier<User>) () -> seller(), SystemRole.SELLER),
                Arguments.of((Supplier<User>) () -> operator(), SystemRole.LOGISTICS_OPERATOR),
                Arguments.of((Supplier<User>) () -> administrator(), SystemRole.ADMINISTRATOR),
                Arguments.of((Supplier<User>) () -> supervisor(), SystemRole.SUPERVISOR));
    }

    @ParameterizedTest
    @MethodSource("subclasses")
    void eachSubclassHasItsFixedRole(Supplier<User> user, SystemRole role) {
        assertThat(user.get().getRole()).isEqualTo(role);
    }
}
