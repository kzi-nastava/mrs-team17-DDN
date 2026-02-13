package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class JdbcPasswordResetTokenRepository implements PasswordResetTokenRepository {

    private final JdbcClient jdbc;

    public JdbcPasswordResetTokenRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void createToken(Long userId, String token, Instant expiresAt) {
        String sql = """
        insert into password_reset_tokens (user_id, token, expires_at)
        values (:userId, :token, :expiresAt)
    """;

        jdbc.sql(sql)
                .param("userId", userId)
                .param("token", token)
                .param("expiresAt", OffsetDateTime.ofInstant(expiresAt, java.time.ZoneOffset.UTC))
                .update();
    }


    @Override
    public Optional<TokenRow> findValidToken(String token, Instant now) {
        String sql = """
        select id, user_id, token, expires_at, used_at
        from password_reset_tokens
        where token = :token
          and used_at is null
          and expires_at > :now
        limit 1
    """;

        return jdbc.sql(sql)
                .param("token", token)
                .param("now", OffsetDateTime.ofInstant(now, java.time.ZoneOffset.UTC))
                .query((rs, rowNum) -> new TokenRow(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("token"),
                        rs.getObject("expires_at", OffsetDateTime.class).toInstant(),
                        rs.getObject("used_at", OffsetDateTime.class) == null
                                ? null
                                : rs.getObject("used_at", OffsetDateTime.class).toInstant()
                ))
                .optional();
    }


    @Override
    public int markUsed(String token, Instant usedAt) {
        String sql = """
        update password_reset_tokens
        set used_at = :usedAt
        where token = :token and used_at is null
    """;

        return jdbc.sql(sql)
                .param("token", token)
                .param("usedAt", OffsetDateTime.ofInstant(usedAt, java.time.ZoneOffset.UTC))
                .update();
    }

}
