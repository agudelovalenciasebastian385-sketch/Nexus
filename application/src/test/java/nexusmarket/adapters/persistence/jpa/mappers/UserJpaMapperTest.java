package nexusmarket.adapters.persistence.jpa.mappers;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static nexusmarket.domain.DomainFixtures.PASSWORD_HASH;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Supervisor;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class UserJpaMapperTest {

    private static final Address HOME = new Address("Carrera 45", "Apto 2", "Bogota", "Cundinamarca", "Colombia");
    private static final Address OFFICE = new Address("Calle 100", null, "Bogota", "Cundinamarca", "Colombia");

    private static Buyer buyer() {
        return new Buyer("u-b", "1001", "Maria Gomez", "maria@gmail.com", PASSWORD_HASH, UserStatus.BLOCKED, ADDRESS,
                List.of(HOME, OFFICE), CommercialStatus.RESTRICTED, null, 5);
    }

    static Stream<User> users() {
        return Stream.of(
                buyer(),
                new Seller("u-s", "1002", "Tech Store", "store@gmail.com", PASSWORD_HASH, UserStatus.ACTIVE, 5),
                new LogisticsOperator("u-o", "1003", "Laura", "laura@nexusmarket.com", PASSWORD_HASH,
                        UserStatus.INACTIVE, 5),
                new Administrator("u-a", "1004", "Admin", "admin@nexusmarket.com", PASSWORD_HASH, UserStatus.ACTIVE, 5),
                new Supervisor("u-u", "1005", "Pedro", "pedro@nexusmarket.com", PASSWORD_HASH, UserStatus.ACTIVE, 5));
    }

    private static User roundTrip(User user) {
        return UserJpaMapper.toDomain(UserJpaMapper.toEntity(user));
    }

    @ParameterizedTest
    @MethodSource("users")
    void everySubclassComesBackAsItsOwnClass(User user) {
        assertThat(roundTrip(user)).isExactlyInstanceOf(user.getClass());
    }

    @ParameterizedTest
    @MethodSource("users")
    void commonFieldsSurviveTheRoundTrip(User user) {
        assertThat(roundTrip(user))
                .usingRecursiveComparison()
                .comparingOnlyFields("id", "identificationNumber", "fullName", "email", "password", "role", "status")
                .isEqualTo(user);
    }

    @ParameterizedTest
    @MethodSource("users")
    void authTokenVersionSurvivesTheRoundTrip(User user) {
        assertThat(roundTrip(user).getAuthTokenVersion()).isEqualTo(5);
    }

    @Test
    void buyerKeepsItsPrimaryAddress() {
        assertThat(((Buyer) roundTrip(buyer())).getPrimaryAddress()).isEqualTo(ADDRESS);
    }

    @Test
    void buyerKeepsItsAdditionalAddressesInOrder() {
        assertThat(((Buyer) roundTrip(buyer())).getAdditionalAddresses()).containsExactly(HOME, OFFICE);
    }

    @Test
    void buyerKeepsItsCommercialStatus() {
        assertThat(((Buyer) roundTrip(buyer())).getCommercialStatus()).isEqualTo(CommercialStatus.RESTRICTED);
    }

    @Test
    void optionalAddressLine2StaysNull() {
        assertThat(((Buyer) roundTrip(buyer())).getAdditionalAddresses().get(1).getLine2()).isNull();
    }

    @Test
    void userThatIsNotABuyerHasNoBuyerColumns() {
        Seller seller = new Seller("u-s", "1002", "Tech Store", "store@gmail.com", PASSWORD_HASH, UserStatus.ACTIVE);

        assertThat(UserJpaMapper.toEntity(seller).getCommercialStatus()).isNull();
    }
}
