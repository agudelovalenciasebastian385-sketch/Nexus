package nexusmarket.infrastructure.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.services.support.FakePasswordService;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MemoryDemoDataSeederTest {

    private final ServiceHarness h = new ServiceHarness();
    private MemoryDemoDataSeeder seeder;

    @BeforeEach
    void seed() {
        String adminEmail = h.admin().getEmail();
        seeder = new MemoryDemoDataSeeder(h.users, h.registerSeller, h.registerStaffUser, h.registerBuyerUser,
                h.registerProduct, h.publishProduct, h.registerInitialStock, adminEmail);
        seeder.run();
    }

    private Seller seller() {
        String id = h.users.findByEmail(MemoryDemoDataSeeder.SELLER_EMAIL).orElseThrow().getId();
        return h.sellers.findById(id).orElseThrow();
    }

    @ParameterizedTest
    @CsvSource({"vendedor@example.com,Seller123*", "operador@example.com,Operator123*",
            "supervisor@example.com,Supervisor123*", "comprador@example.com,Buyer123*"})
    void createsEachUserWithItsDocumentedPassword(String email, String password) {
        assertThat(h.users.findByEmail(email).orElseThrow().getPassword()).isEqualTo(FakePasswordService.hash(password));
    }

    @Test
    void createsThreeProducts() {
        assertThat(h.products.findBySeller(seller())).hasSize(3);
    }

    @Test
    void publishesEveryProduct() {
        List<Product> products = h.products.findBySeller(seller());

        assertThat(products).allMatch(product -> product.getStatus() == ProductStatus.PUBLISHED);
    }

    @Test
    void loadsStockOfThePhysicalProducts() {
        assertThat(h.inventories.findByWarehouse(seller().getWarehouses().get(0))).hasSize(2);
    }

    @Test
    void runningItAgainDoesNotDuplicateTheData() {
        seeder.run();

        assertThat(h.products.findBySeller(seller())).hasSize(3);
    }
}
