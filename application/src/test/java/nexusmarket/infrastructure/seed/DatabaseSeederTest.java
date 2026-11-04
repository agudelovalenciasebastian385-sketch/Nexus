package nexusmarket.infrastructure.seed;

import static org.assertj.core.api.Assertions.assertThat;

import nexusmarket.domain.models.User;
import nexusmarket.domain.services.support.FakePasswordService;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.SystemRole;
import org.junit.jupiter.api.Test;

class DatabaseSeederTest {

    private static final String EMAIL = "seed@nexusmarket.com";

    private final ServiceHarness h = new ServiceHarness();

    private DatabaseSeeder seeder(String email) {
        return new DatabaseSeeder(h.users, h.passwords, "1000000000", "Administrator", email, "Admin123*");
    }

    @Test
    void createsTheInitialAdministrator() {
        seeder(EMAIL).run();

        assertThat(h.users.existsByRole(SystemRole.ADMINISTRATOR)).isTrue();
    }

    @Test
    void storesTheHashOfThePassword() {
        seeder(EMAIL).run();

        User administrator = h.users.findByEmail(EMAIL).orElseThrow();
        assertThat(administrator.getPassword()).isEqualTo(FakePasswordService.hash("Admin123*"));
    }

    @Test
    void doesNothingWhenAnAdministratorExists() {
        h.admin();

        seeder(EMAIL).run();

        assertThat(h.users.existsByEmail(EMAIL)).isFalse();
    }

    @Test
    void doesNotCreateTheAdministratorWhenItsEmailIsTaken() {
        String taken = h.buyer().getEmail();

        seeder(taken).run();

        assertThat(h.users.existsByRole(SystemRole.ADMINISTRATOR)).isFalse();
    }
}
