package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class JdbcScheduledRideRepository implements ScheduledRideRepository {

    private final JdbcClient jdbc;

    public JdbcScheduledRideRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<ScheduledRideRow> findDueScheduledRides(int minutesAhead) {
        String sql = """
            select
                r.id as ride_id,
                r.driver_id,
                r.scheduled_at,
                r.start_address, r.start_lat, r.start_lng,
                r.destination_address, r.dest_lat, r.dest_lng,
                r.vehicle_type,
                r.baby_transport,
                r.pet_transport,
                r.required_seats
            from rides r
            where r.status = 'SCHEDULED'
              and r.canceled = false
              and r.ended_at is null
              and r.started_at is null
              and r.scheduled_at is not null
              and r.scheduled_at <= now() + (:minutesAhead * interval '1 minute')
            order by r.scheduled_at asc
        """;

        return jdbc.sql(sql)
                .param("minutesAhead", minutesAhead)
                .query((rs, rowNum) -> new ScheduledRideRow(
                        rs.getLong("ride_id"),
                        (Long) rs.getObject("driver_id"),
                        rs.getObject("scheduled_at", OffsetDateTime.class),
                        rs.getString("start_address"),
                        rs.getDouble("start_lat"),
                        rs.getDouble("start_lng"),
                        rs.getString("destination_address"),
                        rs.getDouble("dest_lat"),
                        rs.getDouble("dest_lng"),
                        rs.getString("vehicle_type"),
                        rs.getBoolean("baby_transport"),
                        rs.getBoolean("pet_transport"),
                        rs.getInt("required_seats")
                ))
                .list();
    }

    @Override
    public boolean assignDriverToScheduledRide(Long rideId, Long driverId) {
        int updated = jdbc.sql("""
            update rides
            set driver_id = :driverId
            where id = :rideId
              and status = 'SCHEDULED'
              and driver_id is null
              and canceled = false
              and ended_at is null
        """)
                .param("driverId", driverId)
                .param("rideId", rideId)
                .update();

        return updated == 1;
    }

    @Override
    public boolean markScheduledRideAccepted(Long rideId) {
        int updated = jdbc.sql("""
            update rides
            set status = 'ACCEPTED'
            where id = :rideId
              and status = 'SCHEDULED'
              and driver_id is not null
              and canceled = false
              and ended_at is null
        """)
                .param("rideId", rideId)
                .update();

        return updated == 1;
    }
}
