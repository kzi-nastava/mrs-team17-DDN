package org.example.backend.repository;

import org.example.backend.dto.response.RideStatsPointDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class JdbcRideStatsRepository implements RideStatsRepository {

    private final JdbcClient jdbc;

    public JdbcRideStatsRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<RideStatsPointDto> aggregateForDriver(long driverId, LocalDate from, LocalDate to) {
        String sql = """
            select
                r.started_at::date as day,
                count(*) as rides,
                coalesce(sum(r.est_distance_meters), 0) / 1000.0 as kilometers,
                coalesce(sum(r.price), 0) as money
            from rides r
            where r.driver_id = :driverId
              and r.status = 'COMPLETED'
              and r.started_at is not null
              and r.started_at::date >= cast(:from as date)
              and r.started_at::date <= cast(:to as date)
            group by r.started_at::date
            order by r.started_at::date
        """;

        return jdbc.sql(sql)
                .param("driverId", driverId)
                .param("from", from)
                .param("to", to)
                .query((rs, rowNum) -> {
                    RideStatsPointDto p = new RideStatsPointDto();
                    p.setDate(rs.getObject("day", LocalDate.class));
                    p.setRides(rs.getLong("rides"));
                    p.setKilometers(rs.getDouble("kilometers"));
                    p.setMoney(rs.getDouble("money"));
                    return p;
                })
                .list();
    }

    @Override
    public List<RideStatsPointDto> aggregateForPassengerEmail(String email, LocalDate from, LocalDate to) {
        if (email == null || email.trim().isEmpty()) {
            return List.of();
        }

        // Use distinct rides to avoid double-counting if the same passenger is listed multiple times.
        String sql = """
            select
                x.day,
                count(*) as rides,
                coalesce(sum(x.kilometers), 0) as kilometers,
                coalesce(sum(x.money), 0) as money
            from (
                select distinct
                    r.id,
                    r.started_at::date as day,
                    coalesce(r.est_distance_meters, 0) / 1000.0 as kilometers,
                    coalesce(r.price, 0) as money
                from rides r
                join ride_passengers rp on rp.ride_id = r.id
                where lower(rp.email) = lower(:email)
                  and r.status = 'COMPLETED'
                  and r.started_at is not null
                  and r.started_at::date >= cast(:from as date)
                  and r.started_at::date <= cast(:to as date)
            ) x
            group by x.day
            order by x.day
        """;

        return jdbc.sql(sql)
                .param("email", email.trim())
                .param("from", from)
                .param("to", to)
                .query((rs, rowNum) -> {
                    RideStatsPointDto p = new RideStatsPointDto();
                    p.setDate(rs.getObject("day", LocalDate.class));
                    p.setRides(rs.getLong("rides"));
                    p.setKilometers(rs.getDouble("kilometers"));
                    p.setMoney(rs.getDouble("money"));
                    return p;
                })
                .list();
    }

    @Override
    public List<RideStatsPointDto> aggregateForAllCompleted(LocalDate from, LocalDate to) {
        String sql = """
            select
                r.started_at::date as day,
                count(*) as rides,
                coalesce(sum(r.est_distance_meters), 0) / 1000.0 as kilometers,
                coalesce(sum(r.price), 0) as money
            from rides r
            where r.status = 'COMPLETED'
              and r.started_at is not null
              and r.started_at::date >= cast(:from as date)
              and r.started_at::date <= cast(:to as date)
            group by r.started_at::date
            order by r.started_at::date
        """;

        return jdbc.sql(sql)
                .param("from", from)
                .param("to", to)
                .query((rs, rowNum) -> {
                    RideStatsPointDto p = new RideStatsPointDto();
                    p.setDate(rs.getObject("day", LocalDate.class));
                    p.setRides(rs.getLong("rides"));
                    p.setKilometers(rs.getDouble("kilometers"));
                    p.setMoney(rs.getDouble("money"));
                    return p;
                })
                .list();
    }
}
