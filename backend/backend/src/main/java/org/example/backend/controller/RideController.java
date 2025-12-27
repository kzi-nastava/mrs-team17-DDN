package org.example.backend.controller;

import org.example.backend.dto.request.RideRatingRequestDto;
import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.response.RideTrackingResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.example.backend.dto.request.RideOrderRequestDto;
import org.example.backend.dto.response.RideOrderResponseDto;
import java.util.concurrent.ThreadLocalRandom;
import org.example.backend.dto.response.RideDetailsResponseDto;
import java.util.List;
import org.example.backend.dto.request.OrderRideFromFavoriteRouteRequestDto;
import org.example.backend.dto.request.StartRideRequestDto;
import org.example.backend.dto.response.RideStartResponseDto;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @GetMapping("/{rideId}/tracking")
    public ResponseEntity<RideTrackingResponseDto> getRideTracking(
            @PathVariable Long rideId) {

        return ResponseEntity.ok(new RideTrackingResponseDto());
    }

    @PostMapping("/{rideId}/reports")
    public ResponseEntity<Void> reportRideIssue(
            @PathVariable Long rideId,
            @RequestBody RideReportRequestDto request) {

        return ResponseEntity.status(201).build();
    }
    @PutMapping("/{rideId}/finish")
    public ResponseEntity<Void> finishRide(@PathVariable Long rideId) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{rideId}/rating")
    public ResponseEntity<Void> rateRide(
            @PathVariable Long rideId,
            @RequestBody RideRatingRequestDto request) {

        return ResponseEntity.status(201).build();
    }

    @PostMapping
    public ResponseEntity<RideOrderResponseDto> orderRide(@Valid @RequestBody RideOrderRequestDto request) {

        long rideId = ThreadLocalRandom.current().nextLong(1, 1_000_000);

        RideOrderResponseDto response = new RideOrderResponseDto(
                rideId,
                request.getScheduledFor() == null ? "ACCEPTED" : "SCHEDULED",
                0.0,
                request.getScheduledFor(),
                "Ride request received."
        );
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideDetailsResponseDto> getRideDetails(@PathVariable Long rideId) {

        RideDetailsResponseDto dto = new RideDetailsResponseDto();
        dto.setRideId(rideId);
        dto.setStatus("REQUESTED");
        dto.setStartAddress("Bulevar Oslobodjenja 1, Novi Sad");
        dto.setDestinationAddress("Trg Slobode 1, Novi Sad");
        dto.setStops(List.of("Zmaj Jovina 10, Novi Sad", "Dunavska 5, Novi Sad"));
        dto.setPassengerEmails(List.of("putnik1@mail.com", "putnik2@mail.com"));

        dto.setVehicleType("STANDARD");
        dto.setBabyTransport(true);
        dto.setPetTransport(false);

        dto.setPrice(0.0);
        dto.setScheduledFor(null);
        dto.setDriverId(null);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/from-favorites/{favoriteRouteId}")
    public ResponseEntity<RideOrderResponseDto> orderRideFromFavoriteRoute(@PathVariable Long favoriteRouteId, @Valid @RequestBody OrderRideFromFavoriteRouteRequestDto request) {

        long rideId = ThreadLocalRandom.current().nextLong(1, 1_000_000);

        RideOrderResponseDto response = new RideOrderResponseDto();
        response.setRideId(rideId);
        response.setStatus(request.getScheduledFor() == null ? "ACCEPTED" : "SCHEDULED");
        response.setPrice(0.0);
        response.setScheduledFor(request.getScheduledFor());
        response.setMessage("Ride created from favorite route " + favoriteRouteId);

        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/{rideId}/start")
    public ResponseEntity<RideStartResponseDto> startRide(@PathVariable Long rideId,@Valid @RequestBody StartRideRequestDto request) {

        LocalDateTime startedAt = (request.getStartedAt() != null) ? request.getStartedAt() : LocalDateTime.now();

        RideStartResponseDto response = new RideStartResponseDto();
        response.setRideId(rideId);
        response.setStatus("IN_PROGRESS");
        response.setStartedAt(startedAt);
        response.setMessage("Ride started by driverId=" + request.getDriverId());

        return ResponseEntity.ok(response);
    }
}
