package org.example.backend.repository;

import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.response.RideReportResponseDto;
import org.example.backend.dto.response.RideTrackingResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class JdbcRideRepository implements RideRepository {

    private final JdbcClient jdbc;

    public JdbcRideRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<RideTrackingResponseDto> findTrackingByRideId(Long rideId) {
        // ako ti tracking već postoji kroz drugi mehanizam, može ostati InMemory.
        // Za sada: vrati empty da se ne koristi slučajno.
        return Optional.empty();
    }

    @Override
    public RideReportResponseDto createReport(Long rideId, RideReportRequestDto request, OffsetDateTime now) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean finishRide(Long rideId) {
        int updated = jdbc.sql("""
            update rides
            set ended_at = now(),
                status = 'COMPLETED'
            where id = :rideId
              and ended_at is null
              and canceled = false
        """)
                .param("rideId", rideId)
                .update();

        return updated > 0;
    }
}
