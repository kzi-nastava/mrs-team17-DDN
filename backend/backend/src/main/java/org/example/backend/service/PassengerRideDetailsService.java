package org.example.backend.service;

import org.example.backend.dto.response.RidePassengerDetailsResponseDto;
import org.example.backend.repository.PassengerRideDetailsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PassengerRideDetailsService {

    private final PassengerRideDetailsRepository repo;

    public PassengerRideDetailsService(PassengerRideDetailsRepository repo) {
        this.repo = repo;
    }

    public RidePassengerDetailsResponseDto getMyRideDetails(long userId, Long rideId) {
        if (!repo.isPassengerOnRide(rideId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view details of your own rides");
        }

        return repo.findPassengerRideDetails(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
    }
}
