package nexusmarket.domain.services.user;

import static nexusmarket.domain.DomainFixtures.ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.InvalidCredentialsException;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.exceptions.UserAlreadyExistsException;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.models.AuthenticationResult;
import nexusmarket.domain.models.Buyer;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Supervisor;
import nexusmarket.domain.models.User;
import nexusmarket.domain.services.support.FakePasswordService;
import nexusmarket.domain.services.support.ServiceHarness;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.CommercialStatus;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UserServicesTest {

    private static final String PASSWORD = "Buyer123*";

    private final ServiceHarness h = new ServiceHarness();
    private final Administrator admin = h.admin();

    private Buyer registeredBuyer(String document, String email) {
        return h.registerBuyerUser.registerBuyerUser(document, "Maria Gomez", email, PASSWORD, ADDRESS);
    }

    @Nested
    class RegisterBuyer {

        @Test
        void buyerIsStoredActiveAndEnabled() {
            Buyer buyer = registeredBuyer("1030", "maria@gmail.com");

            assertThat(h.buyers.findById(buyer.getId())).contains(buyer);
            assertThat(buyer.getStatus()).isEqualTo(UserStatus.ACTIVE);
            assertThat(buyer.getCommercialStatus()).isEqualTo(CommercialStatus.ENABLED);
        }

        @Test
        void passwordIsStoredEncrypted() {
            Buyer buyer = registeredBuyer("1030", "maria@gmail.com");

            assertThat(buyer.getPassword()).isEqualTo(FakePasswordService.hash(PASSWORD));
        }

        @Test
        void buyerGetsItsOwnEmptyCart() {
            Buyer buyer = registeredBuyer("1030", "maria@gmail.com");

            assertThat(h.carts.findByBuyer(buyer)).hasValueSatisfying(cart -> assertThat(cart.isEmpty()).isTrue());
        }

        @Test
        void registrationIsAudited() {
            Buyer buyer = registeredBuyer("1030", "maria@gmail.com");

            assertThat(h.audit.last(AuditAction.USER_REGISTERED).getEntityId()).isEqualTo(buyer.getId());
        }

        @Test
        void emailIsComparedNormalized() {
            registeredBuyer("1030", "maria@gmail.com");

            assertThatThrownBy(() -> registeredBuyer("2040", "  MARIA@Gmail.com "))
                    .isInstanceOf(UserAlreadyExistsException.class);
        }

        @Test
        void duplicateIdentificationNumberIsRejected() {
            registeredBuyer("1030", "maria@gmail.com");

            assertThatThrownBy(() -> registeredBuyer("1030", "other@gmail.com"))
                    .isInstanceOf(UserAlreadyExistsException.class);
        }

        @Test
        void emptyPasswordIsRejected() {
            assertThatThrownBy(() -> h.registerBuyerUser.registerBuyerUser("1030", "Maria", "maria@gmail.com", " ",
                    ADDRESS)).isInstanceOf(InvalidUserException.class);
        }
    }

    @Nested
    class RegisterStaff {

        @Test
        void administratorRegistersALogisticsOperator() {
            User user = h.registerStaffUser.registerStaffUser(admin, SystemRole.LOGISTICS_OPERATOR, "5001", "Laura",
                    "laura@nexusmarket.com", "Operator123*");

            assertThat(user).isInstanceOf(LogisticsOperator.class);
            assertThat(h.audit.actions()).contains(AuditAction.STAFF_USER_REGISTERED);
        }

        @Test
        void administratorRegistersASupervisor() {
            User user = h.registerStaffUser.registerStaffUser(admin, SystemRole.SUPERVISOR, "5002", "Pedro",
                    "pedro@nexusmarket.com", "Supervisor123*");

            assertThat(user).isInstanceOf(Supervisor.class);
        }

        @Test
        void staffUserCannotHaveAnotherRole() {
            assertThatThrownBy(() -> h.registerStaffUser.registerStaffUser(admin, SystemRole.BUYER, "5003", "Ana",
                    "ana@nexusmarket.com", "Staff123*")).isInstanceOf(InvalidUserException.class);
        }

        @Test
        void duplicateEmailIsRejected() {
            registeredBuyer("1030", "maria@gmail.com");

            assertThatThrownBy(() -> h.registerStaffUser.registerStaffUser(admin, SystemRole.SUPERVISOR, "5004",
                    "Maria", "Maria@Gmail.com", "Staff123*")).isInstanceOf(UserAlreadyExistsException.class);
        }

        @Test
        void duplicateIdentificationNumberIsRejected() {
            registeredBuyer("1030", "maria@gmail.com");

            assertThatThrownBy(() -> h.registerStaffUser.registerStaffUser(admin, SystemRole.SUPERVISOR, "1030",
                    "Maria", "other@nexusmarket.com", "Staff123*")).isInstanceOf(UserAlreadyExistsException.class);
        }

        @Test
        void emptyPasswordIsRejected() {
            assertThatThrownBy(() -> h.registerStaffUser.registerStaffUser(admin, SystemRole.SUPERVISOR, "5005",
                    "Pedro", "pedro@nexusmarket.com", "")).isInstanceOf(InvalidUserException.class);
        }

        @Test
        void onlyAnAdministratorRegistersStaff() {
            assertThatThrownBy(() -> h.registerStaffUser.registerStaffUser(h.supervisor(), SystemRole.SUPERVISOR,
                    "5006", "Pedro", "pedro@nexusmarket.com", "Staff123*"))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }
    }

    @Nested
    class LoginAndLogout {

        @Test
        void loginReturnsTheUserAndATokenWithItsVersion() {
            Buyer buyer = registeredBuyer("1030", "maria@gmail.com");

            AuthenticationResult result = h.login.login("maria@gmail.com", PASSWORD);

            assertThat(result.getAuthenticatedUser()).isSameAs(buyer);
            assertThat(h.jwt.extractTokenVersion(result.getToken())).isEqualTo(1);
        }

        @Test
        void loginIsCaseInsensitiveOnTheEmail() {
            registeredBuyer("1030", "maria@gmail.com");

            assertThat(h.login.login(" Maria@GMAIL.com", PASSWORD).getToken()).isNotBlank();
        }

        @Test
        void loginIsAudited() {
            registeredBuyer("1030", "maria@gmail.com");

            h.login.login("maria@gmail.com", PASSWORD);

            assertThat(h.audit.actions()).contains(AuditAction.USER_LOGGED_IN);
        }

        @Test
        void wrongPasswordIsRejected() {
            registeredBuyer("1030", "maria@gmail.com");

            assertThatThrownBy(() -> h.login.login("maria@gmail.com", "wrong"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        void unknownEmailIsRejectedWithTheSameError() {
            assertThatThrownBy(() -> h.login.login("nobody@gmail.com", PASSWORD))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        void missingCredentialsAreRejected() {
            assertThatThrownBy(() -> h.login.login(null, PASSWORD)).isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        void blockedUserCannotLogIn() {
            h.blocked(registeredBuyer("1030", "maria@gmail.com"));

            assertThatThrownBy(() -> h.login.login("maria@gmail.com", PASSWORD))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void logoutInvalidatesPreviousTokensAndIsAudited() {
            Buyer buyer = registeredBuyer("1030", "maria@gmail.com");

            h.logout.logout(buyer);

            assertThat(buyer.getAuthTokenVersion()).isEqualTo(2);
            assertThat(h.audit.actions()).contains(AuditAction.USER_LOGGED_OUT);
        }

        @Test
        void logoutRequiresAUser() {
            assertThatThrownBy(() -> h.logout.logout(null)).isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void logoutOfAnUnknownUserIsReported() {
            assertThatThrownBy(() -> h.logout.logout(nexusmarket.domain.DomainFixtures.buyer()))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class LoadAndConsult {

        @Test
        void authenticatedUserIsLoadedById() {
            assertThat(h.loadAuthenticatedUser.loadById(admin.getId())).isSameAs(admin);
        }

        @Test
        void loadingWithoutIdIsRejected() {
            assertThatThrownBy(() -> h.loadAuthenticatedUser.loadById(" "))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void loadingAnUnknownUserIsReported() {
            assertThatThrownBy(() -> h.loadAuthenticatedUser.loadById("missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void userConsultsItself() {
            Buyer buyer = h.buyer();

            assertThat(h.consultUser.consultUser(buyer, buyer.getId())).isSameAs(buyer);
        }

        @Test
        void userCannotConsultAnotherUser() {
            Buyer buyer = h.buyer();

            assertThatThrownBy(() -> h.consultUser.consultUser(h.buyer(), buyer.getId()))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void supervisorConsultsAnyUser() {
            Buyer buyer = h.buyer();

            assertThat(h.consultUser.consultUser(h.supervisor(), buyer.getId())).isSameAs(buyer);
        }

        @Test
        void consultingAnUnknownUserIsReported() {
            assertThatThrownBy(() -> h.consultUser.consultUser(admin, "missing"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class ChangeStatusAndPassword {

        @Test
        void administratorBlocksAUserAndInvalidatesItsTokens() {
            Buyer buyer = h.buyer();

            h.changeUserStatus.changeUserStatus(admin, buyer.getId(), UserStatus.BLOCKED);

            assertThat(buyer.getStatus()).isEqualTo(UserStatus.BLOCKED);
            assertThat(buyer.getAuthTokenVersion()).isEqualTo(2);
        }

        @Test
        void statusChangeIsAuditedWithBothStatuses() {
            Buyer buyer = h.buyer();

            h.changeUserStatus.changeUserStatus(admin, buyer.getId(), UserStatus.INACTIVE);

            assertThat(h.audit.last(AuditAction.USER_STATUS_CHANGED).getDetails())
                    .containsEntry("from", "ACTIVE").containsEntry("to", "INACTIVE");
        }

        @Test
        void administratorCannotChangeItsOwnStatus() {
            assertThatThrownBy(() -> h.changeUserStatus.changeUserStatus(admin, admin.getId(), UserStatus.BLOCKED))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void onlyAnAdministratorChangesStatuses() {
            Buyer buyer = h.buyer();

            assertThatThrownBy(() -> h.changeUserStatus.changeUserStatus(h.supervisor(), buyer.getId(),
                    UserStatus.BLOCKED)).isInstanceOf(UnauthorizedOperationException.class);
        }

        @Test
        void changingTheStatusOfAnUnknownUserIsReported() {
            assertThatThrownBy(() -> h.changeUserStatus.changeUserStatus(admin, "missing", UserStatus.BLOCKED))
                    .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        void userChangesItsPassword() {
            Buyer buyer = registeredBuyer("1030", "maria@gmail.com");

            h.changeUserPassword.changeUserPassword(buyer, PASSWORD, "NewPass123*");

            assertThat(buyer.getPassword()).isEqualTo(FakePasswordService.hash("NewPass123*"));
            assertThat(buyer.getAuthTokenVersion()).isEqualTo(2);
            assertThat(h.audit.actions()).contains(AuditAction.USER_PASSWORD_CHANGED);
        }

        @Test
        void wrongCurrentPasswordIsRejected() {
            Buyer buyer = registeredBuyer("1030", "maria@gmail.com");

            assertThatThrownBy(() -> h.changeUserPassword.changeUserPassword(buyer, "wrong", "NewPass123*"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        void emptyNewPasswordIsRejected() {
            Buyer buyer = registeredBuyer("1030", "maria@gmail.com");

            assertThatThrownBy(() -> h.changeUserPassword.changeUserPassword(buyer, PASSWORD, " "))
                    .isInstanceOf(InvalidUserException.class);
        }

        @Test
        void blockedUserCannotChangeItsPassword() {
            Buyer buyer = h.blocked(registeredBuyer("1030", "maria@gmail.com"));

            assertThatThrownBy(() -> h.changeUserPassword.changeUserPassword(buyer, PASSWORD, "NewPass123*"))
                    .isInstanceOf(UnauthorizedOperationException.class);
        }
    }
}
