package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.request.CreateRideRequestDto;
import org.example.backend.dto.response.CreateRideResponseDto;
import org.example.backend.service.RideOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/rides")
public class RideOrderController {

    private final RideOrderService rideOrderService;

    public RideOrderController(RideOrderService rideOrderService) {
        this.rideOrderService = rideOrderService;
    }

    @PostMapping
    public ResponseEntity<CreateRideResponseDto> createRide(@Valid @RequestBody CreateRideRequestDto request) {
        long passengerUserId = requirePassengerUserId();
        CreateRideResponseDto resp = rideOrderService.createRide(passengerUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    private long requirePassengerUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        boolean isPassenger = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_PASSENGER".equals(a.getAuthority()));

        if (!isPassenger) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only passengers can access this endpoint");
        }

        try {
            return Long.parseLong(auth.getPrincipal().toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }
    }
}
