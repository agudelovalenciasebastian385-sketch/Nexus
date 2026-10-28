package nexusmarket.adapters.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import nexusmarket.adapters.security.dtos.AuthenticatedUserPrincipal;
import nexusmarket.domain.exceptions.DomainException;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.JwtServicePort;
import nexusmarket.domain.services.user.LoadAuthenticatedUserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT authentication filter (flow in SDD/Adapters/Security-adapters.md, section 5).
 *
 * <p>For a request with {@code Authorization: Bearer <token>}: validates the token, extracts the
 * user id, loads the stored user through {@link LoadAuthenticatedUserService}, and checks that the
 * user is ACTIVE and that the token version equals its authTokenVersion. Only then the request is
 * authenticated with an {@link AuthenticatedUserPrincipal}. On any failure the security context is
 * left empty, so a protected route ends in 401 through the entry point of {@code SecurityConfig}.
 *
 * <p>Created by {@code SecurityConfig} (not a {@code @Component}) so it runs only inside the
 * Spring Security filter chain.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtServicePort jwtServicePort;
    private final LoadAuthenticatedUserService loadAuthenticatedUserService;

    public JwtAuthenticationFilter(JwtServicePort jwtServicePort,
                                   LoadAuthenticatedUserService loadAuthenticatedUserService) {
        this.jwtServicePort = jwtServicePort;
        this.loadAuthenticatedUserService = loadAuthenticatedUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = extractBearerToken(request);
        if (token != null) {
            User user = authenticate(token);
            if (user != null) {
                AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(user);
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
            } else {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    /** Returns the stored user when every check passes, {@code null} otherwise. */
    private User authenticate(String token) {
        if (!jwtServicePort.validateToken(token)) {
            return null;
        }
        String userId = jwtServicePort.extractUserId(token);
        Integer tokenVersion = jwtServicePort.extractTokenVersion(token);
        if (userId == null || tokenVersion == null) {
            return null;
        }
        User user;
        try {
            user = loadAuthenticatedUserService.loadById(userId);
        } catch (DomainException e) {
            return null;
        }
        if (!user.isActive() || !tokenVersion.equals(user.getAuthTokenVersion())) {
            return null;
        }
        return user;
    }

    private static String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
