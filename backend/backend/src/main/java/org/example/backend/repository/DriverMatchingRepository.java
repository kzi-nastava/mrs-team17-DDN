package org.example.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;

public interface DriverMatchingRepository {

    record CandidateDriver(
            Long driverId,
            double vehicleLat,
            double vehicleLng
    ) {}

    record FinishingSoonDriver(
            Long driverId,
            double vehicleLat,
            double vehicleLng,
            double finishLat,
            double finishLng,
            double remainingSeconds
    ) {}

    List<CandidateDriver> findAvailableDrivers(
            String vehicleTypeLower,
            boolean babyTransport,
            boolean petTransport,
            int requiredSeats
    );

    List<CandidateDriver> findAssignableDriversForScheduledRide(
            String vehicleTypeLower,
            boolean babyTransport,
            boolean petTransport,
            int requiredSeats
    );

    List<FinishingSoonDriver> findDriversFinishingSoon(
            String vehicleTypeLower,
            boolean babyTransport,
            boolean petTransport,
            int requiredSeats,
            int remainingSecondsThreshold
    );

    boolean setDriverAvailable(Long driverId, boolean available);

    boolean tryClaimAvailableDriver(Long driverId);

    boolean hasOpenImmediateRideConflictingWithSchedule(Long driverId, OffsetDateTime latestAllowedFinishAt);

    boolean hasAssignedScheduledRideBefore(Long driverId, OffsetDateTime latestAllowedStartAt);

    boolean hasScheduledRideInWindow(Long driverId, OffsetDateTime fromInclusive, OffsetDateTime toInclusive);
}
