package nexusmarket.adapters.rest;

import static nexusmarket.domain.DomainFixtures.PASSWORD_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import nexusmarket.adapters.security.dtos.AuthenticatedUserPrincipal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Runs the endpoint table of a controller. Each subclass declares a static {@code endpoints()} method,
 * and every endpoint appears three times in the report: status, delegation and response body.
 *
 * <p>The authenticated user reaches the controller as in production: an
 * {@link AuthenticatedUserPrincipal} wrapping the domain user, through the real security chain, so
 * the role rule of the route also applies.
 */
abstract class EndpointWebTestBase<P> extends SecuredWebTestBase {

    protected abstract P port();

    private ResultActions perform(EndpointCase<P> endpoint) throws Exception {
        endpoint.stubbing().on(port());
        MockHttpServletRequestBuilder request = request(endpoint.method(), endpoint.route(), endpoint.uriVariables());
        endpoint.params().forEach(request::param);
        if (endpoint.requestBody() != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(endpoint.requestBody());
        }
        if (endpoint.user() != null) {
            request.with(user(new AuthenticatedUserPrincipal(endpoint.user())));
        }
        return mockMvc.perform(request);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void answersTheDocumentedStatus(EndpointCase<P> endpoint) throws Exception {
        perform(endpoint).andExpect(status().is(endpoint.expectedStatus()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void delegatesToThePortWithExactArguments(EndpointCase<P> endpoint) throws Exception {
        perform(endpoint);

        endpoint.verification().on(port());
        verifyNoMoreInteractions(publicAccessPort, buyerPort, sellerPort, logisticsOperatorPort, administratorPort,
                supervisorPort);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void responseHasMainFieldsAndNoPassword(EndpointCase<P> endpoint) throws Exception {
        ResultActions result = perform(endpoint);
        String body = result.andReturn().getResponse().getContentAsString();

        if (endpoint.expectedStatus() == 204) {
            assertThat(body).isEmpty();
            return;
        }
        assertThat(endpoint.fields()).as("expected fields of %s", endpoint).isNotEmpty();
        for (Map.Entry<String, Object> field : endpoint.fields().entrySet()) {
            result.andExpect(jsonPath(field.getKey()).value(field.getValue()));
        }
        assertThat(body.toLowerCase()).doesNotContain("password").doesNotContain("authtokenversion");
        assertThat(body).doesNotContain(PASSWORD_HASH);
    }
}
