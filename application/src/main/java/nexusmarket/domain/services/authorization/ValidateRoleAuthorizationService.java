package nexusmarket.domain.services.authorization;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * RG-02 and RG-03: validates RG-01 and that the single role of the user is one
 * of the roles allowed for the operation (authorization-services.md).
 */
@Service
@RequiredArgsConstructor
public class ValidateRoleAuthorizationService {

    private final ValidateUserStatusService validateUserStatusService;

    /** Returns the stored (authoritative) user when it is active and holds an allowed role. */
    public User execute(User user, SystemRole... allowedRoles) {
        User stored = validateUserStatusService.execute(user);
        if (!Arrays.asList(allowedRoles).contains(stored.getRole())) {
            throw new UnauthorizedOperationException("Role " + stored.getRole().getCode()
                    + " is not allowed to perform this operation (RG-03).");
        }
        return stored;
    }
}
