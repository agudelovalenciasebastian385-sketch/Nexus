package nexusmarket.adapters.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import nexusmarket.adapters.RolePortsWebTestBase;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.AuthenticationResult;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.services.user.LoadAuthenticatedUserService;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * The real security chain without a database: SecurityConfig, JwtAuthenticationFilter, the real
 * JwtProvider with a test key and LoadAuthenticatedUserService over a mocked UserRepositoryPort.
 */
@WebMvcTest
@Import({SecurityConfig.class, JwtProvider.class, LoadAuthenticatedUserService.class})
@TestPropertySource(properties = "app.security.jwt.secret=" + SecurityConfigWebTest.TEST_SECRET)
class SecurityConfigWebTest extends RolePortsWebTestBase {

    static final String TEST_SECRET = "NexusMarket-Security-Test-Key-With-More-Than-256-Bits";

    private static final String BUYER_ROUTE = "/api/v1/buyer/orders";

    @MockitoBean
    private UserRepositoryPort userRepositoryPort;

    @Autowired
    private JwtProvider jwtProvider;

    /** Stores the user in the mocked repository and returns a real token for it. */
    private String tokenOf(User user) {
        when(userRepositoryPort.findById(user.getId())).thenReturn(Optional.of(user));
        return jwtProvider.generateToken(user);
    }

    private static MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder request, String token) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    // ------------------------------------------------------------------ public routes

    @Test
    void catalogIsOpenWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/public/products")).andExpect(status().isOk());
    }

    @Test
    void loginIsOpenWithoutToken() throws Exception {
        when(publicAccessPort.login(any(), any()))
                .thenReturn(new AuthenticationResult(DomainFixtures.buyer(), "jwt-value"));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"a@b.com\", \"password\": \"Secret123*\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void buyerRegistrationIsOpenWithoutToken() throws Exception {
        when(publicAccessPort.registerBuyerUser(any(), any(), any(), any(), any())).thenReturn(DomainFixtures.buyer());

        mockMvc.perform(post("/api/v1/auth/register/buyers").contentType(MediaType.APPLICATION_JSON).content("""
                        {"identificationNumber": "1030", "fullName": "Maria", "email": "a@b.com",
                         "password": "Buyer123*", "primaryAddress": {"line1": "Calle 1", "city": "Medellin",
                         "state": "Antioquia", "country": "Colombia"}}"""))
                .andExpect(status().isCreated());
    }

    @Test
    void onlyReadingTheCatalogIsOpen() throws Exception {
        mockMvc.perform(post("/api/v1/public/products")).andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------ role routes

    static Stream<Arguments> roleRoutes() {
        return Stream.of(
                route(BUYER_ROUTE, DomainFixtures::buyer, DomainFixtures::seller),
                route("/api/v1/seller/products", DomainFixtures::seller, DomainFixtures::buyer),
                route("/api/v1/logistics/warehouses", DomainFixtures::operator, DomainFixtures::seller),
                route("/api/v1/admin/warehouses", DomainFixtures::administrator, DomainFixtures::supervisor),
                route("/api/v1/supervisor/warehouses", DomainFixtures::supervisor, DomainFixtures::administrator));
    }

    private static Arguments route(String path, Supplier<User> owner, Supplier<User> other) {
        return Arguments.of(path, owner, other);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roleRoutes")
    void roleRouteWithoutTokenAnswers401(String path, Supplier<User> owner, Supplier<User> other) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roleRoutes")
    void roleRouteWithAnotherRoleAnswers403(String path, Supplier<User> owner, Supplier<User> other) throws Exception {
        mockMvc.perform(withToken(get(path), tokenOf(other.get()))).andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roleRoutes")
    void roleRouteWithTheCorrectRoleAnswers200(String path, Supplier<User> owner, Supplier<User> other) throws Exception {
        mockMvc.perform(withToken(get(path), tokenOf(owner.get()))).andExpect(status().isOk());
    }

    // ------------------------------------------------------------------ JWT filter

    @Test
    void tokenWithAnOutdatedVersionAnswers401() throws Exception {
        Buyer buyer = DomainFixtures.buyer();
        String token = tokenOf(buyer);
        buyer.bumpAuthTokenVersion();

        mockMvc.perform(withToken(get(BUYER_ROUTE), token)).andExpect(status().isUnauthorized());
    }

    /** Same token version, so only the status check can reject it. */
    @Test
    void blockedUserAnswers401() throws Exception {
        Buyer active = DomainFixtures.buyer();
        String token = tokenOf(active);
        Buyer blocked = new Buyer(active.getId(), active.getIdentificationNumber(), active.getFullName(),
                active.getEmail(), active.getPassword(), UserStatus.BLOCKED, active.getPrimaryAddress(), null,
                CommercialStatus.ENABLED, null, active.getAuthTokenVersion());
        when(userRepositoryPort.findById(active.getId())).thenReturn(Optional.of(blocked));

        mockMvc.perform(withToken(get(BUYER_ROUTE), token)).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenOfAUserThatNoLongerExistsAnswers401() throws Exception {
        Buyer buyer = DomainFixtures.buyer();
        String token = jwtProvider.generateToken(buyer);
        when(userRepositoryPort.findById(buyer.getId())).thenReturn(Optional.empty());

        mockMvc.perform(withToken(get(BUYER_ROUTE), token)).andExpect(status().isUnauthorized());
    }

    @Test
    void alteredTokenAnswers401() throws Exception {
        String token = tokenOf(DomainFixtures.buyer());
        int signature = token.lastIndexOf('.') + 1;
        String altered = token.substring(0, signature) + (token.charAt(signature) == 'A' ? 'B' : 'A')
                + token.substring(signature + 1);

        mockMvc.perform(withToken(get(BUYER_ROUTE), altered)).andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenAnswers401() throws Exception {
        Buyer buyer = DomainFixtures.buyer();
        when(userRepositoryPort.findById(buyer.getId())).thenReturn(Optional.of(buyer));
        String expired = new JwtProvider(TEST_SECRET, -1).generateToken(buyer);

        mockMvc.perform(withToken(get(BUYER_ROUTE), expired)).andExpect(status().isUnauthorized());
    }

    @Test
    void authorizationHeaderThatIsNotBearerAnswers401() throws Exception {
        mockMvc.perform(get(BUYER_ROUTE).header(HttpHeaders.AUTHORIZATION, "Basic abc"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------------ logout

    @Test
    void logoutWithoutTokenAnswers401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutWithATokenAnswers204() throws Exception {
        mockMvc.perform(withToken(post("/api/v1/auth/logout"), tokenOf(DomainFixtures.seller())))
                .andExpect(status().isNoContent());
    }

    // ------------------------------------------------------------------ error body

    @Test
    void unauthorizedResponseUsesTheStandardBody() throws Exception {
        mockMvc.perform(get(BUYER_ROUTE))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value(BUYER_ROUTE));
    }

    @Test
    void forbiddenResponseUsesTheStandardBody() throws Exception {
        mockMvc.perform(withToken(get(BUYER_ROUTE), tokenOf(DomainFixtures.seller())))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.path").value(BUYER_ROUTE));
    }
}
