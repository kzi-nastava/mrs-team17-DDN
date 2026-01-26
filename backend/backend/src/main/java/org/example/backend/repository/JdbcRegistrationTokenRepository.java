package org.example.backend.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcRegistrationTokenRepository implements RegistrationTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcRegistrationTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void createToken(Long userId, String token, Instant expiresAt) {
        jdbcTemplate.update(
                """
                INSERT INTO registration_tokens (user_id, token, expires_at)
                VALUES (?, ?, ?)
                """,
                userId, token, Timestamp.from(expiresAt)
        );
    }

    @Override
    public Optional<TokenRow> findValidToken(String token, Instant now) {
        List<TokenRow> rows = jdbcTemplate.query(
                """
                SELECT user_id
                FROM registration_tokens
                WHERE token = ?
                  AND used_at IS NULL
                  AND expires_at > ?
                """,
                (rs, rowNum) -> new TokenRow(rs.getLong("user_id")),
                token, Timestamp.from(now)
        );
        return rows.stream().findFirst();
    }

    @Override
    public void markUsed(String token, Instant usedAt) {
        jdbcTemplate.update(
                """
                UPDATE registration_tokens
                SET used_at = ?
                WHERE token = ?
                """,
                Timestamp.from(usedAt), token
        );
    }
}