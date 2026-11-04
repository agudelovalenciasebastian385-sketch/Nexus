package nexusmarket.adapters.persistence.memory;

import java.util.Optional;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/** Users of any role, in memory. BuyerMemoryAdapter and SellerMemoryAdapter share this table, as they share the users table in MySQL. */
@Repository
@Profile("memory")
public class UserMemoryAdapter implements UserRepositoryPort {

    private final MemoryStore<String, User> users = new MemoryStore<>();

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
        return users.get(id);
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

    @Override
    public boolean existsByRole(SystemRole role) {
        return users.values().stream().anyMatch(user -> user.getRole() == role);
    }
}
