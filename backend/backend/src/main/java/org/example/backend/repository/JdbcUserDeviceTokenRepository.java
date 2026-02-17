package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class JdbcUserDeviceTokenRepository implements UserDeviceTokenRepository {

    private final JdbcClient jdbc;

    public JdbcUserDeviceTokenRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void upsertToken(long userId, String token, String platform) {
        if (userId <= 0 || token == null || token.isBlank()) return;

        String normalizedToken = token.trim();
        String normalizedPlatform = (platform == null || platform.isBlank())
                ? "ANDROID"
                : platform.trim().toUpperCase();

        jdbc.sql("""
            insert into user_device_tokens(user_id, token, platform, updated_at, last_seen_at)
            values (:userId, :token, :platform, now(), now())
            on conflict (token)
            do update set
                user_id = excluded.user_id,
                platform = excluded.platform,
                updated_at = now(),
                last_seen_at = now()
        """)
                .param("userId", userId)
                .param("token", normalizedToken)
                .param("platform", normalizedPlatform)
                .update();
    }

    @Override
    public Map<Long, List<String>> findTokensByUserIds(List<Long> userIds) {
        Map<Long, List<String>> out = new LinkedHashMap<>();
        if (userIds == null || userIds.isEmpty()) return out;

        Set<Long> uniqueUserIds = new LinkedHashSet<>();
        for (Long userId : userIds) {
            if (userId != null && userId > 0) uniqueUserIds.add(userId);
        }
        if (uniqueUserIds.isEmpty()) return out;

        for (Long userId : uniqueUserIds) {
            List<String> tokens = jdbc.sql("""
                select token
                from user_device_tokens
                where user_id = :userId
            """)
                    .param("userId", userId)
                    .query(String.class)
                    .list();

            if (tokens == null || tokens.isEmpty()) continue;

            List<String> cleaned = new ArrayList<>();
            for (String token : tokens) {
                if (token == null || token.isBlank()) continue;
                cleaned.add(token.trim());
            }
            if (!cleaned.isEmpty()) {
                out.put(userId, cleaned);
            }
        }

        return out;
    }

    @Override
    public void deleteToken(String token) {
        if (token == null || token.isBlank()) return;

        jdbc.sql("""
            delete from user_device_tokens
            where token = :token
        """)
                .param("token", token.trim())
                .update();
    }
}
