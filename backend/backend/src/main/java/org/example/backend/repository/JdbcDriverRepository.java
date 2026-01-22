package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

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
}
