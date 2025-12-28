package org.example.backend.controller;

import org.example.backend.dto.request.PasswordResetRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    @PostMapping
    public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetRequestDto request) {
        // Ovde ide kasnije logika za reset lozinke
        return ResponseEntity.status(201).build(); // 201 Created ili 200 OK
    }
}
