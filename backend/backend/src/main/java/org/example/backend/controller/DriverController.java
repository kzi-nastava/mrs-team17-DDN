package org.example.backend.controller;

import org.example.backend.dto.response.DriverRideDetailsResponseDto;
import org.example.backend.dto.response.DriverRideHistoryResponseDto;
import org.example.backend.service.DriverRideService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    private final DriverRideService driverRideService;

    public DriverController(DriverRideService driverRideService) {
        this.driverRideService = driverRideService;
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

    private Long getCurrentDriverId() {
        return 1L;
    }
}
