package nexusmarket.domain.models;

import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;

/**
 * Supervisor.
 *
 * <p>Read-only profile for oversight and operational monitoring. Reviews
 * consolidated information across all business domains, without the ability
 * to modify system information.</p>
 *
 * <p>No additional attributes beyond those inherited from {@link User}; its
 * role is always {@link SystemRole#SUPERVISOR}.</p>
 */
public class Supervisor extends User {

    /** Creates a new supervisor starting at token version {@code 1}. */
    public Supervisor(String id, String identificationNumber, String fullName, String email, String password,
                      UserStatus status) {
        this(id, identificationNumber, fullName, email, password, status, 1);
    }

    /** Rehydration constructor used to rebuild a persisted supervisor with its stored token version. */
    public Supervisor(String id, String identificationNumber, String fullName, String email, String password,
                      UserStatus status, Integer authTokenVersion) {
        super(id, identificationNumber, fullName, email, password, SystemRole.SUPERVISOR, status,
                authTokenVersion);
    }

    @Override
    public String toString() {
        return "Supervisor{id='" + getId() + "', fullName='" + getFullName() + "', email='" + getEmail()
                + "', status=" + getStatus() + "}";
    }
}
