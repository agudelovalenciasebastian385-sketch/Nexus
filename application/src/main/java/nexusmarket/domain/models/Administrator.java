package nexusmarket.domain.models;

import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;

/**
 * Administrator.
 *
 * <p>Responsible for the administration of sellers and warehouses within the
 * Marketplace: onboards {@link Seller} instances (sellers cannot
 * self-register) and registers Marketplace-type {@link Warehouse}
 * instances.</p>
 *
 * <p>No additional attributes beyond those inherited from {@link User}; its
 * role is always {@link SystemRole#ADMINISTRATOR}.</p>
 */
public class Administrator extends User {

    /** Creates a new administrator starting at token version {@code 1}. */
    public Administrator(String id, String identificationNumber, String fullName, String email, String password,
                         UserStatus status) {
        this(id, identificationNumber, fullName, email, password, status, 1);
    }

    /** Rehydration constructor used to rebuild a persisted administrator with its stored token version. */
    public Administrator(String id, String identificationNumber, String fullName, String email, String password,
                         UserStatus status, Integer authTokenVersion) {
        super(id, identificationNumber, fullName, email, password, SystemRole.ADMINISTRATOR, status,
                authTokenVersion);
    }

    @Override
    public String toString() {
        return "Administrator{id='" + getId() + "', fullName='" + getFullName() + "', email='" + getEmail()
                + "', status=" + getStatus() + "}";
    }
}
