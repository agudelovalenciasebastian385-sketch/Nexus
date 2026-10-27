package nexusmarket.domain.services.user;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import org.springframework.stereotype.Service;

/**
 * Logs out the authenticated user by invalidating every token issued to it
 * (user-services.md - Logout). The stored user's {@code authTokenVersion} is
 * advanced, so tokens carrying the previous version are rejected from then on.
 */
@Service
@RequiredArgsConstructor
public class LogoutService {

    private final UserRepositoryPort userRepositoryPort;
    private final RegisterAuditLogService registerAuditLogService;

    public void logout(User user) {
        if (user == null) {
            throw new UnauthorizedOperationException("An authenticated user is required to log out.");
        }
        User stored = userRepositoryPort.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User", user.getId()));
        stored.bumpAuthTokenVersion();
        userRepositoryPort.update(stored);
        registerAuditLogService.execute(stored, AuditAction.USER_LOGGED_OUT, AuditEntityType.USER, stored.getId(),
                Map.of());
    }
}
