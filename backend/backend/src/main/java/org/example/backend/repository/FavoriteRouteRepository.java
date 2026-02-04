package org.example.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface FavoriteRouteRepository {

    record FavoriteRouteRow(
            Long id,
            Long userId,
            String name,
            String startAddress,
            double startLat,
            double startLng,
            String destinationAddress,
            double destLat,
            double destLng,
            OffsetDateTime createdAt
    ) {}

    record FavoriteStopRow(
            Long id,
            Long favoriteRouteId,
            int stopOrder,
            String address,
            double lat,
            double lng
    ) {}

    List<FavoriteRouteRow> findAllByUserId(Long userId);

    Optional<FavoriteRouteRow> findByIdAndUserId(Long favoriteRouteId, Long userId);

    List<FavoriteStopRow> findStopsByFavoriteRouteId(Long favoriteRouteId);

    Long insertRouteReturningId(FavoriteRouteRow route);

    void insertStops(Long favoriteRouteId, List<FavoriteStopRow> stops);

    boolean deleteByIdAndUserId(Long favoriteRouteId, Long userId);

    Optional<RideRouteRow> findRideRouteByRideId(Long rideId);

    List<RideStopRow> findRideStopsByRideId(Long rideId);

    boolean rideBelongsToUser(Long rideId, Long userId);

    record RideRouteRow(
            Long rideId,
            String startAddress,
            double startLat,
            double startLng,
            String destinationAddress,
            double destLat,
            double destLng
    ) {}

    record RideStopRow(
            int stopOrder,
            String address,
            double lat,
            double lng
    ) {}
}
