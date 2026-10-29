package nexusmarket.domain.services.support;

import nexusmarket.domain.ports.out.PasswordServicePort;

/** Deterministic PasswordServicePort: the "hash" of a password is {@code "hashed:" + password}. */
public class FakePasswordService implements PasswordServicePort {

    public static String hash(String rawPassword) {
        return "hashed:" + rawPassword;
    }

    @Override
    public String encrypt(String rawPassword) {
        return hash(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return rawPassword != null && hash(rawPassword).equals(encodedPassword);
    }
}
