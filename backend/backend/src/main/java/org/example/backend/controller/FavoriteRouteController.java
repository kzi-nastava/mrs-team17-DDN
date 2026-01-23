package org.example.backend.controller;

import org.example.backend.dto.response.AddFavoriteFromRideResponseDto;
import org.example.backend.dto.response.FavoriteRouteResponseDto;
import org.example.backend.service.FavoriteRouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/favorite-routes")
public class FavoriteRouteController {

    private final FavoriteRouteService favoriteRouteService;

    public FavoriteRouteController(FavoriteRouteService favoriteRouteService) {
        this.favoriteRouteService = favoriteRouteService;
    }

    @GetMapping
    public ResponseEntity<List<FavoriteRouteResponseDto>> list(@PathVariable Long userId) {
        return ResponseEntity.ok(favoriteRouteService.listFavorites(userId));
    }

    @PostMapping("/from-ride/{rideId}")
    public ResponseEntity<AddFavoriteFromRideResponseDto> addFromRide(
            @PathVariable Long userId,
            @PathVariable Long rideId
    ) {
        AddFavoriteFromRideResponseDto resp = favoriteRouteService.addFromRide(userId, rideId);
        return ResponseEntity.status(201).body(resp);
    }

    @DeleteMapping("/{favoriteRouteId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long userId,
            @PathVariable Long favoriteRouteId
    ) {
        favoriteRouteService.deleteFavorite(userId, favoriteRouteId);
        return ResponseEntity.noContent().build();
    }
}
