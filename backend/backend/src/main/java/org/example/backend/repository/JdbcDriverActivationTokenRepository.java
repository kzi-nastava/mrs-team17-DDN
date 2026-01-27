package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class JdbcDriverActivationTokenRepository implements DriverActivationTokenRepository {

    private final JdbcClient jdbc;

    public JdbcDriverActivationTokenRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long createToken(Long userId, String token, OffsetDateTime expiresAt) {
        String sql = """
            insert into driver_activation_tokens (user_id, token, expires_at)
            values (:userId, :token, :expiresAt)
            returning id
        """;

        return jdbc.sql(sql)
                .param("userId", userId)
                .param("token", token)
                .param("expiresAt", expiresAt)
                .query(Long.class)
                .single();
    }

    @Override
    public Optional<TokenRow> findValidByToken(String token) {
        String sql = """
            select id, user_id, expires_at, used_at
            from driver_activation_tokens
            where token = :token
        """;

        return jdbc.sql(sql)
                .param("token", token)
                .query(rs -> {
                    if (!rs.next()) return Optional.empty();

                    Long id = rs.getLong("id");
                    Long userId = rs.getLong("user_id");
                    OffsetDateTime expiresAt = rs.getObject("expires_at", OffsetDateTime.class);
                    OffsetDateTime usedAt = rs.getObject("used_at", OffsetDateTime.class);

                    OffsetDateTime now = OffsetDateTime.now();
                    boolean valid = (usedAt == null) && expiresAt != null && expiresAt.isAfter(now);

                    if (!valid) return Optional.empty();
                    return Optional.of(new TokenRow(id, userId));
                });
    }

    @Override
    public int markUsed(Long tokenId) {
        String sql = """
            update driver_activation_tokens
            set used_at = now()
            where id = :id and used_at is null
        """;

        return jdbc.sql(sql)
                .param("id", tokenId)
                .update();
    }
}
