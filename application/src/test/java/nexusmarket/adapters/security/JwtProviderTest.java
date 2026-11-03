package nexusmarket.adapters.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import nexusmarket.domain.DomainFixtures;
import nexusmarket.domain.models.Administrator;
import nexusmarket.domain.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JwtProviderTest {

    private static final String SECRET = "NexusMarket-Test-Secret-Key-With-More-Than-256-Bits";
    private static final String OTHER_SECRET = "Another-NexusMarket-Test-Secret-Key-With-Enough-Bits";
    private static final long EXPIRATION_MINUTES = 120;

    private static final JwtProvider PROVIDER = new JwtProvider(SECRET, EXPIRATION_MINUTES);

    private final Administrator user = new Administrator("user-1", "1000", "Admin", "admin@nexusmarket.com",
            DomainFixtures.PASSWORD_HASH, UserStatus.ACTIVE, 7);

    private Claims claimsOf(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ------------------------------------------------------------------ valid token

    @Test
    void generatedTokenIsValid() {
        assertThat(PROVIDER.validateToken(PROVIDER.generateToken(user))).isTrue();
    }

    @Test
    void subjectClaimIsTheUserId() {
        assertThat(claimsOf(PROVIDER.generateToken(user)).getSubject()).isEqualTo("user-1");
    }

    @Test
    void verClaimIsTheAuthTokenVersion() {
        assertThat(claimsOf(PROVIDER.generateToken(user)).get("ver", Integer.class)).isEqualTo(7);
    }

    @Test
    void expirationIsTheConfiguredTimeAfterIssuedAt() {
        Claims claims = claimsOf(PROVIDER.generateToken(user));

        long seconds = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;

        assertThat(seconds).isEqualTo(EXPIRATION_MINUTES * 60);
    }

    @Test
    void jtiIsPresent() {
        assertThat(claimsOf(PROVIDER.generateToken(user)).getId()).isNotBlank();
    }

    @Test
    void jtiIsDifferentInEveryToken() {
        assertThat(claimsOf(PROVIDER.generateToken(user)).getId())
                .isNotEqualTo(claimsOf(PROVIDER.generateToken(user)).getId());
    }

    @Test
    void tokenCarriesNoOtherUserData() {
        assertThat(claimsOf(PROVIDER.generateToken(user)).keySet())
                .containsExactlyInAnyOrder("sub", "jti", "ver", "iat", "exp");
    }

    @Test
    void extractUserIdReturnsTheSubject() {
        assertThat(PROVIDER.extractUserId(PROVIDER.generateToken(user))).isEqualTo("user-1");
    }

    @Test
    void extractTokenVersionReturnsTheVersion() {
        assertThat(PROVIDER.extractTokenVersion(PROVIDER.generateToken(user))).isEqualTo(7);
    }

    @Test
    void tokenCannotBeGeneratedWithoutAUser() {
        assertThatThrownBy(() -> PROVIDER.generateToken(null)).isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------ invalid tokens

    private static Administrator tokenUser() {
        return new Administrator("user-1", "1000", "Admin", "admin@nexusmarket.com", DomainFixtures.PASSWORD_HASH,
                UserStatus.ACTIVE, 1);
    }

    /** Changes the first character of the signature, which always changes its decoded bytes. */
    private static String withAlteredSignature(String token) {
        int signature = token.lastIndexOf('.') + 1;
        char replacement = token.charAt(signature) == 'A' ? 'B' : 'A';
        return token.substring(0, signature) + replacement + token.substring(signature + 1);
    }

    /** Keeps the signature of a token but replaces its payload with the one of another user's token. */
    private static String withForeignPayload(String token) {
        String foreign = PROVIDER.generateToken(new Administrator("user-2", "2000", "Other", "other@nexusmarket.com",
                DomainFixtures.PASSWORD_HASH, UserStatus.ACTIVE, 1));
        String[] parts = token.split("\\.");
        return parts[0] + "." + foreign.split("\\.")[1] + "." + parts[2];
    }

    static Stream<Arguments> invalidTokens() {
        String token = PROVIDER.generateToken(tokenUser());
        return Stream.of(
                Arguments.of("altered signature", withAlteredSignature(token)),
                Arguments.of("altered payload", withForeignPayload(token)),
                Arguments.of("signed with another key", new JwtProvider(OTHER_SECRET, 120).generateToken(tokenUser())),
                Arguments.of("expired", new JwtProvider(SECRET, -1).generateToken(tokenUser())),
                Arguments.of("empty", ""),
                Arguments.of("blank", "   "),
                Arguments.of("null", null),
                Arguments.of("not a JWT", "not-a-jwt"),
                Arguments.of("three malformed parts", "a.b.c"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTokens")
    void invalidTokenDoesNotValidate(String description, String token) {
        assertThat(PROVIDER.validateToken(token)).isFalse();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTokens")
    void userIdOfAnInvalidTokenIsNull(String description, String token) {
        assertThat(PROVIDER.extractUserId(token)).isNull();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidTokens")
    void tokenVersionOfAnInvalidTokenIsNull(String description, String token) {
        assertThat(PROVIDER.extractTokenVersion(token)).isNull();
    }

    // ------------------------------------------------------------------ key

    @Test
    void keyShorterThan256BitsFailsAtConstruction() {
        assertThatThrownBy(() -> new JwtProvider("too-short-key", EXPIRATION_MINUTES))
                .isInstanceOf(WeakKeyException.class);
    }
}
