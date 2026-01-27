package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.request.CreateRideRequestDto;
import org.example.backend.dto.response.CreateRideResponseDto;
import org.example.backend.service.RideOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rides")
public class RideOrderController {

    private final RideOrderService rideOrderService;

    public RideOrderController(RideOrderService rideOrderService) {
        this.rideOrderService = rideOrderService;
    }

    @PostMapping
    public ResponseEntity<CreateRideResponseDto> createRide(@Valid @RequestBody CreateRideRequestDto request) {
        CreateRideResponseDto resp = rideOrderService.createRide(request);
        return ResponseEntity.status(201).body(resp);
    }
}
