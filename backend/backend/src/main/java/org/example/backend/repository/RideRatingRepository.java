// backend/src/main/java/org/example/backend/repository/RideRatingRepository.java
package org.example.backend.repository;

import org.example.backend.dto.request.RideRatingRequestDto;
import org.example.backend.dto.response.RideRatingResponseDto;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface RideRatingRepository {

    Optional<RideRatingResponseDto> findByRideId(Long rideId);

    boolean existsForRide(Long rideId);

    boolean isRideCompletedAndNotCanceled(Long rideId);

    Optional<OffsetDateTime> findRideEndedAt(Long rideId);

    RideRatingResponseDto create(Long rideId, RideRatingRequestDto req, OffsetDateTime now);
}
