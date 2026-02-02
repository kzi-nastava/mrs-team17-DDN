package org.example.backend.repository;

import org.example.backend.dto.response.DriverRideDetailsResponseDto;
import org.example.backend.dto.response.DriverRideHistoryResponseDto;
import org.example.backend.dto.response.PassengerInfoResponseDto;
import org.example.backend.dto.response.RideReportResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcDriverRideRepository implements DriverRideRepository {

    private final JdbcClient jdbc;

    public JdbcDriverRideRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<DriverRideHistoryResponseDto> findDriverRides(
            Long driverId,
            LocalDate from,
            LocalDate to
    ) {
        String sql = """
            select
                r.id                    as ride_id,
                r.started_at,
                r.start_address,
                r.destination_address,
                r.canceled,
                r.status,
                r.price
            from rides r
            where r.driver_id = :driverId
              and (cast(:from as date) is null or r.started_at::date >= cast(:from as date))
              and (cast(:to   as date) is null or r.started_at::date <= cast(:to   as date))
            order by r.started_at desc
        """;

        return jdbc.sql(sql)
                .param("driverId", driverId)
                .param("from", from)
                .param("to", to)
                .query((rs, rowNum) -> {
                    DriverRideHistoryResponseDto dto = new DriverRideHistoryResponseDto();
                    dto.setRideId(rs.getLong("ride_id"));
                    dto.setStartedAt(rs.getObject("started_at", java.time.OffsetDateTime.class));
                    dto.setStartAddress(rs.getString("start_address"));
                    dto.setEndAddress(rs.getString("destination_address"));
                    dto.setCanceled(rs.getBoolean("canceled"));
                    dto.setStatus(rs.getString("status"));
                    dto.setPrice(rs.getDouble("price"));
                    return dto;
                })
                .list();
    }

    @Override
    public Optional<DriverRideDetailsResponseDto> findDriverRideDetails(
            Long driverId,
            Long rideId
    ) {
        String rideSql = """
            select *
            from rides
            where id = :rideId
              and driver_id = :driverId
        """;

        Optional<DriverRideDetailsResponseDto> ride = jdbc.sql(rideSql)
                .param("rideId", rideId)
                .param("driverId", driverId)
                .query(rs -> {
                    if (!rs.next()) return Optional.empty();

                    DriverRideDetailsResponseDto dto = new DriverRideDetailsResponseDto();
                    dto.setRideId(rs.getLong("id"));
                    dto.setStartedAt(rs.getObject("started_at", java.time.OffsetDateTime.class));
                    dto.setEndedAt(rs.getObject("ended_at", java.time.OffsetDateTime.class));
                    dto.setStartAddress(rs.getString("start_address"));
                    dto.setDestinationAddress(rs.getString("destination_address"));
                    dto.setStops(null); // popunjavamo ispod
                    dto.setCanceled(rs.getBoolean("canceled"));
                    dto.setCanceledBy(rs.getString("canceled_by"));
                    dto.setStatus(rs.getString("status"));
                    dto.setPrice(rs.getDouble("price"));
                    dto.setPanicTriggered(rs.getBoolean("panic_triggered"));
                    dto.setPassengers(null); // popunjavamo ispod
                    dto.setReports(null); // popunjavamo ispod
                    return Optional.of(dto);
                });

        ride.ifPresent(dto -> {
            dto.setStops(findStops(rideId));
            dto.setPassengers(findPassengers(rideId));
            dto.setReports(findReports(rideId));
        });

        return ride;
    }

    @Override
    public Optional<DriverRideDetailsResponseDto> findActiveRideDetails(
            Long driverId
    ) {
        String sql = """
        select *
        from rides
        where driver_id = :driverId
          and ended_at is null
          and canceled = false
          and status = 'ACTIVE'
        limit 1
    """;

        return jdbc.sql(sql)
                .param("driverId", driverId)
                .query(rs -> {
                    if (!rs.next()) return Optional.empty();

                    DriverRideDetailsResponseDto dto = new DriverRideDetailsResponseDto();
                    Long rideId = rs.getLong("id");

                    dto.setRideId(rideId);
                    dto.setStartedAt(rs.getObject("started_at", java.time.OffsetDateTime.class));
                    dto.setEndedAt(null);
                    dto.setStartAddress(rs.getString("start_address"));
                    dto.setDestinationAddress(rs.getString("destination_address"));
                    dto.setCanceled(false);
                    dto.setCanceledBy(null);
                    dto.setStatus(rs.getString("status"));
                    dto.setPrice(rs.getDouble("price"));
                    dto.setPanicTriggered(rs.getBoolean("panic_triggered"));

                    dto.setStops(findStops(rideId));
                    dto.setPassengers(findPassengers(rideId));
                    dto.setReports(findReports(rideId));

                    return Optional.of(dto);
                });
    }


    @Override
    public List<DriverRideDetailsResponseDto> findAcceptedRides(Long driverId) {
        String sql = """
            select r.id
            from rides r
            where r.driver_id = :driverId
              and r.status = 'ACCEPTED'
              and r.ended_at is null
              and r.canceled = false
            order by coalesce(r.scheduled_at, r.created_at) asc
        """;

        List<Long> rideIds = jdbc.sql(sql)
                .param("driverId", driverId)
                .query(Long.class)
                .list();

        List<DriverRideDetailsResponseDto> out = new ArrayList<>();
        for (Long rideId : rideIds) {
            findDriverRideDetails(driverId, rideId).ifPresent(out::add);
        }
        return out;
    }

    @Override
    public boolean startRide(Long driverId, Long rideId) {
        int updated = jdbc.sql("""
            update rides
            set
                status = 'ACTIVE',
                started_at = case
                    when started_at is null then now()
                    else started_at
                end
            where id = :rideId
              and driver_id = :driverId
              and ended_at is null
              and canceled = false
              and status in ('ACCEPTED', 'ACTIVE')
        """)
                .param("rideId", rideId)
                .param("driverId", driverId)
                .update();

        return updated > 0;
    }

    private List<String> findStops(Long rideId) {
        return jdbc.sql("""
            select address
            from ride_stops
            where ride_id = :rideId
            order by stop_order
        """)
                .param("rideId", rideId)
                .query(String.class)
                .list();
    }

    private List<PassengerInfoResponseDto> findPassengers(Long rideId) {
        return jdbc.sql("""
            select name, email
            from ride_passengers
            where ride_id = :rideId
        """)
                .param("rideId", rideId)
                .query((rs, rowNum) ->
                        new PassengerInfoResponseDto(
                                rs.getString("name"),
                                rs.getString("email")
                        )
                )
                .list();
    }

    private List<RideReportResponseDto> findReports(Long rideId) {
        return jdbc.sql("""
            select id, ride_id, description, created_at
            from ride_reports
            where ride_id = :rideId
            order by created_at desc
        """)
                .param("rideId", rideId)
                .query((rs, rowNum) -> {
                    RideReportResponseDto r = new RideReportResponseDto();
                    r.setId(rs.getLong("id"));
                    r.setRideId(rs.getLong("ride_id"));
                    r.setDescription(rs.getString("description"));
                    r.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
                    return r;
                })
                .list();
    }
}
