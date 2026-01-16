package org.example.backend.controller;

import org.example.backend.dto.response.ActiveVehicleResponseDto;
import org.example.backend.service.VehicleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/active")
    public ResponseEntity<List<ActiveVehicleResponseDto>> getActiveVehicles(
            @RequestParam(required = false) Double minLat,
            @RequestParam(required = false) Double maxLat,
            @RequestParam(required = false) Double minLng,
            @RequestParam(required = false) Double maxLng
    ) {
        return ResponseEntity.ok(vehicleService.getActiveVehicles(minLat, maxLat, minLng, maxLng));
    }
}
