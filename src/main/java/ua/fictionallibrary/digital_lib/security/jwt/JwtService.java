package ua.fictionallibrary.digital_lib.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMins;

    public JwtService (
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-in-minutes:15}") long expirationMins) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMins = expirationMins;
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMins * 60)))
                .signWith(key)
                .compact();
    }

    public long getExpirationInSeconds() {
        return expirationMins * 60;
    }
}
