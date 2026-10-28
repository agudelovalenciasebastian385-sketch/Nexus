package nexusmarket.adapters.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import javax.crypto.SecretKey;
import nexusmarket.adapters.security.dtos.JwtClaimsDTO;
import nexusmarket.adapters.security.mappers.JwtClaimsMapper;
import nexusmarket.domain.models.User;
import nexusmarket.domain.ports.out.JwtServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Output adapter for issuing and validating JWTs (JJWT 0.12.6, HS256).
 *
 * <p>The token carries {@code sub} (user id), {@code jti}, {@code ver} (authTokenVersion),
 * {@code iat} and {@code exp}. Key and lifetime come from {@code app.security.jwt.secret} and
 * {@code app.security.jwt.expiration-minutes}. A secret shorter than 256 bits fails at startup.
 *
 * <p>The JWT filter (REST API step) validates the token, loads the user by
 * {@link #extractUserId} and compares {@link #extractTokenVersion} with its authTokenVersion.
 */
@Component
public class JwtProvider implements JwtServicePort {

    private final SecretKey signingKey;
    private final Duration expiration;

    public JwtProvider(@Value("${app.security.jwt.secret}") String secret,
                       @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    @Override
    public String generateToken(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("A user with an id is required to issue a token.");
        }
        Instant now = Instant.now();
        JwtClaimsDTO claims = JwtClaimsMapper.toDto(user, now, now.plus(expiration));
        return JwtClaimsMapper.applyClaims(Jwts.builder(), claims)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        return parse(token) != null;
    }

    @Override
    public String extractUserId(String token) {
        JwtClaimsDTO claims = parse(token);
        return claims != null ? claims.getUserId() : null;
    }

    @Override
    public Integer extractTokenVersion(String token) {
        JwtClaimsDTO claims = parse(token);
        return claims != null ? claims.getVersion() : null;
    }

    /** Verifies signature and expiration; returns null for any invalid token. */
    private JwtClaimsDTO parse(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return JwtClaimsMapper.toDto(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
