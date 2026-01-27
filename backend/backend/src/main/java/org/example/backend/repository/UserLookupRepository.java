package org.example.backend.repository;

import java.util.Optional;

public interface UserLookupRepository {

    record UserBasic(
            Long id,
            String email,
            String firstName,
            String lastName,
            boolean active,
            boolean blocked,
            String blockReason
    ) {}

    Optional<UserBasic> findById(Long id);

    Optional<UserBasic> findByEmail(String email);
}
