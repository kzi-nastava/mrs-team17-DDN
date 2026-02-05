package org.example.backend.repository;

import org.example.backend.dto.response.ChatMessageResponseDto;
import org.example.backend.dto.response.ChatThreadResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcChatRepository implements ChatRepository {

    private final JdbcClient jdbc;

    public JdbcChatRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<ChatThreadResponseDto> findThreadByUserId(long userId) {
        return jdbc.sql("""
            select id, user_id, last_message_at
            from chat_threads
            where user_id = :userId
        """)
                .param("userId", userId)
                .query((rs, rn) -> new ChatThreadResponseDto(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getObject("last_message_at", OffsetDateTime.class)
                ))
                .optional();
    }

    @Override
    public ChatThreadResponseDto createThreadForUser(long userId) {
        Long id = jdbc.sql("""
            insert into chat_threads (user_id, created_at, last_message_at)
            values (:userId, now(), null)
            returning id
        """)
                .param("userId", userId)
                .query(Long.class)
                .single();

        return new ChatThreadResponseDto(id, userId, null);
    }

    @Override
    public Optional<ChatThreadResponseDto> findThreadById(long threadId) {
        return jdbc.sql("""
            select id, user_id, last_message_at
            from chat_threads
            where id = :id
        """)
                .param("id", threadId)
                .query((rs, rn) -> new ChatThreadResponseDto(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getObject("last_message_at", OffsetDateTime.class)
                ))
                .optional();
    }

    @Override
    public List<ChatThreadResponseDto> listThreads(String query, int limit) {
        String q = (query == null) ? "" : query.trim().toLowerCase();

        return jdbc.sql("""
        select
          ct.id,
          ct.user_id,
          ct.last_message_at,
          u.first_name,
          u.last_name,
          u.email
        from chat_threads ct
        join users u on u.id = ct.user_id
        where (:q = '' or
               lower(u.email) like ('%' || :q || '%') or
               lower(u.first_name) like ('%' || :q || '%') or
               lower(u.last_name) like ('%' || :q || '%') or
               lower(u.first_name || ' ' || u.last_name) like ('%' || :q || '%'))
        order by ct.last_message_at desc nulls last, ct.id desc
        limit :limit
    """)
                .param("q", q)
                .param("limit", limit)
                .query((rs, rn) -> {
                    String first = rs.getString("first_name");
                    String last = rs.getString("last_name");
                    String name = ((first == null) ? "" : first) + " " + ((last == null) ? "" : last);
                    name = name.trim();
                    if (name.isEmpty()) name = rs.getString("email"); // fallback

                    return new ChatThreadResponseDto(
                            rs.getLong("id"),
                            rs.getLong("user_id"),
                            name,
                            rs.getString("email"),
                            rs.getObject("last_message_at", OffsetDateTime.class)
                    );
                })
                .list();
    }


    @Override
    public List<ChatMessageResponseDto> findMessages(long threadId, Long afterId, int limit) {
        if (afterId == null) {
            List<ChatMessageResponseDto> desc = jdbc.sql("""
                select id, sender_role, content, sent_at
                from chat_messages
                where thread_id = :threadId
                order by id desc
                limit :limit
            """)
                    .param("threadId", threadId)
                    .param("limit", limit)
                    .query((rs, rn) -> new ChatMessageResponseDto(
                            rs.getLong("id"),
                            rs.getString("sender_role"),
                            rs.getString("content"),
                            rs.getObject("sent_at", OffsetDateTime.class)
                    ))
                    .list();

            // vrati u asc da frontu bude prirodno
            List<ChatMessageResponseDto> asc = new ArrayList<>(desc.size());
            for (int i = desc.size() - 1; i >= 0; i--) asc.add(desc.get(i));
            return asc;
        }

        return jdbc.sql("""
            select id, sender_role, content, sent_at
            from chat_messages
            where thread_id = :threadId
              and id > :afterId
            order by id asc
            limit :limit
        """)
                .param("threadId", threadId)
                .param("afterId", afterId)
                .param("limit", limit)
                .query((rs, rn) -> new ChatMessageResponseDto(
                        rs.getLong("id"),
                        rs.getString("sender_role"),
                        rs.getString("content"),
                        rs.getObject("sent_at", OffsetDateTime.class)
                ))
                .list();
    }

    @Override
    public ChatMessageResponseDto insertMessage(long threadId, long senderUserId, String senderRole, String content) {
        OffsetDateTime now = OffsetDateTime.now();

        Long id = jdbc.sql("""
            insert into chat_messages (thread_id, sender_user_id, sender_role, content, sent_at)
            values (:threadId, :senderUserId, :senderRole, :content, :sentAt)
            returning id
        """)
                .param("threadId", threadId)
                .param("senderUserId", senderUserId)
                .param("senderRole", senderRole)
                .param("content", content)
                .param("sentAt", now)
                .query(Long.class)
                .single();

        return new ChatMessageResponseDto(id, senderRole, content, now);
    }

    @Override
    public boolean touchThreadLastMessageAt(long threadId) {
        int updated = jdbc.sql("""
            update chat_threads
            set last_message_at = now()
            where id = :id
        """)
                .param("id", threadId)
                .update();

        return updated > 0;
    }
}
