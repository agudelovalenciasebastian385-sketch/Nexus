package nexusmarket.infrastructure.seed;

import java.util.UUID;
import nexusmarket.adapters.persistence.jpa.repositories.SpringDataJpaUserRepository;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.ports.out.PasswordServicePort;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the initial Administrator at startup when no Administrator exists yet.
 *
 * <p>Credentials come from {@code app.seed.admin.*}; the password is hashed through
 * {@link PasswordServicePort}. Idempotent: once any Administrator exists nothing is created.
 * UserRepositoryPort has no query by role, so the check uses the Spring Data repository
 * directly (technical infrastructure, outside the domain).
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final SpringDataJpaUserRepository userJpaRepository;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final String identificationNumber;
    private final String fullName;
    private final String email;
    private final String password;

    public DatabaseSeeder(SpringDataJpaUserRepository userJpaRepository,
                          UserRepositoryPort userRepositoryPort,
                          PasswordServicePort passwordServicePort,
                          @Value("${app.seed.admin.identification-number}") String identificationNumber,
                          @Value("${app.seed.admin.full-name}") String fullName,
                          @Value("${app.seed.admin.email}") String email,
                          @Value("${app.seed.admin.password}") String password) {
        this.userJpaRepository = userJpaRepository;
        this.userRepositoryPort = userRepositoryPort;
        this.passwordServicePort = passwordServicePort;
        this.identificationNumber = identificationNumber;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (userJpaRepository.existsByRole(SystemRole.ADMINISTRATOR.getCode())) {
            LOGGER.info("DatabaseSeeder: an Administrator already exists; nothing to seed.");
            return;
        }
        if (userRepositoryPort.existsByEmail(email)
                || userRepositoryPort.existsByIdentificationNumber(identificationNumber)) {
            LOGGER.warn("DatabaseSeeder: no Administrator exists, but email {} or identification {} is already "
                    + "used by another user; initial Administrator not created.", email, identificationNumber);
            return;
        }
        Administrator administrator = new Administrator(UUID.randomUUID().toString(), identificationNumber,
                fullName, email, passwordServicePort.encrypt(password), UserStatus.ACTIVE);
        userRepositoryPort.save(administrator);
        LOGGER.info("DatabaseSeeder: initial Administrator created (id={}, email={}).", administrator.getId(), email);
    }
}
