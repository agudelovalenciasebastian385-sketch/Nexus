package nexusmarket.adapters.security.mappers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import nexusmarket.adapters.security.dtos.JwtClaimsDTO;
import nexusmarket.domain.models.User;

/**
 * Mapping between the User Domain Model, the claims DTO and the JWT claims.
 *
 * <p>Claims: {@code sub} (user id), {@code jti} (token id), {@code ver} (authTokenVersion),
 * {@code iat} and {@code exp}.
 */
public final class JwtClaimsMapper {

    public static final String VERSION_CLAIM = "ver";

    private JwtClaimsMapper() {
    }

    public static JwtClaimsDTO toDto(User user, Instant issuedAt, Instant expiresAt) {
        if (user == null) {
            return null;
        }
        JwtClaimsDTO dto = new JwtClaimsDTO();
        dto.setUserId(user.getId());
        dto.setTokenId(UUID.randomUUID().toString());
        dto.setVersion(user.getAuthTokenVersion());
        dto.setIssuedAt(issuedAt);
        dto.setExpiresAt(expiresAt);
        return dto;
    }

    public static JwtBuilder applyClaims(JwtBuilder builder, JwtClaimsDTO dto) {
        return builder
                .subject(dto.getUserId())
                .id(dto.getTokenId())
                .claim(VERSION_CLAIM, dto.getVersion())
                .issuedAt(Date.from(dto.getIssuedAt()))
                .expiration(Date.from(dto.getExpiresAt()));
    }

    public static JwtClaimsDTO toDto(Claims claims) {
        if (claims == null) {
            return null;
        }
        JwtClaimsDTO dto = new JwtClaimsDTO();
        dto.setUserId(claims.getSubject());
        dto.setTokenId(claims.getId());
        dto.setVersion(version(claims.get(VERSION_CLAIM)));
        dto.setIssuedAt(claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : null);
        dto.setExpiresAt(claims.getExpiration() != null ? claims.getExpiration().toInstant() : null);
        return dto;
    }

    private static Integer version(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
