// backend/src/main/java/org/example/backend/repository/JdbcRideRepository.java
package org.example.backend.repository;

import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.response.LatLngDto;
import org.example.backend.dto.response.RideReportResponseDto;
import org.example.backend.dto.response.RideTrackingResponseDto;
import org.example.backend.osrm.OsrmClient;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

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
    public Optional<Long> findActiveRideIdForPassenger(long userId) {
        return jdbc.sql("""
            select r.id
            from rides r
            join ride_passengers rp
              on rp.ride_id = r.id
            join users u
              on lower(u.email) = lower(rp.email)
            where u.id = :userId
              and r.status = 'ACTIVE'
              and r.canceled = false
              and r.ended_at is null
            order by r.started_at desc nulls last, r.id desc
            limit 1
        """)
                .param("userId", userId)
                .query(Long.class)
                .optional();
    }

    // NEW: last completed ride (<= 3 days) that is NOT rated yet, for this passenger (by email)
    @Override
    public Optional<Long> findRideIdToRateForPassenger(long userId) {
        return jdbc.sql("""
            select r.id
            from rides r
            join ride_passengers rp
              on rp.ride_id = r.id
            join users u
              on lower(u.email) = lower(rp.email)
            left join ride_ratings rr
              on rr.ride_id = r.id
            where u.id = :userId
              and r.status = 'COMPLETED'
              and r.canceled = false
              and r.ended_at is not null
              and r.ended_at >= now() - interval '3 days'
              and rr.id is null
            order by r.ended_at desc, r.id desc
            limit 1
        """)
                .param("userId", userId)
                .query(Long.class)
                .optional();
    }

    @Override
    public Optional<RideTrackingResponseDto> findTrackingByRideId(Long rideId) {

        String sql = """
            select
                r.status,
                r.picked_up,
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

                    boolean pickedUp = rs.getBoolean("picked_up");

                    double startLat = rs.getDouble("start_lat");
                    double startLng = rs.getDouble("start_lng");
                    double destLat  = rs.getDouble("dest_lat");
                    double destLng  = rs.getDouble("dest_lng");
                    double carLat   = rs.getDouble("car_lat");
                    double carLng   = rs.getDouble("car_lng");

                    RideTrackingResponseDto dto = new RideTrackingResponseDto();
                    dto.setStatus(rs.getString("status"));

                    LatLngDto pickup = new LatLngDto(startLat, startLng);
                    LatLngDto destination = new LatLngDto(destLat, destLng);
                    LatLngDto car = new LatLngDto(carLat, carLng);

                    dto.setPickup(pickup);
                    dto.setDestination(destination);
                    dto.setCar(car);

                    // ROUTE: uvek pickup -> destination (statična linija)
                    try {
                        var route = osrm.routeDrivingWithGeometry(
                                java.util.List.of(
                                        new OsrmClient.Point(pickup.getLat(), pickup.getLng()),
                                        new OsrmClient.Point(destination.getLat(), destination.getLng())
                                )
                        );

                        dto.setRoute(
                                route.geometry().stream()
                                        .map(p -> new LatLngDto(p.lat(), p.lon()))
                                        .toList()
                        );
                    } catch (Exception e) {
                        dto.setRoute(java.util.List.of());
                    }

                    double distanceKm;
                    int etaMinutes;

                    if (!pickedUp) {
                        // 🚕 VOZAČ IDE KA PICKUP-U → ETA/DISTANCE ZAMRZNUTI
                        var res = osrm.routeDriving(
                                java.util.List.of(
                                        new OsrmClient.Point(pickup.getLat(), pickup.getLng()),
                                        new OsrmClient.Point(destination.getLat(), destination.getLng())
                                )
                        );

                        distanceKm = res.distanceMeters() / 1000.0;
                        etaMinutes = (int) Math.max(1, Math.ceil(res.durationSeconds() / 60.0));

                    } else {
                        // 🚗 NAKON PICKUP-A → REAL-TIME
                        var res = osrm.routeDriving(
                                java.util.List.of(
                                        new OsrmClient.Point(car.getLat(), car.getLng()),
                                        new OsrmClient.Point(destination.getLat(), destination.getLng())
                                )
                        );

                        distanceKm = res.distanceMeters() / 1000.0;
                        etaMinutes = (int) Math.max(1, Math.ceil(res.durationSeconds() / 60.0));
                    }

                    dto.setDistanceKm(Math.round(distanceKm * 100.0) / 100.0);
                    dto.setEtaMinutes(etaMinutes);

                    return dto;
                })
                .optional();
    }

    @Override
    public java.util.List<String> findPassengerEmails(Long rideId) {
        return jdbc.sql("""
            select email
            from ride_passengers
            where ride_id = :rideId
        """)
                .param("rideId", rideId)
                .query(String.class)
                .list();
    }

    @Override
    public java.util.Optional<RideAddresses> findRideAddresses(Long rideId) {
        return jdbc.sql("""
            select start_address, destination_address
            from rides
            where id = :rideId
        """)
                .param("rideId", rideId)
                .query((rs, rowNum) -> new RideAddresses(
                        rs.getString("start_address"),
                        rs.getString("destination_address")
                ))
                .optional();
    }

    @Override
    public Optional<RideMoveSnapshot> findMoveSnapshot(Long rideId) {

        String sql = """
            select
                r.status,
                r.ended_at,
                r.canceled,
                r.picked_up,
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
                .query((rs, rowNum) -> {

                    Double pickupLat = (Double) rs.getObject("pickup_lat");
                    Double pickupLng = (Double) rs.getObject("pickup_lng");
                    Double destLat   = (Double) rs.getObject("dest_lat");
                    Double destLng   = (Double) rs.getObject("dest_lng");
                    Double carLat    = (Double) rs.getObject("car_lat");
                    Double carLng    = (Double) rs.getObject("car_lng");

                    if (pickupLat == null || pickupLng == null || destLat == null || destLng == null || carLat == null || carLng == null) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Missing coordinates for simulation (pickup/dest/car)"
                        );
                    }

                    return new RideMoveSnapshot(
                            rs.getString("status"),
                            rs.getObject("ended_at", java.time.OffsetDateTime.class),
                            rs.getBoolean("canceled"),
                            rs.getBoolean("picked_up"),
                            rs.getLong("driver_id"),
                            carLat,
                            carLng,
                            pickupLat,
                            pickupLng,
                            destLat,
                            destLng
                    );
                })
                .optional();
    }

    @Override
    public boolean markPickedUp(Long rideId) {
        int updated = jdbc.sql("""
            update rides
            set picked_up = true
            where id = :rideId
        """)
                .param("rideId", rideId)
                .update();

        return updated > 0;
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

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
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

    @Override
    public boolean startRide(Long rideId) {
        int updated = jdbc.sql("""
            update rides
            set
                status = 'ACTIVE',
                started_at = case
                    when status = 'ACCEPTED' then now()
                    else started_at
                end
            where id = :rideId
              and ended_at is null
              and canceled = false
              and status in ('ACCEPTED', 'ACTIVE')
        """)
                .param("rideId", rideId)
                .update();

        return updated > 0;
    }
}
