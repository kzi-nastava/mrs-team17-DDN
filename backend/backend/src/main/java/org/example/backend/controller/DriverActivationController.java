package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.request.DriverActivateAccountRequestDto;
import org.example.backend.service.DriverActivationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers/activation")
public class DriverActivationController {

    private final DriverActivationService activationService;

    public DriverActivationController(DriverActivationService activationService) {
        this.activationService = activationService;
    }

    @PostMapping
    public ResponseEntity<Void> activateDriver(@Valid @RequestBody DriverActivateAccountRequestDto request) {
        activationService.activate(request);
        return ResponseEntity.ok().build();
    }
}
