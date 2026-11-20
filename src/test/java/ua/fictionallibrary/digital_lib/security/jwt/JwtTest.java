package ua.fictionallibrary.digital_lib.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtTest {

    private static final String SECRET = "test-secret-at-least-256-bits-long-for-hmac-sha-key";
    private static final String OTHER_SECRET = "another-secret-at-least-256-bits-long-for-hmac-key";

    private final JwtService service = new JwtService(SECRET, 15);
    private final JwtValidator validator = new JwtValidator(SECRET);

    private static UserDetails user(String username) {
        return User.withUsername(username).password("x").roles("READER").build();
    }

    @Test
    void tokenRoundTrip() {
        String token = service.generateToken(user("reader"));

        assertEquals("reader", validator.extractUsername(token));
        assertTrue(validator.isTokenValid(token, user("reader")));
    }

    @Test
    void tokenCarriesRoleClaim() {
        String token = service.generateToken(user("reader"));

        assertTrue(validator.extractAllClaims(token).get("role").toString().contains("ROLE_READER"));
    }

    @Test
    void expirationMatchesConfig() {
        assertEquals(900, service.getExpirationInSeconds());
    }

    @Test
    void tokenForAnotherUserInvalid() {
        String token = service.generateToken(user("reader"));

        assertFalse(validator.isTokenValid(token, user("someone-else")));
    }

    @Test
    void expiredTokenRejected() {
        String token = new JwtService(SECRET, -1).generateToken(user("reader"));

        assertThrows(ExpiredJwtException.class, () -> validator.extractUsername(token));
    }

    @Test
    void tokenSignedWithOtherKeyRejected() {
        String token = new JwtService(OTHER_SECRET, 15).generateToken(user("reader"));

        assertThrows(SignatureException.class, () -> validator.extractUsername(token));
    }

    @Test
    void tamperedPayloadRejected() {
        String[] parts = service.generateToken(user("reader")).split("\\.");
        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"admin\"}".getBytes());
        String forged = parts[0] + "." + forgedPayload + "." + parts[2];

        assertThrows(SignatureException.class, () -> validator.extractUsername(forged));
    }

    @Test
    void garbageTokenRejected() {
        assertThrows(MalformedJwtException.class, () -> validator.extractUsername("not.a.jwt"));
    }

    @Test
    void shortSecretRefusedAtStartup() {
        assertThrows(io.jsonwebtoken.security.WeakKeyException.class, () -> new JwtValidator("too-short"));
    }
}