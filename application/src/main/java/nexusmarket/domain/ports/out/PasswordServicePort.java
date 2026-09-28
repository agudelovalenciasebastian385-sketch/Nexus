package nexusmarket.domain.ports.out;

/**
 * Output port: Password encryption and verification. The domain never depends on a hashing library.
 */
public interface PasswordServicePort {

    String encrypt(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
