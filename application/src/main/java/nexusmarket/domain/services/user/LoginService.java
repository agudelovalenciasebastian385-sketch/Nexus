package nexusmarket.domain.services.user;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import nexusmarket.domain.exceptions.InvalidCredentialsException;
import nexusmarket.domain.exceptions.UnauthorizedOperationException;
import nexusmarket.domain.models.AuthenticationResult;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.JwtServicePort;
import nexusmarket.domain.ports.out.PasswordServicePort;
import nexusmarket.domain.ports.out.UserRepositoryPort;
import nexusmarket.domain.services.audit.RegisterAuditLogService;
import nexusmarket.domain.valueobjects.AuditAction;
import nexusmarket.domain.valueobjects.AuditEntityType;
import org.springframework.stereotype.Service;

/**
 * Authenticates a user with its email and password and returns the
 * authenticated user together with its token (user-services.md - Login). The plain password is only compared against the
 * stored hash through {@link PasswordServicePort}.
 */
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordServicePort passwordServicePort;
    private final JwtServicePort jwtServicePort;
    private final RegisterAuditLogService registerAuditLogService;

    public AuthenticationResult login(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            throw new InvalidCredentialsException();
        }
        User user = userRepositoryPort.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (!passwordServicePort.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        if (!user.isActive()) {
            throw new UnauthorizedOperationException(
                    "User " + user.getId() + " is " + user.getStatus().getCode() + " and cannot log in.");
        }
        String token = jwtServicePort.generateToken(user);
        registerAuditLogService.execute(user, AuditAction.USER_LOGGED_IN, AuditEntityType.USER, user.getId(),
                Map.of());
        return new AuthenticationResult(user, token);
    }
}
