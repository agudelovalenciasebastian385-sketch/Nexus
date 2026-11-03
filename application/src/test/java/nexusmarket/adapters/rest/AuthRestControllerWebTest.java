package nexusmarket.adapters.rest;

import static nexusmarket.adapters.rest.RestResponseFixtures.ADDRESS_BODY;
import static nexusmarket.adapters.rest.RestResponseFixtures.BUYER;
import static nexusmarket.adapters.rest.RestResponseFixtures.BUYER_JSON;
import static nexusmarket.adapters.rest.RestResponseFixtures.REQUEST_ADDRESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.AuthenticationResult;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.ports.in.PublicAccessPort;

class AuthRestControllerWebTest extends EndpointWebTestBase<PublicAccessPort> {

    private static final Seller USER = DomainFixtures.seller();

    private static final EndpointCase.Routes<PublicAccessPort> ANONYMOUS = new EndpointCase.Routes<>(null);
    private static final EndpointCase.Routes<PublicAccessPort> AUTHENTICATED = new EndpointCase.Routes<>(USER);

    @Override
    protected PublicAccessPort port() {
        return publicAccessPort;
    }

    static Stream<EndpointCase<PublicAccessPort>> endpoints() {
        return Stream.of(
                ANONYMOUS.post("/api/v1/auth/login")
                        .body("{\"email\": \"a@b.com\", \"password\": \"Secret123*\"}")
                        .stub(port -> when(port.login(any(), any())).thenReturn(new AuthenticationResult(BUYER, "jwt-value")))
                        .calls(port -> verify(port).login(eq("a@b.com"), eq("Secret123*")))
                        .json(Map.of("token", "jwt-value", "tokenType", "Bearer",
                                "expiresIn", SecuredWebTestBase.EXPIRATION_MINUTES * 60, "user.id", BUYER.getId())),
                AUTHENTICATED.post("/api/v1/auth/logout")
                        .calls(port -> verify(port).logout(same(USER)))
                        .status(204),
                ANONYMOUS.post("/api/v1/auth/register/buyers")
                        .body("""
                                {"identificationNumber": "1030", "fullName": "Maria", "email": "a@b.com",
                                 "password": "Buyer123*", "primaryAddress": %s}""".formatted(ADDRESS_BODY))
                        .stub(port -> when(port.registerBuyerUser(any(), any(), any(), any(), any())).thenReturn(BUYER))
                        .calls(port -> verify(port).registerBuyerUser(eq("1030"), eq("Maria"), eq("a@b.com"),
                                eq("Buyer123*"), eq(REQUEST_ADDRESS)))
                        .status(201)
                        .json(BUYER_JSON));
    }
}
