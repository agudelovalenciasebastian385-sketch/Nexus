package nexusmarket.domain.models;

import java.util.regex.Pattern;

import nexusmarket.domain.exceptions.InvalidUserException;
import nexusmarket.domain.valueobjects.SystemRole;
import nexusmarket.domain.valueobjects.UserStatus;

/**
 * User (abstract).
 *
 * <p>Represents any person authorized to interact with NexusMarket. It
 * centralizes the identity and contact information shared by all system
 * participants. Each participant holds a single role within the system and may
 * only interact with the information corresponding to their responsibilities.</p>
 *
 * <p>This class cannot be instantiated directly: it is specialized as
 * {@link Buyer}, {@link Seller}, {@link LogisticsOperator},
 * {@link Administrator} and {@link Supervisor}, each one with a fixed
 * {@link SystemRole}.</p>
 */
public abstract class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String id;
    /** Identity document number. Unique across the platform (enforced at service level). */
    private final String identificationNumber;
    private String fullName;
    private final String email;
    /** Encrypted password (value produced by the password service, never the plain text). */
    private String password;
    private final SystemRole role;
    private UserStatus status;

    protected User(String id, String identificationNumber, String fullName, String email, String password,
                   SystemRole role, UserStatus status) {
        if (id == null || id.isBlank()) {
            throw new InvalidUserException("User id cannot be empty.");
        }
        if (identificationNumber == null || identificationNumber.isBlank()) {
            throw new InvalidUserException("User identificationNumber cannot be empty.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new InvalidUserException("User fullName cannot be empty.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidUserException("User email is not valid: " + email);
        }
        this.id = id;
        this.identificationNumber = identificationNumber;
        this.fullName = fullName;
        this.email = email;
        this.password = requirePassword(password);
        if (role == null) {
            throw new InvalidUserException("User role is required.");
        }
        this.role = role;
        if (status == null) {
            throw new InvalidUserException("User status is required.");
        }
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public SystemRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    /**
     * Changes the operational condition of the user
     * (e.g. {@code ACTIVE}, {@code BLOCKED}, {@code INACTIVE}).
     */
    public void changeStatus(UserStatus newStatus) {
        if (newStatus == null) {
            throw new InvalidUserException("User status is required.");
        }
        this.status = newStatus;
    }

    /** Replaces the encrypted password. */
    public void changePassword(String encodedPassword) {
        this.password = requirePassword(encodedPassword);
    }

    /** Replaces the full name of the user. */
    public void updateFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new InvalidUserException("User fullName cannot be empty.");
        }
        this.fullName = fullName;
    }

    private static String requirePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new InvalidUserException("User password cannot be empty.");
        }
        return password;
    }

    /** Indicates whether the user can operate normally within the system. */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof User)) {
            return false;
        }
        User otherUser = (User) other;
        return id.equals(otherUser.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "User{id='" + id + "', identificationNumber='" + identificationNumber + "', fullName='" + fullName + "', email='" + email
                + "', role=" + role + ", status=" + status + "}";
    }
}
