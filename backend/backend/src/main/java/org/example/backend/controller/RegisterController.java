package org.example.backend.controller;

import org.example.backend.dto.request.RegisterRequestDto;
import org.example.backend.dto.response.RegisterResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
public class RegisterController {

    @PostMapping
    public ResponseEntity<RegisterResponseDto> registerUser(
            @RequestBody RegisterRequestDto request) {
                

        

        return ResponseEntity.status(201).body(new RegisterResponseDto());
    }
}
