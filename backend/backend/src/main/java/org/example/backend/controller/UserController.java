package org.example.backend.controller;

import org.example.backend.dto.request.UserRegisterRequestDto;
import org.example.backend.dto.response.UserRegisterResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public ResponseEntity<UserRegisterResponseDto> registerUser(
            @RequestBody UserRegisterRequestDto request) {
        return ResponseEntity.status(201).body(new UserRegisterResponseDto());
    }
}
