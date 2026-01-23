package org.example.backend.repository;

import java.util.List;

public interface DriverMatchingRepository {

    record CandidateDriver(
            Long driverId,
            double vehicleLat,
            double vehicleLng
    ) {}

    List<CandidateDriver> findAvailableDrivers(
            String vehicleTypeLower,
            boolean babyTransport,
            boolean petTransport
    );

    boolean setDriverAvailable(Long driverId, boolean available);
}
