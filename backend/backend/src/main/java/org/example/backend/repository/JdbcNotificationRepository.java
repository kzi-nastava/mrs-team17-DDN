package org.example.backend.repository;

import org.example.backend.dto.response.NotificationResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcNotificationRepository implements NotificationRepository {

    private final JdbcClient jdbc;

    public JdbcNotificationRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<NotificationResponseDto> listForUser(long userId, int limit) {
        int lim = Math.max(1, Math.min(limit, 200));
        return jdbc.sql("""
            select id, type, title, message, link_url, created_at, read_at
            from notifications
            where user_id = :userId
            order by created_at desc, id desc
            limit :lim
        """)
                .param("userId", userId)
                .param("lim", lim)
                .query((rs, rn) -> new NotificationResponseDto(
                        rs.getLong("id"),
                        rs.getString("type"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getString("link_url"),
                        rs.getObject("created_at", java.time.OffsetDateTime.class),
                        rs.getObject("read_at", java.time.OffsetDateTime.class)
                ))
                .list();
    }

    @Override
    public long countUnread(long userId) {
        Long c = jdbc.sql("""
            select count(1)
            from notifications
            where user_id = :userId
              and read_at is null
        """)
                .param("userId", userId)
                .query(Long.class)
                .single();
        return c == null ? 0 : c;
    }

    @Override
    public boolean markRead(long userId, long notificationId) {
        int updated = jdbc.sql("""
            update notifications
            set read_at = now()
            where id = :id
              and user_id = :userId
              and read_at is null
        """)
                .param("id", notificationId)
                .param("userId", userId)
                .update();
        return updated > 0;
    }

    @Override
    public void createForUsers(List<Long> userIds, String type, String title, String message, String linkUrl) {
        if (userIds == null || userIds.isEmpty()) return;

        for (Long uid : userIds) {
            jdbc.sql("""
                insert into notifications(user_id, type, title, message, link_url)
                values (:userId, :type, :title, :message, :linkUrl)
            """)
                    .param("userId", uid)
                    .param("type", type)
                    .param("title", title)
                    .param("message", message)
                    .param("linkUrl", linkUrl)
                    .update();
        }
    }
}
