package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.request.AddFavoriteRouteRequestDto;
import org.example.backend.dto.response.FavoriteRouteResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/favorite-routes")
public class FavoriteRouteController {

    @GetMapping
    public ResponseEntity<List<FavoriteRouteResponseDto>> listFavorites(@PathVariable Long userId) {

        FavoriteRouteResponseDto r1 = new FavoriteRouteResponseDto();
        r1.setId(1L);
        r1.setStartAddress("Bulevar Oslobodjenja 1, Novi Sad");
        r1.setDestinationAddress("Trg Slobode 1, Novi Sad");
        r1.setStops(List.of("Zmaj Jovina 10, Novi Sad", "Dunavska 5, Novi Sad"));

        return ResponseEntity.ok(List.of(r1));
    }

    @PostMapping
    public ResponseEntity<FavoriteRouteResponseDto> addFavoriteFromRide(@PathVariable Long userId, @Valid @RequestBody AddFavoriteRouteRequestDto request) {

        FavoriteRouteResponseDto created = new FavoriteRouteResponseDto();
        created.setId(1L);
        created.setStartAddress("Stub start (from rideId=" + request.getRideId() + ")");
        created.setDestinationAddress("Stub destination");
        created.setStops(List.of());

        return ResponseEntity.status(201).body(created);
    }

    @DeleteMapping("/{favoriteRouteId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long userId, @PathVariable Long favoriteRouteId) {

        return ResponseEntity.noContent().build();
    }
}
