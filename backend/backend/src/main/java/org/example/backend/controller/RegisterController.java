package org.example.backend.controller;

import org.example.backend.dto.request.RegisterRequestDto;
import org.example.backend.dto.response.RegisterResponseDto;
import org.example.backend.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registration")
public class RegisterController {

    private final RegistrationService registrationService;

    public RegisterController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * 1) Registracija:
     * - napravi user-a kao neaktivan
     * - generiše token
     * - pošalje mail sa linkom ka frontu: /registration-confirm?token=...
     * - vrati 201
     */
    @PostMapping
    public ResponseEntity<RegisterResponseDto> registerUser(@RequestBody RegisterRequestDto request) {
        RegisterResponseDto response = registrationService.register(request);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * 2) Aktivacija naloga (klik iz maila):
     * - potvrdi token, aktivira user-a
     * - vrati 204 (ok bez body)
     */
    @GetMapping("/confirm")
    public ResponseEntity<Void> confirmRegistration(@RequestParam("token") String token) {
        registrationService.confirm(token);
        return ResponseEntity.noContent().build();
    }
}