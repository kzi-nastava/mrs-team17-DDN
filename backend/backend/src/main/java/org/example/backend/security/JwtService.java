package org.example.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    public record JwtUser(long userId, String email, String role) {}

    private final SecretKey key;
    private final long ttlMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.ttl-minutes:120}") long ttlMinutes
    ) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMinutes = ttlMinutes;
    }

    public String generateToken(long userId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlMinutes * 60);

        return Jwts.builder()
                .subject(Long.toString(userId))
                .claims(Map.of("email", email, "role", role))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public JwtUser parseToken(String token) {
        Claims c = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long userId = Long.parseLong(c.getSubject());
        String email = c.get("email", String.class);
        String role = c.get("role", String.class);

        return new JwtUser(userId, email, role);
    }
}
