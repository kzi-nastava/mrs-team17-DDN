package org.example.backend.dto.response;

public record UserAuthResponseDto(
        long id,
        String role,
        String email,
        String passwordHash,
        boolean isActive,
        boolean blocked
) {}
