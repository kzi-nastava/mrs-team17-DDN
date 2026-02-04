package org.example.backend.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface RideOrderRepository {

    boolean userHasActiveRide(String email);

    Long insertRideReturningId(
            Long driverId,
            OffsetDateTime scheduledAt,
            String startAddress,
            String destinationAddress,
            BigDecimal price,
            String status,
            double startLat, double startLng,
            double destLat, double destLng,
            double carLat, double carLng,
            double distanceMeters,
            double durationSeconds
    );

    Long insertScheduledRideReturningId(
            OffsetDateTime scheduledAt,
            String startAddress,
            String destinationAddress,
            BigDecimal price,
            String status,
            double startLat, double startLng,
            double destLat, double destLng,
            double distanceMeters,
            double durationSeconds,
            String vehicleType,
            boolean babyTransport,
            boolean petTransport,
            int requiredSeats
    );
}
