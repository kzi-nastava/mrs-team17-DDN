package org.example.backend.repository;

import org.example.backend.dto.response.*;
import org.example.backend.osrm.OsrmClient;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPassengerRideDetailsRepository implements PassengerRideDetailsRepository {

    private final JdbcClient jdbc;
    private final OsrmClient osrm;

    public JdbcPassengerRideDetailsRepository(JdbcClient jdbc, OsrmClient osrm) {
        this.jdbc = jdbc;
        this.osrm = osrm;
    }

    @Override
    public boolean isPassengerOnRide(Long rideId, long userId) {
        Boolean allowed = jdbc.sql("""
            select exists (
                select 1
                from ride_passengers rp
                join users u on lower(u.email) = lower(rp.email)
                where rp.ride_id = :rideId
                  and u.id = :userId
            )
        """)
                .param("rideId", rideId)
                .param("userId", userId)
                .query(Boolean.class)
                .single();

        return Boolean.TRUE.equals(allowed);
    }

    @Override
    public Optional<RidePassengerDetailsResponseDto> findPassengerRideDetails(Long rideId) {

        String sql = """
            select
                r.id, r.status, r.started_at, r.ended_at,
                r.start_address, r.destination_address,
                r.start_lat, r.start_lng, r.dest_lat, r.dest_lng,
                r.driver_id, r.vehicle_type, r.baby_transport, r.pet_transport
            from rides r
            where r.id = :rideId
        """;

        Optional<RidePassengerDetailsResponseDto> base = jdbc.sql(sql)
                .param("rideId", rideId)
                .query((rs, rowNum) -> {
                    RidePassengerDetailsResponseDto dto = new RidePassengerDetailsResponseDto();
                    dto.setRideId(rs.getLong("id"));
                    dto.setStatus(rs.getString("status"));
                    dto.setStartedAt(rs.getObject("started_at", java.time.OffsetDateTime.class));
                    dto.setEndedAt(rs.getObject("ended_at", java.time.OffsetDateTime.class));
                    dto.setStartAddress(rs.getString("start_address"));
                    dto.setDestinationAddress(rs.getString("destination_address"));
                    dto.setVehicleType(rs.getString("vehicle_type"));
                    dto.setBabyTransport(rs.getBoolean("baby_transport"));
                    dto.setPetTransport(rs.getBoolean("pet_transport"));

                    Double startLat = (Double) rs.getObject("start_lat");
                    Double startLng = (Double) rs.getObject("start_lng");
                    Double destLat = (Double) rs.getObject("dest_lat");
                    Double destLng = (Double) rs.getObject("dest_lng");

                    if (startLat != null && startLng != null) {
                        dto.setStart(new LatLngDto(startLat, startLng));
                    }
                    if (destLat != null && destLng != null) {
                        dto.setDestination(new LatLngDto(destLat, destLng));
                    }

                    Long driverId = (Long) rs.getObject("driver_id");
                    if (driverId != null) {
                        dto.setDriver(fetchDriverPublicInfo(driverId));
                    }

                    return dto;
                })
                .optional();

        if (base.isEmpty()) {
            return Optional.empty();
        }

        RidePassengerDetailsResponseDto dto = base.get();

        // checkpoints
        List<RideCheckpointDto> checkpoints = jdbc.sql("""
            select stop_order, address, lat, lng
            from ride_stops
            where ride_id = :rideId
            order by stop_order asc
        """)
                .param("rideId", rideId)
                .query((rs2, rn) -> new RideCheckpointDto(
                        rs2.getInt("stop_order"),
                        rs2.getString("address"),
                        rs2.getDouble("lat"),
                        rs2.getDouble("lng")
                ))
                .list();
        dto.setStops(checkpoints);

        // route for the map (best-effort: if OSRM/coords are unavailable, leave it empty instead of failing)
        if (dto.getStart() != null && dto.getDestination() != null) {
            try {
                List<OsrmClient.Point> routePoints = new ArrayList<>();
                routePoints.add(new OsrmClient.Point(dto.getStart().getLat(), dto.getStart().getLng()));
                for (RideCheckpointDto cp : checkpoints) {
                    routePoints.add(new OsrmClient.Point(cp.getLat(), cp.getLng()));
                }
                routePoints.add(new OsrmClient.Point(dto.getDestination().getLat(), dto.getDestination().getLng()));

                OsrmClient.RouteWithGeometry routeForMap = osrm.routeDrivingWithGeometry(routePoints);
                dto.setRoute(
                        routeForMap.geometry().stream()
                                .map(p -> new LatLngDto(p.lat(), p.lon()))
                                .toList()
                );
                dto.setDistanceKm(Math.round((routeForMap.distanceMeters() / 1000.0) * 100.0) / 100.0);
            } catch (Exception e) {
                dto.setRoute(List.of());
                dto.setDistanceKm(0);
            }
        } else {
            dto.setRoute(List.of());
        }

        // reports (inconsistencies reported during the ride)
        List<RideReportDto> reports = jdbc.sql("""
            select description, created_at
            from ride_reports
            where ride_id = :rideId
            order by created_at asc
        """)
                .param("rideId", rideId)
                .query((rs2, rn) -> new RideReportDto(
                        rs2.getString("description"),
                        rs2.getTimestamp("created_at")
                ))
                .list();
        dto.setReports(reports);

        // rating (may not exist yet)
        RideRatingResponseDto rating = jdbc.sql("""
            select id, ride_id, driver_rating, vehicle_rating, comment, created_at
            from ride_ratings
            where ride_id = :rideId
        """)
                .param("rideId", rideId)
                .query((rs2, rn) -> {
                    RideRatingResponseDto r = new RideRatingResponseDto();
                    r.setId(rs2.getLong("id"));
                    r.setRideId(rs2.getLong("ride_id"));
                    r.setDriverRating(rs2.getInt("driver_rating"));
                    r.setVehicleRating(rs2.getInt("vehicle_rating"));
                    r.setComment(rs2.getString("comment"));
                    r.setCreatedAt(rs2.getObject("created_at", java.time.OffsetDateTime.class));
                    return r;
                })
                .optional()
                .orElse(null);
        dto.setRating(rating);

        return Optional.of(dto);
    }

    private DriverPublicInfoResponseDto fetchDriverPublicInfo(Long driverId) {
        return jdbc.sql("""
            select
                d.id as driver_id,
                u.first_name, u.last_name, u.profile_image_url,
                v.model, v.type, v.license_plate
            from drivers d
            join users u on u.id = d.user_id
            left join vehicles v on v.driver_id = d.id
            where d.id = :driverId
            order by v.id
            limit 1
        """)
                .param("driverId", driverId)
                .query((rs, rowNum) -> {
                    DriverPublicInfoResponseDto d = new DriverPublicInfoResponseDto();
                    d.setDriverId(rs.getLong("driver_id"));
                    d.setFirstName(rs.getString("first_name"));
                    d.setLastName(rs.getString("last_name"));
                    d.setProfileImageUrl(rs.getString("profile_image_url"));
                    d.setVehicleModel(rs.getString("model"));
                    d.setVehicleType(rs.getString("type"));
                    d.setLicensePlate(rs.getString("license_plate"));
                    return d;
                })
                .optional()
                .orElse(null);
    }
}
