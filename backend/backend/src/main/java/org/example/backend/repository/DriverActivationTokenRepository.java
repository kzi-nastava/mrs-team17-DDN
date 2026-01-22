package org.example.backend.repository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface DriverActivationTokenRepository {

    Long createToken(Long userId, String token, OffsetDateTime expiresAt);

    Optional<TokenRow> findValidByToken(String token);

    int markUsed(Long tokenId);

    record TokenRow(Long id, Long userId) {}
}
