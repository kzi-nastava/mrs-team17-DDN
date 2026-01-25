// backend/src/main/java/org/example/backend/service/RideRatingService.java
package org.example.backend.service;

import org.example.backend.dto.request.RideRatingRequestDto;
import org.example.backend.dto.response.RideRatingResponseDto;
import org.example.backend.repository.RideRatingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Service
public class RideRatingService {

    private final RideRatingRepository repo;

    public RideRatingService(RideRatingRepository repo) {
        this.repo = repo;
    }

    public RideRatingResponseDto getRating(Long rideId) {
        return repo.findByRideId(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rating not found"));
    }

    public RideRatingResponseDto submitRating(Long rideId, RideRatingRequestDto req) {
        validateRequest(req);

        if (!repo.isRideCompletedAndNotCanceled(rideId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride is not completed");
        }

        var endedAt = repo.findRideEndedAt(rideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride not found"));

        if (endedAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride is not completed");
        }

        if (OffsetDateTime.now().isAfter(endedAt.plusDays(3))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating window expired");
        }

        if (repo.existsForRide(rideId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Rating already exists");
        }

        return repo.create(rideId, req, OffsetDateTime.now());
    }

    private static void validateRequest(RideRatingRequestDto req) {
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
        }

        if (req.getDriverRating() < 1 || req.getDriverRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver rating must be 1..5");
        }

        if (req.getVehicleRating() < 1 || req.getVehicleRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle rating must be 1..5");
        }

        String c = req.getComment();
        if (c != null && c.trim().length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment too long");
        }
    }
}
