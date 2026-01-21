package org.example.backend.service;

import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.response.RideReportResponseDto;
import org.example.backend.dto.response.RideTrackingResponseDto;
import org.example.backend.repository.RideRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
public class RideService {

    private final RideRepository repository;

    public RideService(RideRepository repository) {
        this.repository = repository;
    }

    public RideTrackingResponseDto getRideTracking(Long rideId) {
        return repository.findTrackingByRideId(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride tracking not found"));
    }

    public RideReportResponseDto reportRideIssue(Long rideId, RideReportRequestDto request) {
        try {
            return repository.createReport(rideId, request, OffsetDateTime.now());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public void finishRide(Long rideId) {
        boolean ok = repository.finishRide(rideId);
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found");
        }
    }
}
