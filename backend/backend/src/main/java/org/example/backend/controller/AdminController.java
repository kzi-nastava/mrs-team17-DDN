package org.example.backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.example.backend.dto.response.AdminRideDetailsResponseDto;
import org.example.backend.dto.response.AdminRideHistoryItemDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/rides")
    public ResponseEntity<List<AdminRideHistoryItemDto>> getAllRides(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/rides/{rideId}")
    public ResponseEntity<AdminRideDetailsResponseDto> getRideDetails(@PathVariable Long rideId) {
        return ResponseEntity.ok(new AdminRideDetailsResponseDto());
    }

    @GetMapping("/users/{userId}/rides")
    public ResponseEntity<List<AdminRideHistoryItemDto>> getUserRides(@PathVariable Long userId) {
        return ResponseEntity.ok(List.of());
    }
}
