package org.example.backend.repository;

import org.example.backend.dto.response.DriverRideDetailsResponseDto;
import org.example.backend.dto.response.DriverRideHistoryResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DriverRideRepository {
    List<DriverRideHistoryResponseDto> findDriverRides(Long driverId, LocalDate from, LocalDate to);
    Optional<DriverRideDetailsResponseDto> findDriverRideDetails(Long driverId, Long rideId);
    Optional<DriverRideDetailsResponseDto> findActiveRideDetails(Long driverId);

    List<DriverRideDetailsResponseDto> findAcceptedRides(Long driverId);
    boolean startRide(Long driverId, Long rideId);
}
