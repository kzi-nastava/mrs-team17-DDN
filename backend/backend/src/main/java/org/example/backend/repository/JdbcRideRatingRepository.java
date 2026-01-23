package org.example.backend.repository;

import org.example.backend.dto.request.RideRatingRequestDto;
import org.example.backend.dto.response.RideRatingResponseDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class JdbcRideRatingRepository implements RideRatingRepository {

    private final JdbcClient jdbc;

    public JdbcRideRatingRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<RideRatingResponseDto> findByRideId(Long rideId) {
        String sql = """
            select
                rr.id,
                rr.ride_id,
                rr.driver_rating,
                rr.vehicle_rating,
                rr.comment,
                rr.created_at
            from ride_ratings rr
            where rr.ride_id = :rideId
        """;

        return jdbc.sql(sql)
                .param("rideId", rideId)
                .query((rs, rowNum) -> {
                    RideRatingResponseDto dto = new RideRatingResponseDto();
                    dto.setId(rs.getLong("id"));
                    dto.setRideId(rs.getLong("ride_id"));
                    dto.setDriverRating(rs.getInt("driver_rating"));
                    dto.setVehicleRating(rs.getInt("vehicle_rating"));
                    dto.setComment(rs.getString("comment"));
                    dto.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    return dto;
                })
                .optional();
    }

    @Override
    public boolean existsForRide(Long rideId) {
        Integer x = jdbc.sql("""
            select 1
            from ride_ratings
            where ride_id = :rideId
        """)
                .param("rideId", rideId)
                .query(Integer.class)
                .optional()
                .orElse(null);

        return x != null;
    }

    @Override
    public boolean isRideCompletedAndNotCanceled(Long rideId) {
        Integer c = jdbc.sql("""
            select count(1)
            from rides
            where id = :rideId
              and canceled = false
              and status = 'COMPLETED'
              and ended_at is not null
        """)
                .param("rideId", rideId)
                .query(Integer.class)
                .single();

        return c != null && c > 0;
    }

    @Override
    public RideRatingResponseDto create(Long rideId, RideRatingRequestDto req, OffsetDateTime now) {
        Long id = jdbc.sql("""
            insert into ride_ratings (ride_id, driver_rating, vehicle_rating, comment, created_at)
            values (:rideId, :driver, :vehicle, :comment, :createdAt)
            returning id
        """)
                .param("rideId", rideId)
                .param("driver", req.getDriverRating())
                .param("vehicle", req.getVehicleRating())
                .param("comment", normalizeComment(req.getComment()))
                .param("createdAt", now)
                .query(Long.class)
                .single();

        RideRatingResponseDto dto = new RideRatingResponseDto();
        dto.setId(id);
        dto.setRideId(rideId);
        dto.setDriverRating(req.getDriverRating());
        dto.setVehicleRating(req.getVehicleRating());
        dto.setComment(normalizeComment(req.getComment()));
        dto.setCreatedAt(now);
        return dto;
    }

    private static String normalizeComment(String c) {
        if (c == null) return null;
        String t = c.trim();
        return t.isEmpty() ? null : t;
    }
}
