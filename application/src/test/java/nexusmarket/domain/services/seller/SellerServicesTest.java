package nexusmarket.domain.services.seller;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.exceptions.UserAlreadyExistsException;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.Seller;
import nexusmarket.domain.models.Warehouse;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.WarehouseType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SellerServicesTest {

    private final ServiceHarness h = new ServiceHarness();
    private final Administrator admin = h.admin();

    private Seller registered(String document, String email) {
        return h.registerSeller.registerSeller(admin, document, "Tech Store", email, "Seller123*", "Main Warehouse",
                ADDRESS);
    }

    @Nested
    class RegisterSeller {

        @Test
        void sellerIsStoredWithItsFirstSellerWarehouse() {
            Seller seller = registered("900", "store@gmail.com");

            Warehouse first = seller.getWarehouses().get(0);
            assertThat(h.sellers.findById(seller.getId())).contains(seller);
            assertThat(h.warehouses.findById(first.getId())).contains(first);
            assertThat(first.getType()).isEqualTo(WarehouseType.SELLER);
        }

        @Test
        void registrationAuditsTheSellerAndTheWarehouse() {
            registered("900", "store@gmail.com");

            assertThat(h.audit.actions()).containsSubsequence(AuditAction.SELLER_REGISTERED,
                    AuditAction.WAREHOUSE_REGISTERED);
        }

        @Test
        void emailIsComparedNormalized() {
            registered("900", "store@gmail.com");

            assertThatThrownBy(() -> registered("901", "STORE@gmail.com"))
                    .isInstanceOf(UserAlreadyExistsException.class);
        }

        @Test
        void duplicateIdentificationNumberIsRejected() {
            registered("900", "store@gmail.com");

            assertThatThrownBy(() -> registered("900", "other@gmail.com")).isInstanceOf(UserAlreadyExistsException.class);
        }

        @Test
        void emptyPasswordIsRejected() {
            assertThatThrownBy(() -> h.registerSeller.registerSeller(admin, "900", "Tech", "store@gmail.com", " ", "W",
                    ADDRESS)).isInstanceOf(InvalidUserException.class);
        }

        @Test
        void onlyAnAdministratorRegistersSellers() {
            assertThatThrownBy(() -> h.registerSeller.registerSeller(h.seller(), "900", "Tech", "store@gmail.com",
                    "Seller123*", "W", ADDRESS)).isInstanceOf(UnauthorizedOperationException.class);
        }
    }

    @Nested
    class ConsultAndUpdate {

        private final Seller seller = h.seller();

        @Test
        void sellerConsultsItself() {
            assertThat(h.consultSeller.consultSeller(seller, seller.getId())).isSameAs(seller);
        }

        @Test
        void sellerCannotConsultAnotherSeller() {
            assertThatThrownBy(() -> h.consultSeller.consultSeller(h.seller(), seller.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void administratorConsultsAnySeller() {
            assertThat(h.consultSeller.consultSeller(admin, seller.getId())).isSameAs(seller);
        }

        @Test
        void consultingAnUnknownSellerIsReported() {
            assertThatThrownBy(() -> h.consultSeller.consultSeller(admin, "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void sellerUpdatesItsNameAndIsAudited() {
            h.updateSeller.updateSeller(seller, seller.getId(), "New Name");

            assertThat(seller.getFullName()).isEqualTo("New Name");
            assertThat(h.audit.actions()).contains(AuditAction.SELLER_UPDATED);
        }

        @Test
        void administratorUpdatesAnySeller() {
            h.updateSeller.updateSeller(admin, seller.getId(), "Renamed");

            assertThat(seller.getFullName()).isEqualTo("Renamed");
        }

        @Test
        void sellerCannotUpdateAnotherSeller() {
            assertThatThrownBy(() -> h.updateSeller.updateSeller(h.seller(), seller.getId(), "Hacked"))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void blankNameIsRejected() {
            assertThatThrownBy(() -> h.updateSeller.updateSeller(seller, seller.getId(), " "))
                    .isInstanceOf(InvalidUserException.class);
        }

        @Test
        void updatingAnUnknownSellerIsReported() {
            assertThatThrownBy(() -> h.updateSeller.updateSeller(admin, "missing", "Name"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }
}
