package nexusmarket.adapters.useCases;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nexusmarket.domain.models.AuthenticationResult;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.PhysicalProduct;
import nexusmarket.domain.models.Product;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import org.junit.jupiter.api.Test;

class PublicAccessUseCaseImplTest {

    private static final String EMAIL = "maria@gmail.com";
    private static final String PASSWORD = "Buyer123*";

    private final ServiceHarness h = new ServiceHarness();
    private final PublicAccessUseCaseImpl useCase = UseCaseFactory.publicAccess(h);

    private Buyer registeredBuyer() {
        return useCase.registerBuyerUser("1030", "Maria", EMAIL, PASSWORD, ADDRESS);
    }

    @Test
    void registerBuyerUserStoresTheBuyer() {
        Buyer buyer = registeredBuyer();

        assertThat(h.buyers.findById(buyer.getId())).contains(buyer);
    }

    @Test
    void loginReturnsATokenOfTheUser() {
        Buyer buyer = registeredBuyer();

        AuthenticationResult result = useCase.login(EMAIL, PASSWORD);

        assertThat(h.jwt.extractUserId(result.getToken())).isEqualTo(buyer.getId());
    }

    @Test
    void logoutIsAudited() {
        Buyer buyer = registeredBuyer();

        useCase.logout(buyer);

        assertThat(h.audit.actions()).contains(AuditAction.USER_LOGGED_OUT);
    }

    @Test
    void consultCatalogReturnsThePublishedProducts() {
        PhysicalProduct mouse = h.physicalProduct(h.seller(), "100.00");

        List<Product> catalog = useCase.consultCatalog();

        assertThat(catalog).containsExactly(mouse);
    }

    @Test
    void consultCatalogProductReturnsTheProduct() {
        PhysicalProduct mouse = h.physicalProduct(h.seller(), "100.00");

        assertThat(useCase.consultCatalogProduct(mouse.getId())).isSameAs(mouse);
    }
}
