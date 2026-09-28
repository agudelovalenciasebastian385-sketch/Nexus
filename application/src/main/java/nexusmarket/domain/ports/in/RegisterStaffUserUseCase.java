package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.SystemRole;

/**
 * Input port: RegisterStaffUser use case.
 *
 * <p>Registers a staff user: a logistics operator or a supervisor.</p>
 */
public interface RegisterStaffUserUseCase {

    User registerStaffUser(User requestingUser, SystemRole role, String identificationNumber,
                           String fullName, String email, String rawPassword);
}
