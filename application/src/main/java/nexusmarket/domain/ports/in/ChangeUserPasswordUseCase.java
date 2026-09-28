package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.User;

/**
 * Input port: ChangeUserPassword use case.
 *
 * <p>Changes the password of the requesting user.</p>
 */
public interface ChangeUserPasswordUseCase {

    void changeUserPassword(User requestingUser, String currentPassword, String newPassword);
}
