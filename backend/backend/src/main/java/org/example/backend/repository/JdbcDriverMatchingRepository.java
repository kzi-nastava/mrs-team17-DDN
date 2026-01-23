package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcDriverMatchingRepository implements DriverMatchingRepository {

    private final JdbcClient jdbc;

    public JdbcDriverMatchingRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CandidateDriver> findAvailableDrivers(String vehicleTypeLower, boolean babyTransport, boolean petTransport) {
        String sql = """
            select
                d.id as driver_id,
                v.latitude as lat,
                v.longitude as lng
            from drivers d
            join vehicles v on v.driver_id = d.id
            left join users u on u.id = d.user_id
            where d.available = true
              and v.type = :type
              and (:baby = false or v.baby_transport = true)
              and (:pet  = false or v.pet_transport  = true)
              and (d.user_id is null or u.is_active = true)
        """;

        return jdbc.sql(sql)
                .param("type", vehicleTypeLower)
                .param("baby", babyTransport)
                .param("pet", petTransport)
                .query((rs, rowNum) -> new CandidateDriver(
                        rs.getLong("driver_id"),
                        rs.getDouble("lat"),
                        rs.getDouble("lng")
                ))
                .list();
    }

    @Override
    public boolean setDriverAvailable(Long driverId, boolean available) {
        int updated = jdbc.sql("""
            update drivers
            set available = :available
            where id = :id
        """)
                .param("available", available)
                .param("id", driverId)
                .update();

        return updated > 0;
    }
}
