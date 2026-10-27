package nexusmarket.domain.services.user;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.EntityNotFoundException;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
import org.springframework.stereotype.Service;

/**
 * Changes the operational status of a user (user-services.md -
 * ChangeUserStatus). Restricted to administrators.
 */
@Service
@RequiredArgsConstructor
public class ChangeUserStatusService {

    private final UserRepositoryPort userRepositoryPort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public User changeUserStatus(User requestingUser, String userId, UserStatus newStatus) {
        User administrator = validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR);
        User user = userRepositoryPort.findById(userId).orElseThrow(() -> new EntityNotFoundException("User", userId));

        UserStatus previousStatus = user.getStatus();
        user.changeStatus(newStatus);
        userRepositoryPort.update(user);
        registerAuditLogService.execute(administrator, AuditAction.USER_STATUS_CHANGED, AuditEntityType.USER,
                user.getId(), Map.of("from", previousStatus.getCode(), "to", newStatus.getCode()));
        return user;
    }
}
