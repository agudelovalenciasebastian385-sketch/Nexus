package nexusmarket.domain.services.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.UserRepositoryPort;

/**
 * In-memory UserRepositoryPort. Its map is also the store of InMemoryBuyerRepository and
 * InMemorySellerRepository, like the single users table in MySQL. Objects are stored and returned by
 * reference (the real adapter also returns the saved instance).
 */
public class InMemoryUserRepository implements UserRepositoryPort {

    final Map<String, User> users = new LinkedHashMap<>();

    @Override
    public User save(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void update(User user) {
        users.put(user.getId(), user);
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public boolean existsByIdentificationNumber(String identificationNumber) {
        return users.values().stream().anyMatch(user -> user.getIdentificationNumber().equals(identificationNumber));
    }
}
