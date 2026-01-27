// backend/src/main/java/org/example/backend/controller/RideController.java
package org.example.backend.controller;

import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.request.RideRatingRequestDto;
import org.example.backend.dto.response.RideReportResponseDto;
import org.example.backend.dto.response.RideRatingResponseDto;
import org.example.backend.dto.response.RideTrackingResponseDto;
import org.example.backend.service.RideRatingService;
import org.example.backend.service.RideService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;
    private final RideRatingService rideRatingService;

    public RideController(RideService rideService, RideRatingService rideRatingService) {
        this.rideService = rideService;
        this.rideRatingService = rideRatingService;
    }

    @GetMapping("/{rideId}/tracking")
    public ResponseEntity<RideTrackingResponseDto> getRideTracking(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.getRideTracking(rideId));
    }

    @PostMapping("/{rideId}/reports")
    public ResponseEntity<RideReportResponseDto> reportRideIssue(
            @PathVariable Long rideId,
            @RequestBody RideReportRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rideService.reportRideIssue(rideId, request));
    }

    @PutMapping("/{rideId}/finish")
    public ResponseEntity<Void> finishRide(@PathVariable Long rideId) {
        rideService.finishRide(rideId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{rideId}/simulate-step")
    public ResponseEntity<Void> simulateStep(@PathVariable Long rideId) {
        rideService.simulateVehicleStep(rideId);
        return ResponseEntity.ok().build();
    }

    // --- RATING (2.8) ---

    @GetMapping("/{rideId}/rating")
    public ResponseEntity<RideRatingResponseDto> getRating(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideRatingService.getRating(rideId));
    }

    @PostMapping("/{rideId}/rating")
    public ResponseEntity<RideRatingResponseDto> submitRating(
            @PathVariable Long rideId,
            @RequestBody RideRatingRequestDto request
    ) {
        RideRatingResponseDto res = rideRatingService.submitRating(rideId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PutMapping("/{rideId}/start")
    public ResponseEntity<Void> startRide(@PathVariable Long rideId) {
        rideService.startRide(rideId);
        return ResponseEntity.ok().build();
    }

}
