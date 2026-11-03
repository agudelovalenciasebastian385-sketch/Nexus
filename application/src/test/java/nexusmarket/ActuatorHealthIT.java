package nexusmarket;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nexusmarket.adapters.security.dtos.AuthenticatedUserPrincipal;
import nexusmarket.domain.DomainFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The health endpoint used by the Docker healthcheck. It needs the full context, because the
 * {@code @WebMvcTest} slice does not load the actuator.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorHealthIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthIsOpenWithoutTokenAndUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    /** Authenticated, so 404 proves that the endpoint is not exposed rather than only protected. */
    @Test
    void otherActuatorEndpointsAreNotExposedEvenToAnAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/actuator/env")
                        .with(user(new AuthenticatedUserPrincipal(DomainFixtures.administrator()))))
                .andExpect(status().isNotFound());
    }
}
