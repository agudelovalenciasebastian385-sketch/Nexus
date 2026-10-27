package nexusmarket.domain.models;

import nexusmarket.domain.exceptions.InvalidUserException;

/**
 * AuthenticationResult.
 *
 * <p>Outcome of a successful login: the authenticated {@link User} together
 * with the freshly issued token. The token references the user and its
 * {@code authTokenVersion}, so it stops being valid as soon as that version
 * advances (logout, password change or status change).</p>
 */
public class AuthenticationResult {

    private final User authenticatedUser;
    private final String token;

    public AuthenticationResult(User authenticatedUser, String token) {
        if (authenticatedUser == null) {
            throw new InvalidUserException("The authenticated user is required.");
        }
        if (token == null || token.isBlank()) {
            throw new InvalidUserException("The authentication token cannot be empty.");
        }
        this.authenticatedUser = authenticatedUser;
        this.token = token;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "AuthenticationResult{authenticatedUser=" + authenticatedUser.getId() + "}";
    }
}
