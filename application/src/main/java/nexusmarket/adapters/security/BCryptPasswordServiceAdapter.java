package nexusmarket.adapters.security;

import nexusmarket.domain.ports.out.PasswordServicePort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Output adapter for password hashing with BCrypt.
 *
 * <p>Owns its encoder until SecurityConfig (REST API step) exposes a shared PasswordEncoder bean.
 */
@Component
public class BCryptPasswordServiceAdapter implements PasswordServicePort {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String encrypt(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
