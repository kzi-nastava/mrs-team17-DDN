package org.example.backend.repository;

import org.example.backend.dto.response.PassengerRideHistoryResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class JdbcPassengerRideRepository implements PassengerRideRepository {

    private final JdbcClient jdbc;

    public JdbcPassengerRideRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<PassengerRideHistoryResponseDto> findPassengerRides(String passengerEmail, LocalDate from, LocalDate to) {
        if (passengerEmail == null || passengerEmail.trim().isEmpty()) {
            return List.of();
        }

        String sql = """
            select distinct
                r.id as ride_id,
                r.started_at,
                r.start_address,
                r.destination_address
            from rides r
            join ride_passengers rp on rp.ride_id = r.id
            where lower(rp.email) = lower(:email)
              and (cast(:from as date) is null or r.started_at::date >= cast(:from as date))
              and (cast(:to   as date) is null or r.started_at::date <= cast(:to   as date))
            order by r.started_at desc
        """;

        List<PassengerRideHistoryResponseDto> list = jdbc.sql(sql)
                .param("email", passengerEmail.trim())
                .param("from", from)
                .param("to", to)
                .query((rs, rowNum) -> {
                    PassengerRideHistoryResponseDto dto = new PassengerRideHistoryResponseDto();
                    Long rideId = rs.getLong("ride_id");
                    dto.setRideId(rideId);
                    dto.setStartedAt(rs.getObject("started_at", java.time.OffsetDateTime.class));
                    dto.setStartAddress(rs.getString("start_address"));
                    dto.setDestinationAddress(rs.getString("destination_address"));
                    // stops popunjavamo ispod
                    return dto;
                })
                .list();

        for (PassengerRideHistoryResponseDto dto : list) {
            dto.setStops(findStops(dto.getRideId()));
        }

        return list;
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
}
