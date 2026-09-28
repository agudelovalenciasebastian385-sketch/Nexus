package nexusmarket.domain.ports.in;

/**
 * Input port: Login use case.
 *
 * <p>Authenticates a user with its email and password and returns a token.</p>
 */
public interface LoginUseCase {

    String login(String email, String rawPassword);
}
