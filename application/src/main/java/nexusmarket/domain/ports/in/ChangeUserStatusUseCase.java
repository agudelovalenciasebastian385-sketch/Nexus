package nexusmarket.domain.ports.in;

import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.UserStatus;

/**
 * Input port: ChangeUserStatus use case.
 *
 * <p>Changes the operational status of a user ({@code ACTIVE}, {@code BLOCKED}, {@code INACTIVE}).</p>
 */
public interface ChangeUserStatusUseCase {

    User changeUserStatus(User requestingUser, String userId, UserStatus newStatus);
}
