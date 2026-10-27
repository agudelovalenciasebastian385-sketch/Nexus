package nexusmarket.domain.services.user;

import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.exceptions.UserAlreadyExistsException;
import nexusmarket.domain.models.LogisticsOperator;
import nexusmarket.domain.models.Supervisor;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.PasswordServicePort;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.services.authorization.ValidateRoleAuthorizationService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;
import org.springframework.stereotype.Service;

/**
 * Registers a staff user: a logistics operator or a supervisor
 * (user-services.md - RegisterStaffUser). Restricted to administrators.
 */
@Service
@RequiredArgsConstructor
public class RegisterStaffUserService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final ValidateRoleAuthorizationService validateRoleAuthorizationService;
    private final RegisterAuditLogService registerAuditLogService;

    public User registerStaffUser(User requestingUser, SystemRole role, String identificationNumber,
                                  String fullName, String email, String rawPassword) {
        User administrator = validateRoleAuthorizationService.execute(requestingUser, SystemRole.ADMINISTRATOR);
        if (role != SystemRole.LOGISTICS_OPERATOR && role != SystemRole.SUPERVISOR) {
            throw new InvalidUserException("A staff user must have the role "
                    + SystemRole.LOGISTICS_OPERATOR.getCode() + " or " + SystemRole.SUPERVISOR.getCode() + ".");
        }
        validateUniqueness(identificationNumber, email);
        String encodedPassword = encrypt(rawPassword);

        String id = UUID.randomUUID().toString();
        User staffUser = role == SystemRole.LOGISTICS_OPERATOR
                ? new LogisticsOperator(id, identificationNumber, fullName, email, encodedPassword, UserStatus.ACTIVE)
                : new Supervisor(id, identificationNumber, fullName, email, encodedPassword, UserStatus.ACTIVE);

        User saved = userRepositoryPort.save(staffUser);
        registerAuditLogService.execute(administrator, AuditAction.STAFF_USER_REGISTERED, AuditEntityType.USER,
                saved.getId(), Map.of("role", role.getCode(), "email", saved.getEmail()));
        return saved;
    }

    private void validateUniqueness(String identificationNumber, String email) {
        if (identificationNumber != null && userRepositoryPort.existsByIdentificationNumber(identificationNumber)) {
            throw new UserAlreadyExistsException(
                    "A user with identification number " + identificationNumber + " is already registered.");
        }
        if (email != null && userRepositoryPort.existsByEmail(email)) {
            throw new UserAlreadyExistsException("A user with email " + email + " is already registered.");
        }
    }

    private String encrypt(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidUserException("Password cannot be empty.");
        }
        return passwordServicePort.encrypt(rawPassword);
    }
}
