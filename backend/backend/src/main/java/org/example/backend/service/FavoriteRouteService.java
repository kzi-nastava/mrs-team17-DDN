package org.example.backend.service;

import org.example.backend.dto.response.AddFavoriteFromRideResponseDto;
import org.example.backend.dto.response.FavoriteRoutePointResponseDto;
import org.example.backend.dto.response.FavoriteRouteResponseDto;
import org.example.backend.repository.FavoriteRouteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class FavoriteRouteService {

    private final FavoriteRouteRepository favoriteRepo;

    public FavoriteRouteService(FavoriteRouteRepository favoriteRepo) {
        this.favoriteRepo = favoriteRepo;
    }

    @Transactional(readOnly = true)
    public List<FavoriteRouteResponseDto> listFavorites(Long userId) {
        List<FavoriteRouteRepository.FavoriteRouteRow> routes = favoriteRepo.findAllByUserId(userId);

        List<FavoriteRouteResponseDto> result = new ArrayList<>();
        for (FavoriteRouteRepository.FavoriteRouteRow r : routes) {
            List<FavoriteRouteRepository.FavoriteStopRow> stops = favoriteRepo.findStopsByFavoriteRouteId(r.id());
            result.add(mapToDto(r, stops));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public FavoriteRouteResponseDto getFavorite(Long userId, Long favoriteRouteId) {
        FavoriteRouteRepository.FavoriteRouteRow route = favoriteRepo.findByIdAndUserId(favoriteRouteId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite route not found"));

        List<FavoriteRouteRepository.FavoriteStopRow> stops = favoriteRepo.findStopsByFavoriteRouteId(route.id());
        return mapToDto(route, stops);
    }

    @Transactional
    public AddFavoriteFromRideResponseDto addFromRide(Long userId, Long rideId) {

        if (!favoriteRepo.rideBelongsToUser(rideId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can add favorite only from your own ride.");
        }

        FavoriteRouteRepository.RideRouteRow ride = favoriteRepo.findRideRouteByRideId(rideId)
                .orElseThrow(() -> new IllegalArgumentException("Ride not found"));

        List<FavoriteRouteRepository.RideStopRow> rideStops = favoriteRepo.findRideStopsByRideId(rideId);

        String name = "Ride " + rideId;

        FavoriteRouteRepository.FavoriteRouteRow newFav = new FavoriteRouteRepository.FavoriteRouteRow(
                null,
                userId,
                name,
                ride.startAddress(),
                ride.startLat(),
                ride.startLng(),
                ride.destinationAddress(),
                ride.destLat(),
                ride.destLng(),
                null
        );

        Long favId = favoriteRepo.insertRouteReturningId(newFav);

        if (rideStops != null && !rideStops.isEmpty()) {
            List<FavoriteRouteRepository.FavoriteStopRow> favStops = new ArrayList<>();
            for (FavoriteRouteRepository.RideStopRow s : rideStops) {
                favStops.add(new FavoriteRouteRepository.FavoriteStopRow(
                        null,
                        favId,
                        s.stopOrder(),
                        s.address(),
                        s.lat(),
                        s.lng()
                ));
            }
            favoriteRepo.insertStops(favId, favStops);
        }

        AddFavoriteFromRideResponseDto resp = new AddFavoriteFromRideResponseDto();
        resp.setFavoriteRouteId(favId);
        resp.setStatus("CREATED");
        return resp;
    }

    @Transactional
    public void deleteFavorite(Long userId, Long favoriteRouteId) {
        boolean deleted = favoriteRepo.deleteByIdAndUserId(favoriteRouteId, userId);
        if (!deleted) {
            throw new IllegalArgumentException("Favorite route not found");
        }
    }

    private FavoriteRouteResponseDto mapToDto(
            FavoriteRouteRepository.FavoriteRouteRow r,
            List<FavoriteRouteRepository.FavoriteStopRow> stops
    ) {
        FavoriteRouteResponseDto dto = new FavoriteRouteResponseDto();
        dto.setId(r.id());
        dto.setName(r.name());

        dto.setStart(new FavoriteRoutePointResponseDto(
                r.startAddress(),
                r.startLat(),
                r.startLng()
        ));

        dto.setDestination(new FavoriteRoutePointResponseDto(
                r.destinationAddress(),
                r.destLat(),
                r.destLng()
        ));

        List<FavoriteRoutePointResponseDto> stopDtos = new ArrayList<>();
        if (stops != null) {
            for (FavoriteRouteRepository.FavoriteStopRow s : stops) {
                stopDtos.add(new FavoriteRoutePointResponseDto(
                        s.address(),
                        s.lat(),
                        s.lng()
                ));
            }
        }
        dto.setStops(stopDtos);

        return dto;
    }
}
