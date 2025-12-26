package org.example.backend.controller;

import org.example.backend.dto.response.DriverRideHistoryResponseDto;
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
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {

        return ResponseEntity.ok(List.of());
    }
}
