package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.request.AdminPricingUpdateRequestDto;
import org.example.backend.dto.response.AdminPricingResponseDto;
import org.example.backend.service.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pricing")
public class AdminPricingController {

    private final PricingService service;

    public AdminPricingController(PricingService service) {
        this.service = service;
    }

    @GetMapping
    public AdminPricingResponseDto get() {
        return service.get();
    }

    @PutMapping
    public ResponseEntity<Void> update(@Valid @RequestBody AdminPricingUpdateRequestDto req) {
        service.update(req);
        return ResponseEntity.ok().build();
    }
}
