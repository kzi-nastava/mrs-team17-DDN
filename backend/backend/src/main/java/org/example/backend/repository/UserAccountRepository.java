package org.example.backend.repository;

public interface UserAccountRepository {

    boolean existsByEmail(String email);

    Long insertDriverUserReturningId(
            String email,
            String passwordHash,
            String firstName,
            String lastName,
            String address,
            String phone
    );

    int activateAndSetPassword(Long userId, String newPasswordHash);
}
