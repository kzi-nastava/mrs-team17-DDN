package org.example.backend.repository;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository {

    void createToken(Long userId, String token, Instant expiresAt);

    Optional<TokenRow> findValidToken(String token, Instant now);

    int markUsed(String token, Instant usedAt);

    record TokenRow(long id, long userId, String token, Instant expiresAt, Instant usedAt) {}
}
