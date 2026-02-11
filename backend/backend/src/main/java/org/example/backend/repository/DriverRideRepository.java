package org.example.backend.repository;

import org.example.backend.dto.response.DriverRideDetailsResponseDto;
import org.example.backend.dto.response.DriverRideHistoryResponseDto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface DriverRideRepository {
    record StartRideSnapshot(
            String status,
            OffsetDateTime scheduledAt,
            Double pickupLat,
            Double pickupLng,
            Double carLat,
            Double carLng
    ) {}

    List<DriverRideHistoryResponseDto> findDriverRides(Long driverId, LocalDate from, LocalDate to);
    Optional<DriverRideDetailsResponseDto> findDriverRideDetails(Long driverId, Long rideId);
    Optional<DriverRideDetailsResponseDto> findActiveRideDetails(Long driverId);

    List<DriverRideDetailsResponseDto> findAcceptedRides(Long driverId);
    List<DriverRideDetailsResponseDto> findUpcomingRides(Long driverId);
    Optional<StartRideSnapshot> findStartRideSnapshot(Long driverId, Long rideId);
    boolean startRide(Long driverId, Long rideId);
    boolean finishRide(Long driverId, Long rideId);
    boolean hasUpcomingAssignedRide(Long driverId);
}
