package org.example.backend.service;

import org.example.backend.dto.response.DriverRideDetailsResponseDto;
import org.example.backend.dto.response.DriverRideHistoryResponseDto;
import org.example.backend.repository.DriverRideRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class DriverRideService {

    private final DriverRideRepository repository;

    public DriverRideService(DriverRideRepository repository) {
        this.repository = repository;
    }

    public List<DriverRideHistoryResponseDto> getDriverRides(Long driverId, LocalDate from, LocalDate to) {
        return repository.findDriverRides(driverId, from, to);
    }

    public DriverRideDetailsResponseDto getDriverRideDetails(Long driverId, Long rideId) {
        return repository.findDriverRideDetails(driverId, rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found"));
    }

    public DriverRideDetailsResponseDto getActiveRide(Long driverId) {
        return repository.findActiveRideDetails(driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active ride"));
    }
}
