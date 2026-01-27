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

    Long insertPassengerUserReturningId(
        String email,
        String passwordHash,
        String firstName,
        String lastName,
        String address,
        String phone
);

// aktivacija bez menjanja passworda (potrebno za confirm)
int activateUser(Long userId);
}
