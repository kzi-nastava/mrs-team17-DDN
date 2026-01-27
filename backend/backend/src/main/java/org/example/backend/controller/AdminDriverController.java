package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.request.AdminCreateDriverRequestDto;
import org.example.backend.dto.response.AdminCreateDriverResponseDto;
import org.example.backend.service.AdminDriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/drivers")
public class AdminDriverController {

    private final AdminDriverService adminDriverService;

    public AdminDriverController(AdminDriverService adminDriverService) {
        this.adminDriverService = adminDriverService;
    }

    @PostMapping
    public ResponseEntity<AdminCreateDriverResponseDto> createDriver(
            @Valid @RequestBody AdminCreateDriverRequestDto request
    ) {
        AdminCreateDriverResponseDto response = adminDriverService.createDriver(request);
        return ResponseEntity.status(201).body(response);
    }
}
