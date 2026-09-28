package nexusmarket.domain.services.user;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.InvalidCredentialsException;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.in.ChangeUserPasswordUseCase;
import nexusmarket.domain.ports.out.PasswordServicePort;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import org.springframework.stereotype.Service;

/**
 * Changes the password of the requesting user (user-services.md -
 * ChangeUserPassword). The new password is stored only as the hash produced by
 * {@link PasswordServicePort}.
 */
@Service
@RequiredArgsConstructor
public class ChangeUserPasswordService implements ChangeUserPasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    @Override
    public void changeUserPassword(User requestingUser, String currentPassword, String newPassword) {
        User user = validateRoleAuthorizationService.execute(requestingUser, SystemRole.values());
        if (currentPassword == null || !passwordServicePort.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidUserException("The new password cannot be empty.");
        }
        user.changePassword(passwordServicePort.encrypt(newPassword));
        userRepositoryPort.update(user);
        registerAuditLogService.execute(user, AuditAction.USER_PASSWORD_CHANGED, AuditEntityType.USER, user.getId(),
                Map.of());
    }
}
