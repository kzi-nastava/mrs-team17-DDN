package org.example.backend.controller;

import org.example.backend.dto.response.DriverRideDetailsResponseDto;
import org.example.backend.dto.response.DriverRideHistoryResponseDto;
import org.example.backend.repository.DriverRepository;
import org.example.backend.service.DriverRideService;
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
@RequestMapping("/api/driver")
public class DriverController {

    private final DriverRideService driverRideService;
    private final DriverRepository drivers;

    public DriverController(DriverRideService driverRideService, DriverRepository drivers) {
        this.driverRideService = driverRideService;
        this.drivers = drivers;
    }

    @GetMapping("/rides")
    public ResponseEntity<List<DriverRideHistoryResponseDto>> getDriverRides(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(driverRideService.getDriverRides(getCurrentDriverId(), from, to));
    }

    @GetMapping("/rides/{rideId}")
    public ResponseEntity<DriverRideDetailsResponseDto> getDriverRideDetails(@PathVariable Long rideId) {
        return ResponseEntity.ok(driverRideService.getDriverRideDetails(getCurrentDriverId(), rideId));
    }

    @GetMapping("/active-ride")
    public ResponseEntity<DriverRideDetailsResponseDto> getActiveRide() {
        return ResponseEntity.ok(driverRideService.getActiveRide(getCurrentDriverId()));
    }

    @GetMapping("/rides/accepted")
    public ResponseEntity<List<DriverRideDetailsResponseDto>> getAcceptedRides() {
        return ResponseEntity.ok(driverRideService.getAcceptedRides(getCurrentDriverId()));
    }

    @GetMapping("/rides/upcoming")
    public ResponseEntity<List<DriverRideDetailsResponseDto>> getUpcomingRides() {
        return ResponseEntity.ok(driverRideService.getUpcomingRides(getCurrentDriverId()));
    }

    @PutMapping("/rides/{rideId}/start")
    public ResponseEntity<Void> startRide(@PathVariable Long rideId) {
        driverRideService.startRide(getCurrentDriverId(), rideId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/rides/{rideId}/finish")
    public ResponseEntity<Void> finishRide(@PathVariable Long rideId) {
        driverRideService.finishRide(getCurrentDriverId(), rideId);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentDriverId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        boolean isDriver = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_DRIVER".equals(a.getAuthority()));

        if (!isDriver) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only drivers can access this endpoint");
        }

        long userId;
        try {
            userId = Long.parseLong(auth.getPrincipal().toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }

        return drivers.findDriverIdByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Driver profile not found"));
    }
}
