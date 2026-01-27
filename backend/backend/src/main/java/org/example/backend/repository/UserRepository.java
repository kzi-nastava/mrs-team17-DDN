package org.example.backend.repository;

import org.example.backend.dto.response.UserAuthResponseDto;

import java.util.Optional;

public interface UserRepository {
    Optional<UserAuthResponseDto> findAuthByEmail(String email);
}
