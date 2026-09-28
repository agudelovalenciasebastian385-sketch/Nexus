package nexusmarket.domain.ports.out;

import java.util.Optional;

import nexusmarket.domain.models.User;

/**
 * Output port: Persistence and queries of every {@code User} (any subclass); uniqueness checks of email and identification number.
 */
public interface UserRepositoryPort {

    User save(User user);

    void update(User user);

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByIdentificationNumber(String identificationNumber);
}
