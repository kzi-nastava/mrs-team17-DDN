package org.example.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ScheduledRideRepository {

    record ScheduledRideRow(
            Long rideId,
            OffsetDateTime scheduledAt,
            String startAddress,
            double startLat,
            double startLng,
            String destinationAddress,
            double destLat,
            double destLng,
            String vehicleType,
            boolean babyTransport,
            boolean petTransport,
            int requiredSeats
    ) {}
    List<ScheduledRideRow> findDueScheduledRides(int minutesAhead);
    boolean assignDriverToScheduledRide(Long rideId, Long driverId);
    boolean markScheduledRideAccepted(Long rideId);
}
