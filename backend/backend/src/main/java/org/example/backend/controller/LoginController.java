package org.example.backend.controller;

import org.example.backend.dto.request.LoginRequestDto;
import org.example.backend.dto.response.LoginResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @PostMapping
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        //  logika autentifikacije
        return ResponseEntity.ok(new LoginResponseDto());
    }
}
