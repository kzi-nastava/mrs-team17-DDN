package org.example.backend.repository;

import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcDriverRepository implements DriverRepository {

    private final JdbcClient jdbc;
    private final SimpleJdbcInsert driverInsert;

    public JdbcDriverRepository(JdbcClient jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.driverInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("drivers")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long insertDriverReturningId(Long userId) {
        Number key = driverInsert.executeAndReturnKey(
                Map.of(
                        "user_id", userId,
                        "available", false
                )
        );
        return key.longValue();
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
