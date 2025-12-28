package org.example.backend.controller;

import org.example.backend.dto.request.RideCancelRequestDto;
import org.example.backend.dto.request.RidePauseRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rides")
public class RideActionsController {

    @PutMapping("/{rideId}/cancel")
    public ResponseEntity<Void> cancelRide(
            @PathVariable Long rideId,
            @RequestBody RideCancelRequestDto request) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{rideId}/pause")
    public ResponseEntity<Void> pauseRide(
            @PathVariable Long rideId,
            @RequestBody RidePauseRequestDto request) {
        return ResponseEntity.ok().build();
    }
}
