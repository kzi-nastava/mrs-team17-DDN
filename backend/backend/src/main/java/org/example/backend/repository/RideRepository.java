package org.example.backend.repository;

import org.example.backend.dto.request.RideReportRequestDto;
import org.example.backend.dto.response.RideReportResponseDto;
import org.example.backend.dto.response.RideTrackingResponseDto;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface RideRepository {

    Optional<RideTrackingResponseDto> findTrackingByRideId(Long rideId);

    RideReportResponseDto createReport(Long rideId, RideReportRequestDto request, OffsetDateTime now);

    boolean finishRide(Long rideId);
}
