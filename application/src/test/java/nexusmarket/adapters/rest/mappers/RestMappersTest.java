package nexusmarket.adapters.rest.mappers;

import static nexusmarket.domain.DomainFixtures.PASSWORD_HASH;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.AuthenticationResult;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.json.JsonMapper;

/**
 * The responses are serialized to JSON, as the API does, to check what really leaves the application.
 */
class RestMappersTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static String json(Object response) {
        return JSON.writeValueAsString(response);
    }

    private static Seller sellerWithWarehouse() {
        Seller seller = DomainFixtures.seller();
        seller.addWarehouse(DomainFixtures.sellerWarehouse(seller));
        return seller;
    }

    static Stream<Arguments> responsesWithUserData() {
        Buyer buyer = DomainFixtures.buyer();
        Seller seller = sellerWithWarehouse();
        return Stream.of(
                Arguments.of("user", json(UserRestMapper.toResponse(DomainFixtures.administrator()))),
                Arguments.of("buyer", json(BuyerRestMapper.toResponse(buyer))),
                Arguments.of("seller", json(SellerRestMapper.toResponse(seller))),
                Arguments.of("seller registration", json(SellerRestMapper.toRegistrationResponse(seller))),
                Arguments.of("login", json(UserRestMapper.toLoginResponse(new AuthenticationResult(buyer, "jwt"), 7200))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("responsesWithUserData")
    void responseDoesNotContainThePasswordHash(String description, String json) {
        assertThat(json).doesNotContain(PASSWORD_HASH);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("responsesWithUserData")
    void responseHasNoPasswordField(String description, String json) {
        assertThat(json.toLowerCase()).doesNotContain("password");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("responsesWithUserData")
    void responseDoesNotExposeTheTokenVersion(String description, String json) {
        assertThat(json).doesNotContain("authTokenVersion");
    }

    @Test
    void loginResponseCarriesTheToken() {
        AuthenticationResult result = new AuthenticationResult(DomainFixtures.buyer(), "jwt-value");

        assertThat(json(UserRestMapper.toLoginResponse(result, 7200))).contains("\"token\":\"jwt-value\"");
    }

    @Test
    void userRoleIsExposedAsItsCode() {
        assertThat(json(UserRestMapper.toResponse(DomainFixtures.operator())))
                .contains("\"role\":\"LOGISTICS_OPERATOR\"");
    }

    @Test
    void userStatusIsExposedAsItsCode() {
        assertThat(json(UserRestMapper.toResponse(DomainFixtures.administrator()))).contains("\"status\":\"ACTIVE\"");
    }

    @Test
    void commercialStatusIsExposedAsItsCode() {
        Buyer buyer = DomainFixtures.buyer(UserStatus.ACTIVE, CommercialStatus.RESTRICTED);

        assertThat(json(BuyerRestMapper.toResponse(buyer))).contains("\"commercialStatus\":\"RESTRICTED\"");
    }

    @Test
    void productTypeIsExposedAsItsCode() {
        PhysicalProduct product = DomainFixtures.publishedPhysical(DomainFixtures.seller());

        assertThat(json(ProductRestMapper.toResponse(product))).contains("\"type\":\"PHYSICAL\"");
    }

    @Test
    void productStatusIsExposedAsItsCode() {
        PhysicalProduct product = DomainFixtures.publishedPhysical(DomainFixtures.seller());

        assertThat(json(ProductRestMapper.toResponse(product))).contains("\"status\":\"PUBLISHED\"");
    }

    @Test
    void productShowsItsSellerByIdAndNameOnly() {
        Seller seller = DomainFixtures.seller();

        assertThat(json(ProductRestMapper.toResponse(DomainFixtures.publishedPhysical(seller))))
                .contains(seller.getId())
                .doesNotContain(seller.getEmail());
    }

    @Test
    void sellerRegistrationIncludesTheFirstWarehouse() {
        Seller seller = sellerWithWarehouse();

        assertThat(SellerRestMapper.toRegistrationResponse(seller).getFirstWarehouse().getId())
                .isEqualTo(seller.getWarehouses().get(0).getId());
    }
}
