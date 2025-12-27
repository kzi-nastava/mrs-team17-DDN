package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.request.DriverActivateAccountRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers/activation")
public class DriverActivationController {

    @PostMapping
    public ResponseEntity<Void> activateDriver(@Valid @RequestBody DriverActivateAccountRequestDto request) {
        return ResponseEntity.ok().build();
    }
}
