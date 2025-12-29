package org.example.backend.controller;

import org.example.backend.dto.response.DriverRideDetailsResponseDto;
import org.example.backend.dto.response.DriverRideHistoryResponseDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    @GetMapping("/{driverId}/rides")
    public ResponseEntity<List<DriverRideHistoryResponseDto>> getDriverRides(
            @PathVariable Long driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{driverId}/rides/{rideId}")
    public ResponseEntity<DriverRideDetailsResponseDto> getDriverRideDetails(
            @PathVariable Long driverId,
            @PathVariable Long rideId
    ) {
        DriverRideDetailsResponseDto dto = new DriverRideDetailsResponseDto();

        dto.setRideId(rideId);
        return ResponseEntity.ok(dto);
    }
}
