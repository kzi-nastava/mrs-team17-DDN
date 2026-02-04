package org.example.backend.repository;

import org.example.backend.dto.response.UserAuthResponseDto;

import java.util.Optional;

public interface UserRepository {
    Optional<UserAuthResponseDto> findAuthByEmail(String email);
    Optional<UserAuthResponseDto> findAuthById(Long id);
    int updatePasswordHash(Long id, String newPasswordHash);
}
