package nexusmarket.domain.models;

import java.util.Objects;
import java.util.regex.Pattern;

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
 * {@link Buyer} and {@link Seller} in the current delivery. The remaining
 * roles of the {@link SystemRole} catalog (Logistics Operator, Administrator,
 * Supervisor) have no dedicated subclass yet.</p>
 */
public abstract class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String id;
    private String fullName;
    private final String email;
    private final SystemRole role;
    private UserStatus status;

    protected User(String id, String fullName, String email, SystemRole role, UserStatus status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("User id cannot be empty.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("User fullName cannot be empty.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("User email is not valid: " + email);
        }
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = Objects.requireNonNull(role, "User role is required.");
        this.status = Objects.requireNonNull(status, "User status is required.");
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
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
        this.status = Objects.requireNonNull(newStatus, "User status is required.");
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
        return "User{id='" + id + "', fullName='" + fullName + "', email='" + email
                + "', role=" + role + ", status=" + status + "}";
    }
}
