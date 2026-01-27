package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcDriverRepository implements DriverRepository {

    private final JdbcClient jdbc;

    public JdbcDriverRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Long insertDriverReturningId(Long userId) {
        String sql = """
            insert into drivers (user_id, available)
            values (:userId, false)
            returning id
        """;

        return jdbc.sql(sql)
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
