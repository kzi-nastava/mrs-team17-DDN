package org.example.backend.controller;

import org.example.backend.dto.response.ActiveVehicleResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @GetMapping("/active")
    public ResponseEntity<List<ActiveVehicleResponseDto>> getActiveVehicles() {
        return ResponseEntity.ok(List.of());
    }
}
