package org.example.backend.repository;

import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.response.LatLngDto;
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

        String sql = """
            select
                r.status,
                r.start_lat, r.start_lng,
                r.dest_lat,  r.dest_lng,
                r.car_lat,   r.car_lng
            from rides r
            where r.id = :rideId
        """;

        return jdbc.sql(sql)
                .param("rideId", rideId)
                .query((rs, rowNum) -> {

                    RideTrackingResponseDto dto = new RideTrackingResponseDto();

                    dto.setStatus(rs.getString("status"));

                    LatLngDto pickup = new LatLngDto(
                            rs.getDouble("start_lat"),
                            rs.getDouble("start_lng")
                    );

                    LatLngDto destination = new LatLngDto(
                            rs.getDouble("dest_lat"),
                            rs.getDouble("dest_lng")
                    );

                    LatLngDto car = new LatLngDto(
                            rs.getDouble("car_lat"),
                            rs.getDouble("car_lng")
                    );

                    dto.setPickup(pickup);
                    dto.setDestination(destination);
                    dto.setCar(car);

                    double distanceKm = haversineKm(
                            car.getLat(), car.getLng(),
                            destination.getLat(), destination.getLng()
                    );

                    dto.setDistanceKm(round2(distanceKm));

                    int etaMinutes = (int) Math.max(
                            1,
                            Math.round((distanceKm / 30.0) * 60.0)
                    );
                    dto.setEtaMinutes(etaMinutes);

                    return dto;
                })
                .optional();
    }

    @Override
    public RideReportResponseDto createReport(Long rideId, RideReportRequestDto request, OffsetDateTime now) {

        if (!isRideActive(rideId)) {
            throw new IllegalStateException("Ride is not active");
        }

        String desc = request.getDescription() == null ? "" : request.getDescription().trim();
        if (desc.length() < 5) {
            throw new IllegalArgumentException("Description too short");
        }

        Long reportId = jdbc.sql("""
            insert into ride_reports (ride_id, description, created_at)
            values (:rideId, :description, :createdAt)
            returning id
        """)
                .param("rideId", rideId)
                .param("description", desc)
                .param("createdAt", now)
                .query(Long.class)
                .single();

        RideReportResponseDto dto = new RideReportResponseDto();
        dto.setId(reportId);
        dto.setRideId(rideId);
        dto.setDescription(desc);
        dto.setCreatedAt(now);

        return dto;
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

    // ================= helpers =================

    private boolean isRideActive(Long rideId) {
        Integer c = jdbc.sql("""
            select count(1)
            from rides
            where id = :rideId
              and canceled = false
              and ended_at is null
              and status = 'ACTIVE'
        """)
                .param("rideId", rideId)
                .query(Integer.class)
                .single();

        return c != null && c > 0;
    }

    private static double haversineKm(
            double lat1, double lon1,
            double lat2, double lon2
    ) {
        final double R = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
