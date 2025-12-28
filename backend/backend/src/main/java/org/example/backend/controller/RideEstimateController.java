package org.example.backend.controller;

import org.example.backend.dto.request.RideEstimateRequestDto;
import org.example.backend.dto.response.RideEstimateResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rides")
public class RideEstimateController {

    @PostMapping("/estimate")
    public ResponseEntity<RideEstimateResponseDto> estimateRide(
            @RequestBody RideEstimateRequestDto request) {
        return ResponseEntity.ok(new RideEstimateResponseDto());
    }
}
