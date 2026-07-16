package org.example.backend.repository;

import org.example.backend.dto.response.*;
import org.example.backend.osrm.OsrmClient;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcAdminRideHistoryRepository implements AdminRideHistoryRepository {

    // whitelist: public sort key -> real SQL column, to avoid SQL injection via ?sort=
    private static final Map<String, String> SORTABLE_COLUMNS = Map.ofEntries(
            Map.entry("rideId", "r.id"),
            Map.entry("startDate", "r.started_at"),
            Map.entry("startedAt", "r.started_at"),
            Map.entry("endDate", "r.ended_at"),
            Map.entry("endedAt", "r.ended_at"),
            Map.entry("startAddress", "r.start_address"),
            Map.entry("route", "r.start_address"),
            Map.entry("destinationAddress", "r.destination_address"),
            Map.entry("status", "r.status"),
            Map.entry("canceled", "r.canceled"),
            Map.entry("canceledBy", "r.canceled_by"),
            Map.entry("price", "r.price"),
            Map.entry("panicActivated", "r.panic_triggered"),
            Map.entry("panic", "r.panic_triggered")
    );

    private final JdbcClient jdbc;
    private final OsrmClient osrm;

    public JdbcAdminRideHistoryRepository(JdbcClient jdbc, OsrmClient osrm) {
        this.jdbc = jdbc;
        this.osrm = osrm;
    }

    @Override
    public List<AdminRideHistoryItemDto> listAll(LocalDate from, LocalDate to, String sort, String dir) {
        String sql = """
            select
                r.id as ride_id,
                r.start_address,
                r.destination_address,
                r.started_at,
                r.ended_at,
                r.status,
                r.canceled,
                r.canceled_by,
                r.price,
                r.panic_triggered
            from rides r
            where (cast(:from as date) is null or r.started_at::date >= cast(:from as date))
              and (cast(:to   as date) is null or r.started_at::date <= cast(:to   as date))
            order by %s
        """.formatted(orderByClause(sort, dir));

        return jdbc.sql(sql)
                .param("from", from)
                .param("to", to)
                .query(this::mapHistoryRow)
                .list();
    }

    @Override
    public List<AdminRideHistoryItemDto> listByDriver(long driverUserId, LocalDate from, LocalDate to, String sort, String dir) {
        String sql = """
            select
                r.id as ride_id,
                r.start_address,
                r.destination_address,
                r.started_at,
                r.ended_at,
                r.status,
                r.canceled,
                r.canceled_by,
                r.price,
                r.panic_triggered
            from rides r
            join drivers d on d.id = r.driver_id
            where d.user_id = :driverUserId
              and (cast(:from as date) is null or r.started_at::date >= cast(:from as date))
              and (cast(:to   as date) is null or r.started_at::date <= cast(:to   as date))
            order by %s
        """.formatted(orderByClause(sort, dir));

        return jdbc.sql(sql)
                .param("driverUserId", driverUserId)
                .param("from", from)
                .param("to", to)
                .query(this::mapHistoryRow)
                .list();
    }

    @Override
    public List<AdminRideHistoryItemDto> listByPassenger(long passengerUserId, LocalDate from, LocalDate to, String sort, String dir) {
        String sql = """
            select
                r.id as ride_id,
                r.start_address,
                r.destination_address,
                r.started_at,
                r.ended_at,
                r.status,
                r.canceled,
                r.canceled_by,
                r.price,
                r.panic_triggered
            from rides r
            join ride_passengers rp on rp.ride_id = r.id
            join users u on lower(u.email) = lower(rp.email)
            where u.id = :passengerUserId
              and (cast(:from as date) is null or r.started_at::date >= cast(:from as date))
              and (cast(:to   as date) is null or r.started_at::date <= cast(:to   as date))
            order by %s
        """.formatted(orderByClause(sort, dir));

        return jdbc.sql(sql)
                .param("passengerUserId", passengerUserId)
                .param("from", from)
                .param("to", to)
                .query(this::mapHistoryRow)
                .list();
    }

    @Override
    public Optional<AdminRideDetailsResponseDto> findDetails(long rideId) {
        String sql = """
            select
                r.id, r.status, r.started_at, r.ended_at,
                r.start_address, r.destination_address,
                r.start_lat, r.start_lng, r.dest_lat, r.dest_lng,
                r.driver_id, r.vehicle_type, r.baby_transport, r.pet_transport,
                r.canceled, r.canceled_by, r.cancel_reason,
                r.price, r.panic_triggered
            from rides r
            where r.id = :rideId
        """;

        Optional<AdminRideDetailsResponseDto> base = jdbc.sql(sql)
                .param("rideId", rideId)
                .query((rs, rowNum) -> {
                    AdminRideDetailsResponseDto dto = new AdminRideDetailsResponseDto();
                    dto.setRideId(rs.getLong("id"));
                    dto.setStatus(rs.getString("status"));
                    dto.setStartDate(rs.getObject("started_at", OffsetDateTime.class));
                    dto.setEndDate(rs.getObject("ended_at", OffsetDateTime.class));
                    dto.setStartAddress(rs.getString("start_address"));
                    dto.setDestinationAddress(rs.getString("destination_address"));
                    dto.setVehicleType(rs.getString("vehicle_type"));
                    dto.setBabyTransport(rs.getBoolean("baby_transport"));
                    dto.setPetTransport(rs.getBoolean("pet_transport"));
                    dto.setCanceled(rs.getBoolean("canceled"));
                    dto.setCanceledBy(rs.getString("canceled_by"));
                    dto.setCancelReason(rs.getString("cancel_reason"));
                    dto.setPrice(rs.getDouble("price"));
                    dto.setPanicActivated(rs.getBoolean("panic_triggered"));

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

        AdminRideDetailsResponseDto dto = base.get();

        // checkpoints
        List<RideCheckpointDto> checkpoints = jdbc.sql("""
            select stop_order, address, lat, lng
            from ride_stops
            where ride_id = :rideId
            order by stop_order asc
        """)
                .param("rideId", rideId)
                .query((rs, rn) -> new RideCheckpointDto(
                        rs.getInt("stop_order"),
                        rs.getString("address"),
                        rs.getDouble("lat"),
                        rs.getDouble("lng")
                ))
                .list();
        dto.setStops(checkpoints);

        // road-following route for the map (best-effort)
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

        // passengers
        List<PassengerInfoResponseDto> passengers = jdbc.sql("""
            select name, email
            from ride_passengers
            where ride_id = :rideId
        """)
                .param("rideId", rideId)
                .query((rs, rn) -> new PassengerInfoResponseDto(
                        rs.getString("name"),
                        rs.getString("email")
                ))
                .list();
        dto.setPassengers(passengers);

        // reports of inconsistency
        List<RideReportResponseDto> reports = jdbc.sql("""
            select id, ride_id, description, created_at
            from ride_reports
            where ride_id = :rideId
            order by created_at desc
        """)
                .param("rideId", rideId)
                .query((rs, rn) -> {
                    RideReportResponseDto r = new RideReportResponseDto();
                    r.setId(rs.getLong("id"));
                    r.setRideId(rs.getLong("ride_id"));
                    r.setDescription(rs.getString("description"));
                    r.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    return r;
                })
                .list();
        dto.setReports(reports);

        // rating (may not exist)
        RideRatingResponseDto rating = jdbc.sql("""
            select id, ride_id, driver_rating, vehicle_rating, comment, created_at
            from ride_ratings
            where ride_id = :rideId
        """)
                .param("rideId", rideId)
                .query((rs, rn) -> {
                    RideRatingResponseDto r = new RideRatingResponseDto();
                    r.setId(rs.getLong("id"));
                    r.setRideId(rs.getLong("ride_id"));
                    r.setDriverRating(rs.getInt("driver_rating"));
                    r.setVehicleRating(rs.getInt("vehicle_rating"));
                    r.setComment(rs.getString("comment"));
                    r.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
                    return r;
                })
                .optional()
                .orElse(null);
        dto.setRating(rating);

        return Optional.of(dto);
    }

    @Override
    public Optional<ReorderSource> findReorderSource(long rideId) {
        String sql = """
            select
                r.start_address, r.start_lat, r.start_lng,
                r.destination_address, r.dest_lat, r.dest_lng,
                r.vehicle_type, r.baby_transport, r.pet_transport,
                u.id as passenger_user_id
            from rides r
            left join ride_passengers rp on rp.ride_id = r.id
            left join users u on lower(u.email) = lower(rp.email)
            where r.id = :rideId
            limit 1
        """;

        Optional<ReorderSource> base = jdbc.sql(sql)
                .param("rideId", rideId)
                .query((rs, rowNum) -> new ReorderSource(
                        (Long) rs.getObject("passenger_user_id"),
                        rs.getString("start_address"), rs.getDouble("start_lat"), rs.getDouble("start_lng"),
                        rs.getString("destination_address"), rs.getDouble("dest_lat"), rs.getDouble("dest_lng"),
                        List.of(),
                        rs.getString("vehicle_type"),
                        rs.getBoolean("baby_transport"),
                        rs.getBoolean("pet_transport")
                ))
                .optional();

        if (base.isEmpty()) return Optional.empty();

        List<ReorderStop> stops = jdbc.sql("""
            select address, lat, lng
            from ride_stops
            where ride_id = :rideId
            order by stop_order asc
        """)
                .param("rideId", rideId)
                .query((rs, rn) -> new ReorderStop(
                        rs.getString("address"),
                        rs.getDouble("lat"),
                        rs.getDouble("lng")
                ))
                .list();

        ReorderSource withStops = new ReorderSource(
                base.get().passengerUserId(),
                base.get().startAddress(), base.get().startLat(), base.get().startLng(),
                base.get().destinationAddress(), base.get().destLat(), base.get().destLng(),
                stops,
                base.get().vehicleType(),
                base.get().babyTransport(),
                base.get().petTransport()
        );

        return Optional.of(withStops);
    }

    private AdminRideHistoryItemDto mapHistoryRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        AdminRideHistoryItemDto dto = new AdminRideHistoryItemDto();
        dto.setRideId(rs.getLong("ride_id"));
        String startAddress = rs.getString("start_address");
        String destinationAddress = rs.getString("destination_address");
        dto.setStartAddress(startAddress);
        dto.setDestinationAddress(destinationAddress);
        dto.setRoute(((startAddress != null ? startAddress : "?") + "  ⟶  " + (destinationAddress != null ? destinationAddress : "?")));
        dto.setStartDate(rs.getObject("started_at", OffsetDateTime.class));
        dto.setEndDate(rs.getObject("ended_at", OffsetDateTime.class));
        dto.setStatus(rs.getString("status"));
        dto.setCanceled(rs.getBoolean("canceled"));
        dto.setCanceledBy(rs.getString("canceled_by"));
        dto.setPrice(rs.getDouble("price"));
        dto.setPanicActivated(rs.getBoolean("panic_triggered"));
        return dto;
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

    private String orderByClause(String sort, String dir) {
        String column = SORTABLE_COLUMNS.getOrDefault(sort, "r.started_at");
        String direction = "asc".equalsIgnoreCase(dir) ? "asc" : "desc";
        // secondary sort by id keeps paging/order stable when the primary column ties
        return column + " " + direction + " nulls last, r.id " + direction;
    }
}
