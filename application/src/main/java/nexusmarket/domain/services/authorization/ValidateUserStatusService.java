package nexusmarket.domain.services.authorization;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Service;

/**
 * RG-01: every operation must be executed by an authenticated user that is
 * currently {@code ACTIVE}.
 *
 * <p>The user is reloaded through {@link UserRepositoryPort} so that a user
 * blocked after obtaining its token cannot operate. The stored user is returned
 * and is the one used by the calling service (authorization-services.md).</p>
 */
@Service
@RequiredArgsConstructor
public class ValidateUserStatusService {

    private final UserRepositoryPort userRepositoryPort;

    public User execute(User user) {
        if (user == null) {
            throw new UnauthorizedOperationException("An authenticated user is required (RG-01).");
        }
        User stored = userRepositoryPort.findById(user.getId())
                .orElseThrow(() -> new UnauthorizedOperationException(
                        "The authenticated user " + user.getId() + " does not exist (RG-01)."));
        if (!stored.isActive()) {
            throw new UnauthorizedOperationException(
                    "User " + stored.getId() + " is not active and cannot perform operations (RG-01).");
        }
        return stored;
    }
}
