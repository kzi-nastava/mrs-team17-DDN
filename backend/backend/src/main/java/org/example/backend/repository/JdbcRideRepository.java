package org.example.backend.repository;

import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.response.LatLngDto;
import org.example.backend.dto.response.RideReportResponseDto;
import org.example.backend.dto.response.RideTrackingResponseDto;
import org.example.backend.osrm.OsrmClient;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class JdbcRideRepository implements RideRepository {

    private final JdbcClient jdbc;
    private final OsrmClient osrm;

    public JdbcRideRepository(JdbcClient jdbc, OsrmClient osrm) {
        this.jdbc = jdbc;
        this.osrm = osrm;
    }
    @Override
    public java.util.List<Long> findActiveRideIds() {
        return jdbc.sql("""
        select r.id
        from rides r
        where r.status = 'ACTIVE'
          and r.canceled = false
          and r.ended_at is null
    """)
                .query(Long.class)
                .list();
    }


    @Override
    public Optional<RideTrackingResponseDto> findTrackingByRideId(Long rideId) {

        String sql = """
            select
                r.status,
                r.start_lat, r.start_lng,
                r.dest_lat,  r.dest_lng,
                v.latitude   as car_lat,
                v.longitude  as car_lng
            from rides r
            join vehicles v
              on v.driver_id = r.driver_id
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

                    try {
                        var route = osrm.routeDrivingWithGeometry(
                                java.util.List.of(
                                        new OsrmClient.Point(pickup.getLat(), pickup.getLng()),
                                        new OsrmClient.Point(destination.getLat(), destination.getLng())
                                )
                        );

                        var routePoints = route.geometry().stream()
                                .map(p -> new LatLngDto(p.lat(), p.lon()))
                                .toList();

                        dto.setRoute(routePoints);

                    } catch (Exception e) {
                        dto.setRoute(java.util.List.of());
                    }

                    double distanceKm;
                    int etaMinutes;

                    try {
                        var res = osrm.routeDriving(
                                java.util.List.of(
                                        new OsrmClient.Point(car.getLat(), car.getLng()),
                                        new OsrmClient.Point(destination.getLat(), destination.getLng())
                                )
                        );

                        System.out.println(
                                "OSRM OK: meters=" + res.distanceMeters()
                                        + ", seconds=" + res.durationSeconds()
                        );

                        distanceKm = res.distanceMeters() / 1000.0;
                        etaMinutes = (int) Math.max(
                                1,
                                Math.round(res.durationSeconds() / 60.0)
                        );

                    } catch (Exception e) {
                        System.err.println(
                                "OSRM FAILED → fallback: "
                                        + e.getClass().getSimpleName()
                                        + " - " + e.getMessage()
                        );

                        double fallbackKm = haversineKm(
                                car.getLat(), car.getLng(),
                                destination.getLat(), destination.getLng()
                        );

                        distanceKm = fallbackKm;
                        etaMinutes = (int) Math.max(
                                1,
                                Math.round((fallbackKm / 30.0) * 60.0)
                        );
                    }

                    dto.setDistanceKm(round2(distanceKm));
                    dto.setEtaMinutes(etaMinutes);

                    return dto;
                })
                .optional();
    }

    /**
     * Reads minimal data needed for moving a vehicle (simulation).
     * Returns empty if ride/vehicle not found.
     */
    @Override
    public Optional<RideMoveSnapshot> findMoveSnapshot(Long rideId) {

        String sql = """
        select
            r.status,
            r.ended_at,
            r.canceled,
            r.driver_id,
            r.start_lat as pickup_lat,
            r.start_lng as pickup_lng,
            r.dest_lat  as dest_lat,
            r.dest_lng  as dest_lng,
            v.latitude  as car_lat,
            v.longitude as car_lng
        from rides r
        join vehicles v
          on v.driver_id = r.driver_id
        where r.id = :rideId
    """;

        return jdbc.sql(sql)
                .param("rideId", rideId)
                .query((rs, rowNum) -> new RideMoveSnapshot(
                        rs.getString("status"),
                        rs.getObject("ended_at", java.time.OffsetDateTime.class),
                        rs.getBoolean("canceled"),
                        rs.getLong("driver_id"),
                        rs.getDouble("car_lat"),
                        rs.getDouble("car_lng"),
                        rs.getDouble("pickup_lat"),
                        rs.getDouble("pickup_lng"),
                        rs.getDouble("dest_lat"),
                        rs.getDouble("dest_lng")
                ))
                .optional();
    }


    @Override
    public boolean updateVehicleLocation(long driverId, double lat, double lng) {
        int updated = jdbc.sql("""
            update vehicles
            set latitude = :lat,
                longitude = :lng,
                updated_at = now()
            where driver_id = :driverId
        """)
                .param("lat", lat)
                .param("lng", lng)
                .param("driverId", driverId)
                .update();

        return updated > 0;
    }

    @Override
    public RideReportResponseDto createReport(
            Long rideId,
            RideReportRequestDto request,
            OffsetDateTime now
    ) {

        if (!isRideActive(rideId)) {
            throw new IllegalStateException("Ride is not active");
        }

        String desc = request.getDescription() == null
                ? ""
                : request.getDescription().trim();

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
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
