package nexusmarket.domain.services.user;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Service;

/**
 * Loads the authenticated user by the identifier carried in its token
 * (user-services.md - LoadAuthenticatedUser). Internal service used by the JWT
 * filter of the security phase; it is not exposed in any role port.
 */
@Service
@RequiredArgsConstructor
public class LoadAuthenticatedUserService {

    private final UserRepositoryPort userRepositoryPort;

    public User loadById(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new UnauthorizedOperationException("The authenticated user id is required.");
        }
        return userRepositoryPort.findById(userId).orElseThrow(() -> new EntityNotFoundException("User", userId));
    }
}
