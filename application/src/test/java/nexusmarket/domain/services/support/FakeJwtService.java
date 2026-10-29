package nexusmarket.domain.services.support;

import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.JwtServicePort;

/** Deterministic JwtServicePort: the token is {@code "token:<userId>:<version>"}. */
public class FakeJwtService implements JwtServicePort {

    private static final String PREFIX = "token:";

    @Override
    public String generateToken(User user) {
        return PREFIX + user.getId() + ":" + user.getAuthTokenVersion();
    }

    @Override
    public boolean validateToken(String token) {
        return token != null && token.startsWith(PREFIX) && token.split(":").length == 3;
    }

    @Override
    public String extractUserId(String token) {
        return validateToken(token) ? token.split(":")[1] : null;
    }

    @Override
    public Integer extractTokenVersion(String token) {
        return validateToken(token) ? Integer.valueOf(token.split(":")[2]) : null;
    }
}
