package nexusmarket.adapters.security.dtos;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nexusmarket.domain.models.User;
import nexusmarket.domain.valueobjects.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security principal wrapping the domain {@link User} loaded from the database by
 * {@code JwtAuthenticationFilter} on every request.
 *
 * <p>Spring Security types stay outside {@code domain/}: this adapter DTO is the only place where
 * the domain user meets Spring's authentication model. Controllers receive the domain user with
 * {@code @AuthenticationPrincipal(expression = "user") User}. The authority is
 * {@code ROLE_<SystemRole code>}; the password hash is never exposed.
 */
@Getter
@RequiredArgsConstructor
public class AuthenticatedUserPrincipal implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getCode()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.BLOCKED;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
