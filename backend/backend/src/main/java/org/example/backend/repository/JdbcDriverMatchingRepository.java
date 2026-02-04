package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcDriverMatchingRepository implements DriverMatchingRepository {

    private static final int MAX_WORK_SECONDS_LAST_24H = 8 * 3600;

    private final JdbcClient jdbc;

    public JdbcDriverMatchingRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<CandidateDriver> findAvailableDrivers(String vehicleTypeLower, boolean babyTransport, boolean petTransport, int requiredSeats) {
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
              and v.seats >= :seats
              and (:baby = false or v.baby_transport = true)
              and (:pet  = false or v.pet_transport  = true)
              and (d.user_id is null or (u.is_active = true and u.blocked = false))
              and not exists (
                    select 1
                    from rides r
                    where r.driver_id = d.id
                      and r.status = 'ACTIVE'
                      and r.ended_at is null
                      and r.canceled = false
              )
              and coalesce((
                    select extract(epoch from sum(coalesce(r2.ended_at, now()) - r2.started_at))
                    from rides r2
                    where r2.driver_id = d.id
                      and r2.started_at is not null
                      and r2.started_at >= now() - interval '24 hours'
                      and r2.canceled = false
              ), 0) < :maxWorkSeconds
        """;

        return jdbc.sql(sql)
                .param("type", vehicleTypeLower)
                .param("seats", requiredSeats)
                .param("baby", babyTransport)
                .param("pet", petTransport)
                .param("maxWorkSeconds", MAX_WORK_SECONDS_LAST_24H)
                .query((rs, rowNum) -> new CandidateDriver(
                        rs.getLong("driver_id"),
                        rs.getDouble("lat"),
                        rs.getDouble("lng")
                ))
                .list();
    }

    @Override
    public List<FinishingSoonDriver> findDriversFinishingSoon(
            String vehicleTypeLower,
            boolean babyTransport,
            boolean petTransport,
            int requiredSeats,
            int remainingSecondsThreshold
    ) {
        String sql = """
            select
                d.id as driver_id,
                v.latitude as lat,
                v.longitude as lng,
                r.dest_lat as finish_lat,
                r.dest_lng as finish_lng,
                (r.est_duration_seconds - extract(epoch from (now() - r.started_at))) as remaining_sec
            from drivers d
            join vehicles v on v.driver_id = d.id
            join rides r on r.driver_id = d.id
            left join users u on u.id = d.user_id
            where r.status = 'ACTIVE'
              and r.ended_at is null
              and r.canceled = false
              and r.started_at is not null
              and r.est_duration_seconds is not null
              and (r.est_duration_seconds - extract(epoch from (now() - r.started_at))) between 0 and :thr
              and v.type = :type
              and v.seats >= :seats
              and (:baby = false or v.baby_transport = true)
              and (:pet  = false or v.pet_transport  = true)
              and (d.user_id is null or (u.is_active = true and u.blocked = false))
              and not exists (
                    select 1
                    from rides r3
                    where r3.driver_id = d.id
                      and r3.status = 'ACCEPTED'
                      and r3.ended_at is null
                      and r3.canceled = false
              )
              and coalesce((
                    select extract(epoch from sum(coalesce(r2.ended_at, now()) - r2.started_at))
                    from rides r2
                    where r2.driver_id = d.id
                      and r2.started_at is not null
                      and r2.started_at >= now() - interval '24 hours'
                      and r2.canceled = false
              ), 0) < :maxWorkSeconds
        """;

        return jdbc.sql(sql)
                .param("type", vehicleTypeLower)
                .param("seats", requiredSeats)
                .param("baby", babyTransport)
                .param("pet", petTransport)
                .param("thr", remainingSecondsThreshold)
                .param("maxWorkSeconds", MAX_WORK_SECONDS_LAST_24H)
                .query((rs, rowNum) -> new FinishingSoonDriver(
                        rs.getLong("driver_id"),
                        rs.getDouble("lat"),
                        rs.getDouble("lng"),
                        rs.getDouble("finish_lat"),
                        rs.getDouble("finish_lng"),
                        rs.getDouble("remaining_sec")
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

    @Override
    public boolean tryClaimAvailableDriver(Long driverId) {
        int updated = jdbc.sql("""
            update drivers
            set available = false
            where id = :id
              and available = true
        """)
                .param("id", driverId)
                .update();

        return updated > 0;
    }
}
