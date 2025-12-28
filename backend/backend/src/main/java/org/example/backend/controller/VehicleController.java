package org.example.backend.controller;

import org.example.backend.dto.response.ActiveVehicleResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @GetMapping("/active")
    public ResponseEntity<List<ActiveVehicleResponseDto>> getActiveVehicles(
            @RequestParam(required = false) Double minLat,
            @RequestParam(required = false) Double maxLat,
            @RequestParam(required = false) Double minLng,
            @RequestParam(required = false) Double maxLng
    ) {
        boolean any = minLat != null || maxLat != null || minLng != null || maxLng != null;
        boolean all = minLat != null && maxLat != null && minLng != null && maxLng != null;

        if (any && !all) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(List.of());
    }
}
