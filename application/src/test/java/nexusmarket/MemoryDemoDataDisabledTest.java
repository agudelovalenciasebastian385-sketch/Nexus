package nexusmarket;

import static org.assertj.core.api.Assertions.assertThat;

import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.infrastructure.seed.MemoryDemoDataSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/** With app.seed.demo.enabled=false, the memory profile only has the initial administrator. */
@SpringBootTest(properties = "app.seed.demo.enabled=false")
@ActiveProfiles("memory")
class MemoryDemoDataDisabledTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private UserRepositoryPort users;

    @Test
    void theSampleDataSeederIsNotCreated() {
        assertThat(context.getBeanNamesForType(MemoryDemoDataSeeder.class)).isEmpty();
    }

    @Test
    void theSampleUsersDoNotExist() {
        assertThat(users.existsByEmail("vendedor@example.com")).isFalse();
    }

    @Test
    void theInitialAdministratorStillExists() {
        assertThat(users.existsByRole(SystemRole.ADMINISTRATOR)).isTrue();
    }
}
