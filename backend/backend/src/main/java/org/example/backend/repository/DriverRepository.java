package org.example.backend.repository;

import java.util.Optional;

public interface DriverRepository {
    Long insertDriverReturningId(Long userId);
    Optional<Long> findDriverIdByUserId(long userId);
    void setAvailable(long driverId, boolean available);
}
