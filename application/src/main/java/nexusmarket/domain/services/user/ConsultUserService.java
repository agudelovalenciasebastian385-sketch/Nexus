package nexusmarket.domain.services.user;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.ConsultUserUseCase;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Retrieves the information of a user (user-services.md - ConsultUser). Users
 * other than administrators and supervisors can only consult themselves.
 */
@Service
@RequiredArgsConstructor
public class ConsultUserService implements ConsultUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;

    @Override
    public User consultUser(User requestingUser, String userId) {
        User requester = validateRoleAuthorizationService.execute(requestingUser, SystemRole.values());
        boolean canConsultAnyUser = requester.getRole() == SystemRole.ADMINISTRATOR
                || requester.getRole() == SystemRole.SUPERVISOR;
        if (!canConsultAnyUser && !requester.getId().equals(userId)) {
            throw new UnauthorizedOperationException("A user can only consult its own information (RG-03).");
        }
        return userRepositoryPort.findById(userId).orElseThrow(() -> new EntityNotFoundException("User", userId));
    }
}
