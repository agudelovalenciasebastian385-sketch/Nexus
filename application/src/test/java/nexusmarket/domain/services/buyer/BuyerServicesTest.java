package nexusmarket.domain.services.buyer;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.Address;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.CommercialStatus;
import org.junit.jupiter.api.Test;

class BuyerServicesTest {

    private static final Address HOME = new Address("Carrera 45 # 30-12", null, "Bogota", "Cundinamarca", "Colombia");
    private static final Address OFFICE = new Address("Calle 100 # 10-20", "Of 301", "Bogota", "Cundinamarca",
            "Colombia");

    private final ServiceHarness h = new ServiceHarness();
    private final Buyer buyer = h.buyer();

    @Test
    void buyerConsultsItsOwnProfile() {
        assertThat(h.consultBuyer.consultBuyer(buyer, buyer.getId())).isSameAs(buyer);
    }

    @Test
    void buyerCannotConsultAnotherBuyer() {
        assertThatThrownBy(() -> h.consultBuyer.consultBuyer(h.buyer(), buyer.getId()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void administratorConsultsAnyBuyer() {
        assertThat(h.consultBuyer.consultBuyer(h.admin(), buyer.getId())).isSameAs(buyer);
    }

    @Test
    void sellerIsNotAllowedToConsultBuyers() {
        assertThatThrownBy(() -> h.consultBuyer.consultBuyer(h.seller(), buyer.getId()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void consultingAnUnknownBuyerIsReported() {
        assertThatThrownBy(() -> h.consultBuyer.consultBuyer(h.admin(), "missing"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void profileUpdateReplacesNameAndAddresses() {
        buyer.addAdditionalAddress(HOME);

        h.updateBuyerProfile.updateBuyerProfile(buyer, "Maria G.", HOME, List.of(OFFICE));

        assertThat(buyer.getFullName()).isEqualTo("Maria G.");
        assertThat(buyer.getPrimaryAddress()).isEqualTo(HOME);
        assertThat(buyer.getAdditionalAddresses()).containsExactly(OFFICE);
    }

    @Test
    void profileUpdateIsAudited() {
        h.updateBuyerProfile.updateBuyerProfile(buyer, "Maria G.", ADDRESS, List.of(HOME, OFFICE));

        assertThat(h.audit.last(AuditAction.BUYER_PROFILE_UPDATED).getDetails())
                .containsEntry("additionalAddresses", "2");
    }

    @Test
    void duplicateAdditionalAddressesAreRejected() {
        assertThatThrownBy(() -> h.updateBuyerProfile.updateBuyerProfile(buyer, "Maria", ADDRESS, List.of(HOME, HOME)))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void blankNameIsRejected() {
        assertThatThrownBy(() -> h.updateBuyerProfile.updateBuyerProfile(buyer, " ", ADDRESS, List.of()))
                .isInstanceOf(InvalidUserException.class);
    }

    @Test
    void onlyABuyerUpdatesItsProfile() {
        assertThatThrownBy(() -> h.updateBuyerProfile.updateBuyerProfile(h.admin(), "Admin", ADDRESS, List.of()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void administratorChangesTheCommercialStatusAndItIsAudited() {
        h.changeBuyerCommercialStatus.changeBuyerCommercialStatus(h.admin(), buyer.getId(), CommercialStatus.SUSPENDED);

        assertThat(buyer.getCommercialStatus()).isEqualTo(CommercialStatus.SUSPENDED);
        assertThat(h.audit.last(AuditAction.BUYER_COMMERCIAL_STATUS_CHANGED).getDetails())
                .containsEntry("from", "ENABLED").containsEntry("to", "SUSPENDED");
    }

    @Test
    void onlyAnAdministratorChangesTheCommercialStatus() {
        assertThatThrownBy(() -> h.changeBuyerCommercialStatus.changeBuyerCommercialStatus(h.supervisor(),
                buyer.getId(), CommercialStatus.SUSPENDED)).isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void changingTheCommercialStatusOfAnUnknownBuyerIsReported() {
        assertThatThrownBy(() -> h.changeBuyerCommercialStatus.changeBuyerCommercialStatus(h.admin(), "missing",
                CommercialStatus.SUSPENDED)).isInstanceOf(EntityNotFoundException.class);
    }
}
