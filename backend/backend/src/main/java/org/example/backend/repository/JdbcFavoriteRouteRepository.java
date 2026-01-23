package org.example.backend.repository;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcFavoriteRouteRepository implements FavoriteRouteRepository {

    private final JdbcClient jdbc;

    public JdbcFavoriteRouteRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<FavoriteRouteRow> findAllByUserId(Long userId) {
        return jdbc.sql("""
            select
                id, user_id, name,
                start_address, start_lat, start_lng,
                destination_address, dest_lat, dest_lng,
                created_at
            from favorite_routes
            where user_id = :userId
            order by created_at desc
        """)
                .param("userId", userId)
                .query((rs, rowNum) -> new FavoriteRouteRow(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("name"),
                        rs.getString("start_address"),
                        rs.getDouble("start_lat"),
                        rs.getDouble("start_lng"),
                        rs.getString("destination_address"),
                        rs.getDouble("dest_lat"),
                        rs.getDouble("dest_lng"),
                        rs.getObject("created_at", OffsetDateTime.class)
                ))
                .list();
    }

    @Override
    public Optional<FavoriteRouteRow> findByIdAndUserId(Long favoriteRouteId, Long userId) {
        return jdbc.sql("""
            select
                id, user_id, name,
                start_address, start_lat, start_lng,
                destination_address, dest_lat, dest_lng,
                created_at
            from favorite_routes
            where id = :id and user_id = :userId
        """)
                .param("id", favoriteRouteId)
                .param("userId", userId)
                .query((rs, rowNum) -> new FavoriteRouteRow(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("name"),
                        rs.getString("start_address"),
                        rs.getDouble("start_lat"),
                        rs.getDouble("start_lng"),
                        rs.getString("destination_address"),
                        rs.getDouble("dest_lat"),
                        rs.getDouble("dest_lng"),
                        rs.getObject("created_at", OffsetDateTime.class)
                ))
                .optional();
    }

    @Override
    public List<FavoriteStopRow> findStopsByFavoriteRouteId(Long favoriteRouteId) {
        return jdbc.sql("""
            select id, favorite_route_id, stop_order, address, lat, lng
            from favorite_route_stops
            where favorite_route_id = :rid
            order by stop_order asc
        """)
                .param("rid", favoriteRouteId)
                .query((rs, rowNum) -> new FavoriteStopRow(
                        rs.getLong("id"),
                        rs.getLong("favorite_route_id"),
                        rs.getInt("stop_order"),
                        rs.getString("address"),
                        rs.getDouble("lat"),
                        rs.getDouble("lng")
                ))
                .list();
    }

    @Override
    public Long insertRouteReturningId(FavoriteRouteRow r) {
        return jdbc.sql("""
            insert into favorite_routes (
                user_id, name,
                start_address, start_lat, start_lng,
                destination_address, dest_lat, dest_lng
            )
            values (
                :userId, :name,
                :startAddr, :startLat, :startLng,
                :destAddr, :destLat, :destLng
            )
            returning id
        """)
                .param("userId", r.userId())
                .param("name", r.name())
                .param("startAddr", r.startAddress())
                .param("startLat", r.startLat())
                .param("startLng", r.startLng())
                .param("destAddr", r.destinationAddress())
                .param("destLat", r.destLat())
                .param("destLng", r.destLng())
                .query(Long.class)
                .single();
    }

    @Override
    public void insertStops(Long favoriteRouteId, List<FavoriteStopRow> stops) {
        if (stops == null || stops.isEmpty()) return;

        for (FavoriteStopRow s : stops) {
            jdbc.sql("""
                insert into favorite_route_stops (favorite_route_id, stop_order, address, lat, lng)
                values (:rid, :ord, :addr, :lat, :lng)
            """)
                    .param("rid", favoriteRouteId)
                    .param("ord", s.stopOrder())
                    .param("addr", s.address())
                    .param("lat", s.lat())
                    .param("lng", s.lng())
                    .update();
        }
    }

    @Override
    public boolean deleteByIdAndUserId(Long favoriteRouteId, Long userId) {
        int updated = jdbc.sql("""
            delete from favorite_routes
            where id = :id and user_id = :userId
        """)
                .param("id", favoriteRouteId)
                .param("userId", userId)
                .update();

        return updated > 0;
    }

    @Override
    public Optional<RideRouteRow> findRideRouteByRideId(Long rideId) {
        return jdbc.sql("""
            select
                id as ride_id,
                start_address, start_lat, start_lng,
                destination_address, dest_lat, dest_lng
            from rides
            where id = :rideId
        """)
                .param("rideId", rideId)
                .query((rs, rowNum) -> new RideRouteRow(
                        rs.getLong("ride_id"),
                        rs.getString("start_address"),
                        rs.getDouble("start_lat"),
                        rs.getDouble("start_lng"),
                        rs.getString("destination_address"),
                        rs.getDouble("dest_lat"),
                        rs.getDouble("dest_lng")
                ))
                .optional();
    }

    @Override
    public List<RideStopRow> findRideStopsByRideId(Long rideId) {
        return jdbc.sql("""
            select stop_order, address, lat, lng
            from ride_stops
            where ride_id = :rideId
            order by stop_order asc
        """)
                .param("rideId", rideId)
                .query((rs, rowNum) -> new RideStopRow(
                        rs.getInt("stop_order"),
                        rs.getString("address"),
                        rs.getDouble("lat"),
                        rs.getDouble("lng")
                ))
                .list();
    }
}
