package org.example.backend.repository;

import org.example.backend.dto.response.RidePassengerDetailsResponseDto;

import java.util.Optional;

public interface PassengerRideDetailsRepository {

    /**
     * Full details for the "Info" popup in passenger ride history:
     * addresses, coordinates, road-following route, checkpoints, ride reports,
     * rating (if any) and driver/vehicle public info (if a driver was assigned).
     */
    Optional<RidePassengerDetailsResponseDto> findPassengerRideDetails(Long rideId);

    /**
     * True if the given user is the passenger (by email) on this ride.
     */
    boolean isPassengerOnRide(Long rideId, long userId);
}
