package org.example.backend.controller;

import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.request.RideRatingRequestDto;
import org.example.backend.dto.response.PassengerRideHistoryResponseDto;
import org.example.backend.dto.response.RideReportResponseDto;
import org.example.backend.dto.response.RideRatingResponseDto;
import org.example.backend.dto.response.RideTrackingResponseDto;
import org.example.backend.repository.DriverRepository;
import org.example.backend.service.DriverRideService;
import org.example.backend.service.PassengerRideHistoryService;
import org.example.backend.service.RideRatingService;
import org.example.backend.service.RideService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;
    private final RideRatingService rideRatingService;
    private final PassengerRideHistoryService passengerRideHistoryService;
    private final DriverRideService driverRideService;
    private final DriverRepository driverRepository;

    public RideController(
            RideService rideService,
            RideRatingService rideRatingService,
            PassengerRideHistoryService passengerRideHistoryService,
            DriverRideService driverRideService,
            DriverRepository driverRepository
    ) {
        this.rideService = rideService;
        this.rideRatingService = rideRatingService;
        this.passengerRideHistoryService = passengerRideHistoryService;
        this.driverRideService = driverRideService;
        this.driverRepository = driverRepository;
    }

    // -------------------------
    // NEW: "my active ride" APIs
    // -------------------------

    @GetMapping("/active/tracking")
    public ResponseEntity<RideTrackingResponseDto> getMyActiveRideTracking() {
        long userId = requirePassengerUserId();
        Long rideId = rideService.getActiveRideIdForPassenger(userId);
        var rideTracking = rideService.getRideTracking(rideId);
        return ResponseEntity.ok(rideTracking);
    }

    @PostMapping("/active/reports")
    public ResponseEntity<RideReportResponseDto> reportIssueForMyActiveRide(
            @RequestBody RideReportRequestDto request
    ) {
        long userId = requirePassengerUserId();
        Long rideId = rideService.getActiveRideIdForPassenger(userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rideService.reportRideIssue(rideId, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PassengerRideHistoryResponseDto>> getMyRideHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        long userId = requirePassengerUserId();
        return ResponseEntity.ok(passengerRideHistoryService.getMyRideHistory(userId, from, to));
    }

    // -------------------------
    // Existing APIs (keep as-is)
    // -------------------------

    @GetMapping("/{rideId}/tracking")
    public ResponseEntity<RideTrackingResponseDto> getRideTracking(@PathVariable Long rideId) {
        Authentication auth = requireAuthentication();
        long userId = parseAuthenticatedUserId(auth);
        if (!hasRole(auth, "ROLE_ADMIN")) {
            rideService.ensureUserCanAccessRideTracking(userId, rideId);
        }
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
        long driverId = requireDriverId();
        driverRideService.finishRide(driverId, rideId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rate/pending")
    public ResponseEntity<java.util.Map<String, Long>> getPendingRatingRide() {
        long userId = requirePassengerUserId();
        Long rideId = rideService.getRideIdToRateForPassenger(userId);
        return ResponseEntity.ok(java.util.Map.of("rideId", rideId));
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
        long driverId = requireDriverId();
        driverRideService.startRide(driverId, rideId);
        return ResponseEntity.ok().build();
    }

    private long requirePassengerUserId() {
        Authentication auth = requireAuthentication();
        if (!hasRole(auth, "ROLE_PASSENGER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only passengers can access this endpoint");
        }
        return parseAuthenticatedUserId(auth);
    }

    private long requireDriverId() {
        Authentication auth = requireAuthentication();
        if (!hasRole(auth, "ROLE_DRIVER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only drivers can access this endpoint");
        }

        long userId = parseAuthenticatedUserId(auth);

        return driverRepository.findDriverIdByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Driver profile not found"));
    }

    private Authentication requireAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return auth;
    }

    private long parseAuthenticatedUserId(Authentication auth) {
        try {
            return Long.parseLong(auth.getPrincipal().toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }
}
