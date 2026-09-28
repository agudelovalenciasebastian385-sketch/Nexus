package nexusmarket.domain.ports.out;

import nexusmarket.domain.models.User;

/**
 * Output port: Token generation and validation. The domain never depends on a JWT library.
 */
public interface JwtServicePort {

    String generateToken(User user);

    boolean validateToken(String token);

    User reconstructUser(String token);
}
