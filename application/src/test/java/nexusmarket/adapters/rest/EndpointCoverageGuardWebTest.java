package nexusmarket.adapters.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Compares the endpoints registered by Spring MVC with the endpoint tables. A new endpoint without a
 * row, or a row left behind by a deleted endpoint, fails here. Spring's own handlers (such as /error)
 * are the only ones excluded, so a controller in any package of the project counts.
 */
class EndpointCoverageGuardWebTest extends SecuredWebTestBase {

    private static final int EXPECTED_ENDPOINTS = 108;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private Set<String> registeredEndpoints() {
        Set<String> endpoints = new TreeSet<>();
        handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
            if (!handler.getBeanType().getPackageName().startsWith("org.springframework")) {
                mapping.getMethodsCondition().getMethods().forEach(method ->
                        mapping.getPatternValues().forEach(pattern -> endpoints.add(method + " " + pattern)));
            }
        });
        return endpoints;
    }

    private static List<String> testedEndpoints() {
        return Stream.of(
                        AuthRestControllerWebTest.endpoints(),
                        PublicRestControllerWebTest.endpoints(),
                        BuyerRestControllerWebTest.endpoints(),
                        SellerRestControllerWebTest.endpoints(),
                        LogisticsOperatorRestControllerWebTest.endpoints(),
                        AdministratorRestControllerWebTest.endpoints(),
                        SupervisorRestControllerWebTest.endpoints())
                .flatMap(table -> table.map(EndpointCase::toString))
                .toList();
    }

    @Test
    void theApiHasExactlyTheExpectedNumberOfEndpoints() {
        assertThat(registeredEndpoints()).hasSize(EXPECTED_ENDPOINTS);
    }

    @Test
    void everyRegisteredEndpointHasATest() {
        List<String> missing = new ArrayList<>(registeredEndpoints());
        missing.removeAll(testedEndpoints());

        assertThat(missing).withFailMessage("Endpoints without a test: %s", missing).isEmpty();
    }

    @Test
    void everyTestedEndpointIsRegistered() {
        List<String> stale = new ArrayList<>(testedEndpoints());
        stale.removeAll(registeredEndpoints());

        assertThat(stale).withFailMessage("Tests of endpoints that do not exist: %s", stale).isEmpty();
    }

    @Test
    void noEndpointIsTestedTwice() {
        List<String> tested = testedEndpoints();

        assertThat(tested).doesNotHaveDuplicates();
    }
}
