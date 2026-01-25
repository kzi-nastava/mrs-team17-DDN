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

    Optional<RideMoveSnapshot> findMoveSnapshot(Long rideId);

    boolean updateVehicleLocation(long driverId, double lat, double lng);

    java.util.List<Long> findActiveRideIds();

    // NEW: one-way switch when car reaches pickup (prevents ping-pong)
    boolean markPickedUp(Long rideId);

    public record RideMoveSnapshot(
            String status,
            java.time.OffsetDateTime endedAt,
            boolean canceled,
            boolean pickedUp,      // NEW
            long driverId,
            double carLat,
            double carLng,
            double pickupLat,
            double pickupLng,
            double destLat,
            double destLng
    ) {}

    boolean startRide(Long rideId);
}
