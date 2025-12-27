package org.example.backend.controller;

import org.example.backend.dto.request.RideRatingRequestDto;
import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.response.RideTrackingResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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



}
