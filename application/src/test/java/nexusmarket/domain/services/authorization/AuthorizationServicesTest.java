package nexusmarket.domain.services.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.Order;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;

class AuthorizationServicesTest {

    private final ServiceHarness h = new ServiceHarness();

    // ------------------------------------------------------------------ RG-01: authenticated and active

    @Test
    void missingUserIsRejected() {
        assertThatThrownBy(() -> h.validateUserStatus.execute(null)).isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void userThatDoesNotExistIsRejected() {
        assertThatThrownBy(() -> h.validateUserStatus.execute(DomainFixtures.buyer()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void inactiveUserIsRejected() {
        Buyer buyer = h.buyer();
        buyer.changeStatus(UserStatus.INACTIVE);

        assertThatThrownBy(() -> h.validateUserStatus.execute(buyer)).isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void storedUserIsReturnedInsteadOfTheTokenCopy() {
        Buyer stored = h.buyer();
        Buyer copy = new Buyer(stored.getId(), stored.getIdentificationNumber(), "Old Name", stored.getEmail(),
                stored.getPassword(), UserStatus.ACTIVE, stored.getPrimaryAddress(), stored.getCommercialStatus());

        assertThat(h.validateUserStatus.execute(copy)).isSameAs(stored);
    }

    // ------------------------------------------------------------------ RG-02 / RG-03: role

    @Test
    void userWithAnAllowedRolePasses() {
        Buyer buyer = h.buyer();

        assertThat(h.validateRole.execute(buyer, SystemRole.BUYER, SystemRole.SUPERVISOR)).isSameAs(buyer);
    }

    @Test
    void userWithARoleThatIsNotAllowedIsRejected() {
        assertThatThrownBy(() -> h.validateRole.execute(h.buyer(), SystemRole.ADMINISTRATOR))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    // ------------------------------------------------------------------ RG-03: ownership

    @Test
    void buyerOwnsItsOrder() {
        Buyer buyer = h.buyer();
        Order order = DomainFixtures.physicalOrder(buyer);

        assertThatCode(() -> h.buyerOwnership.execute(buyer, order)).doesNotThrowAnyException();
    }

    @Test
    void buyerDoesNotOwnAnotherBuyersOrder() {
        Order order = DomainFixtures.physicalOrder(h.buyer());

        assertThatThrownBy(() -> h.buyerOwnership.execute(h.buyer(), order))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void buyerOwnershipDoesNotRestrictOtherRoles() {
        Order order = DomainFixtures.physicalOrder(h.buyer());

        assertThatCode(() -> h.buyerOwnership.execute(h.supervisor(), order)).doesNotThrowAnyException();
    }

    @Test
    void buyerCannotAccessAnotherBuyer() {
        assertThatThrownBy(() -> h.buyerOwnership.execute(h.buyer(), h.buyer()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void sellerCannotAccessAnotherSeller() {
        assertThatThrownBy(() -> h.sellerOwnership.execute(h.seller(), h.seller()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void sellerDoesNotOwnAMarketplaceWarehouse() {
        Warehouse hub = h.marketplaceWarehouse();

        assertThatThrownBy(() -> h.sellerOwnership.execute(h.seller(), hub))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void sellerOwnsItsWarehouse() {
        Seller seller = h.seller();

        assertThatCode(() -> h.sellerOwnership.execute(seller, h.firstWarehouse(seller))).doesNotThrowAnyException();
    }

    @Test
    void sellerOwnershipDoesNotRestrictOtherRoles() {
        assertThatCode(() -> h.sellerOwnership.execute(h.operator(), h.marketplaceWarehouse()))
                .doesNotThrowAnyException();
    }
}
