package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class JdbcDriverRepository implements DriverRepository {

    private final JdbcClient jdbc;

    public JdbcDriverRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long insertDriverReturningId(Long userId) {
        jdbc.sql("""
                insert into drivers (user_id, available, created_at)
                values (:userId, :available, :createdAt)
                """)
                .param("userId", userId)
                .param("available", false)
                .param("createdAt", OffsetDateTime.now())
                .update();

        return jdbc.sql("""
                select id
                from drivers
                where user_id = :userId
                """)
                .param("userId", userId)
                .query(Long.class)
                .single();
    }
    @Override
    public Optional<Long> findDriverIdByUserId(long userId) {
        return jdbc.sql("""
                select d.id
                from drivers d
                where d.user_id = :userId
                """)
                .param("userId", userId)
                .query(Long.class)
                .optional();
    }

    @Override
    public void setAvailable(long driverId, boolean available) {
        jdbc.sql("""
                update drivers
                set available = :available
                where id = :driverId
                """)
                .param("available", available)
                .param("driverId", driverId)
                .update();
    }
}
