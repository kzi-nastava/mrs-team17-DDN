package org.example.backend.repository;

import java.time.Instant;
import java.util.Optional;

public interface RegistrationTokenRepository {

    record TokenRow(Long userId) {}

    void createToken(Long userId, String token, Instant expiresAt);

    Optional<TokenRow> findValidToken(String token, Instant now);

    void markUsed(String token, Instant usedAt);
}