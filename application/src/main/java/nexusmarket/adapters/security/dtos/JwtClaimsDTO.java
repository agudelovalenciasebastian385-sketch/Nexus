package nexusmarket.adapters.security.dtos;

import java.time.Instant;
import lombok.Data;

/**
 * DTO with the claims carried by a NexusMarket JWT.
 *
 * <p>The token is minimal: it identifies the user and the token version only. It never carries
 * user data; the JWT filter loads the current User from the database on every request. The
 * domain never sees claims: only the security adapter converts User &lt;-&gt; this DTO &lt;-&gt;
 * claims.
 */
@Data
public class JwtClaimsDTO {

    /** {@code sub}: User.id. */
    private String userId;

    /** {@code jti}: unique token id. */
    private String tokenId;

    /** {@code ver}: User.authTokenVersion when the token was issued. */
    private Integer version;

    /** {@code iat}: issue instant. */
    private Instant issuedAt;

    /** {@code exp}: expiration instant. */
    private Instant expiresAt;
}
