package org.example.backend.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface RideOrderRepository {

    boolean userHasOpenImmediateRide(String email);

    boolean userHasOpenImmediateRideConflictingWithSchedule(String email, OffsetDateTime latestAllowedFinishAt);

    boolean userHasScheduledRideBefore(String email, OffsetDateTime latestAllowedStartAt);

    boolean userHasScheduledRideInWindow(String email, OffsetDateTime fromInclusive, OffsetDateTime toInclusive);

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
            Long driverId,
            OffsetDateTime scheduledAt,
            String startAddress,
            String destinationAddress,
            BigDecimal price,
            String status,
            double startLat, double startLng,
            double destLat, double destLng,
            Double carLat, Double carLng,
            double distanceMeters,
            double durationSeconds,
            String vehicleType,
            boolean babyTransport,
            boolean petTransport,
            int requiredSeats
    );
}
