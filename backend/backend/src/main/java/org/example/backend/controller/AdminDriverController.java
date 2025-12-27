package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.request.AdminCreateDriverRequestDto;
import org.example.backend.dto.response.AdminCreateDriverResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/drivers")
public class AdminDriverController {

    @PostMapping
    public ResponseEntity<AdminCreateDriverResponseDto> createDriver(@Valid @RequestBody AdminCreateDriverRequestDto request) {

        AdminCreateDriverResponseDto response = new AdminCreateDriverResponseDto();
        response.setDriverId(1L);
        response.setEmail(request.getEmail());
        response.setStatus("PENDING_PASSWORD_SETUP");
        response.setActivationLinkValidHours(24);

        return ResponseEntity.status(201).body(response);
    }
}
