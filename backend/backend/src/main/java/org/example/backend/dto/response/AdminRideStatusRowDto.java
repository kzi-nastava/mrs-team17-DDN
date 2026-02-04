package org.example.backend.dto.response;

import java.time.OffsetDateTime;

public record AdminRideStatusRowDto(
        long rideId,
        long driverId,
        Long userId,
        String driverEmail,
        String driverFirstName,
        String driverLastName,
        String status,
        OffsetDateTime startedAt,
        double carLat,
        double carLng
) {}
