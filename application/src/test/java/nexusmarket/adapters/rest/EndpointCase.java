package nexusmarket.adapters.rest;

import java.util.LinkedHashMap;
import java.util.Map;
import nexusmarket.domain.models.User;
import org.springframework.http.HttpMethod;

/**
 * One row of an endpoint table: the request, the stubbing of the role port, the exact call expected on
 * the port and the expected response. {@link #toString()} is the name of the case in the test report.
 */
final class EndpointCase<P> {

    /** A stubbing or a verification over the mocked role port. */
    interface PortAction<P> {
        void on(P port);
    }

    private final HttpMethod method;
    private final String route;
    private final Object[] uriVariables;
    private final User user;
    private final Map<String, String> params = new LinkedHashMap<>();
    private final Map<String, Object> fields = new LinkedHashMap<>();
    private String body;
    private PortAction<P> stub = port -> { };
    private PortAction<P> verification;
    private int status = 200;

    private EndpointCase(HttpMethod method, String route, Object[] uriVariables, User user) {
        this.method = method;
        this.route = route;
        this.uriVariables = uriVariables;
        this.user = user;
    }

    /** Builds the cases of a controller, all requested by the same authenticated user (null: anonymous). */
    static final class Routes<P> {

        private final User user;

        Routes(User user) {
            this.user = user;
        }

        EndpointCase<P> get(String route, Object... uriVariables) {
            return new EndpointCase<>(HttpMethod.GET, route, uriVariables, user);
        }

        EndpointCase<P> post(String route, Object... uriVariables) {
            return new EndpointCase<>(HttpMethod.POST, route, uriVariables, user);
        }

        EndpointCase<P> put(String route, Object... uriVariables) {
            return new EndpointCase<>(HttpMethod.PUT, route, uriVariables, user);
        }

        EndpointCase<P> patch(String route, Object... uriVariables) {
            return new EndpointCase<>(HttpMethod.PATCH, route, uriVariables, user);
        }

        EndpointCase<P> delete(String route, Object... uriVariables) {
            return new EndpointCase<>(HttpMethod.DELETE, route, uriVariables, user);
        }
    }

    EndpointCase<P> body(String json) {
        this.body = json;
        return this;
    }

    EndpointCase<P> param(String name, String value) {
        params.put(name, value);
        return this;
    }

    EndpointCase<P> stub(PortAction<P> action) {
        this.stub = action;
        return this;
    }

    EndpointCase<P> calls(PortAction<P> action) {
        this.verification = action;
        return this;
    }

    EndpointCase<P> status(int expected) {
        this.status = expected;
        return this;
    }

    /** Expected fields of a JSON object response, with paths relative to the root. */
    EndpointCase<P> json(Map<String, Object> expected) {
        expected.forEach((path, value) -> fields.put("$." + path, value));
        return this;
    }

    /** Expected fields of a JSON array response holding exactly one element. */
    EndpointCase<P> jsonList(Map<String, Object> expected) {
        fields.put("$.length()", 1);
        expected.forEach((path, value) -> fields.put("$[0]." + path, value));
        return this;
    }

    HttpMethod method() {
        return method;
    }

    String route() {
        return route;
    }

    Object[] uriVariables() {
        return uriVariables;
    }

    User user() {
        return user;
    }

    Map<String, String> params() {
        return params;
    }

    Map<String, Object> fields() {
        return fields;
    }

    String requestBody() {
        return body;
    }

    PortAction<P> stubbing() {
        return stub;
    }

    PortAction<P> verification() {
        return verification;
    }

    int expectedStatus() {
        return status;
    }

    @Override
    public String toString() {
        return method + " " + route;
    }
}
