package nexusmarket.adapters.rest;

import nexusmarket.adapters.RolePortsWebTestBase;
import nexusmarket.adapters.security.JwtProvider;
import nexusmarket.adapters.security.SecurityConfig;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.services.user.LoadAuthenticatedUserService;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Web layer with every controller and the real security chain, without a database. All the subclasses
 * share one cached Spring context, which is also what lets the guard test see the 108 endpoints.
 */
@WebMvcTest
@Import({SecurityConfig.class, JwtProvider.class, LoadAuthenticatedUserService.class})
@TestPropertySource(properties = {
        "app.security.jwt.secret=NexusMarket-Endpoint-Test-Key-With-More-Than-256-Bits",
        "app.security.jwt.expiration-minutes=" + SecuredWebTestBase.EXPIRATION_MINUTES})
public abstract class SecuredWebTestBase extends RolePortsWebTestBase {

    static final int EXPIRATION_MINUTES = 90;

    @MockitoBean
    private UserRepositoryPort userRepositoryPort;
}
