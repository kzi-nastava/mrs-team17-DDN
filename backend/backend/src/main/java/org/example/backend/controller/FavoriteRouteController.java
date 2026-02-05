package org.example.backend.controller;

import org.example.backend.dto.response.AddFavoriteFromRideResponseDto;
import org.example.backend.dto.response.FavoriteRouteResponseDto;
import org.example.backend.service.FavoriteRouteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        long currentUserId = requirePassengerUserId();
        enforceSameUser(userId, currentUserId);

        return ResponseEntity.ok(favoriteRouteService.listFavorites(currentUserId));
    }

    @GetMapping("/{favoriteRouteId}")
    public ResponseEntity<FavoriteRouteResponseDto> get(
            @PathVariable Long userId,
            @PathVariable Long favoriteRouteId
    ) {
        long currentUserId = requirePassengerUserId();
        enforceSameUser(userId, currentUserId);

        FavoriteRouteResponseDto dto = favoriteRouteService.getFavorite(currentUserId, favoriteRouteId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/from-ride/{rideId}")
    public ResponseEntity<AddFavoriteFromRideResponseDto> addFromRide(
            @PathVariable Long userId,
            @PathVariable Long rideId
    ) {
        long currentUserId = requirePassengerUserId();
        enforceSameUser(userId, currentUserId);

        AddFavoriteFromRideResponseDto resp = favoriteRouteService.addFromRide(currentUserId, rideId);
        return ResponseEntity.status(201).body(resp);
    }

    @DeleteMapping("/{favoriteRouteId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long userId,
            @PathVariable Long favoriteRouteId
    ) {
        long currentUserId = requirePassengerUserId();
        enforceSameUser(userId, currentUserId);

        favoriteRouteService.deleteFavorite(currentUserId, favoriteRouteId);
        return ResponseEntity.noContent().build();
    }

    private void enforceSameUser(Long pathUserId, long currentUserId) {
        if (pathUserId == null || pathUserId.longValue() != currentUserId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can access only your own favorites.");
        }
    }

    private long requirePassengerUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        boolean isPassenger = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PASSENGER".equals(a.getAuthority()));

        if (!isPassenger) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only passengers can access this endpoint");
        }

        try {
            return Long.parseLong(auth.getPrincipal().toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }
    }
}
