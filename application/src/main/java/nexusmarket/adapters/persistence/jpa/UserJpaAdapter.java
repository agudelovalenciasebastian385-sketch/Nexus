package nexusmarket.adapters.persistence.jpa;

import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.mappers.UserJpaMapper;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Repository;

/**
 * Output persistence adapter for User (every role) backed by Spring Data JPA (MySQL).
 *
 * <p>The users table holds every User subclass; the mapper rebuilds the right one from the
 * role column.
 */
@Repository
public class UserJpaAdapter implements UserRepositoryPort {

    private final SpringDataJpaUserRepository userRepository;

    public UserJpaAdapter(SpringDataJpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User save(User user) {
        userRepository.save(UserJpaMapper.toEntity(user));
        return user;
    }

    @Override
    public void update(User user) {
        userRepository.save(UserJpaMapper.toEntity(user));
    }

    @Override
    public Optional<User> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id).map(UserJpaMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email).map(UserJpaMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return email != null && userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByIdentificationNumber(String identificationNumber) {
        return identificationNumber != null && userRepository.existsByIdentificationNumber(identificationNumber);
    }
}
