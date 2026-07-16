package org.example.backend.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import org.example.backend.dto.request.AdminReorderRideRequestDto;
import org.example.backend.dto.response.AdminRideDetailsResponseDto;
import org.example.backend.dto.response.AdminRideHistoryItemDto;
import org.example.backend.dto.response.CreateRideResponseDto;
import org.example.backend.service.AdminRideHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminRideHistoryService rideHistoryService;

    public AdminController(AdminRideHistoryService rideHistoryService) {
        this.rideHistoryService = rideHistoryService;
    }

    /** Full ride history, any driver or passenger. Sorted newest-to-oldest by default. */
    @GetMapping("/rides")
    public ResponseEntity<List<AdminRideHistoryItemDto>> getAllRides(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir) {
        return ResponseEntity.ok(rideHistoryService.getAllRides(from, to, sort, dir));
    }

    /** Detailed view of a single ride: route, driver, passengers, reports, rating. */
    @GetMapping("/rides/{rideId}")
    public ResponseEntity<AdminRideDetailsResponseDto> getRideDetails(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideHistoryService.getRideDetails(rideId));
    }

    /** History of rides for one driver (driverId = users.id, same convention as /admin/users). */
    @GetMapping("/drivers/{driverId}/rides")
    public ResponseEntity<List<AdminRideHistoryItemDto>> getDriverRides(
            @PathVariable Long driverId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir) {
        return ResponseEntity.ok(rideHistoryService.getDriverRides(driverId, from, to, sort, dir));
    }

    /** History of rides for one passenger (userId = users.id). */
    @GetMapping("/users/{userId}/rides")
    public ResponseEntity<List<AdminRideHistoryItemDto>> getUserRides(
            @PathVariable Long userId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir) {
        return ResponseEntity.ok(rideHistoryService.getPassengerRides(userId, from, to, sort, dir));
    }

    /** Order the same route again on behalf of the ride's original passenger. */
    @PostMapping("/rides/{rideId}/reorder")
    public ResponseEntity<CreateRideResponseDto> reorderSameRoute(
            @PathVariable Long rideId,
            @Valid @RequestBody(required = false) AdminReorderRideRequestDto request) {
        CreateRideResponseDto resp = rideHistoryService.reorderSameRoute(rideId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
}
