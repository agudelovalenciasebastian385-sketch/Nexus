package nexusmarket.adapters.persistence.jpa.repositories;

import java.util.Optional;
import nexusmarket.adapters.persistence.jpa.entities.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the users table (every User subclass).
 */
public interface SpringDataJpaUserRepository extends JpaRepository<UserJpaEntity, String> {

    Optional<UserJpaEntity> findByIdAndRole(String id, String role);

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByIdentificationNumber(String identificationNumber);

    boolean existsByRole(String role);
}
