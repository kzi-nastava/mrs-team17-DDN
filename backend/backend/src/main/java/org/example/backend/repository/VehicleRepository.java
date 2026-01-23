package org.example.backend.repository;

import org.example.backend.dto.response.ActiveVehicleResponseDto;

import java.util.List;

public interface VehicleRepository {

    List<ActiveVehicleResponseDto> findActiveVehicles(Double minLat, Double maxLat, Double minLng, Double maxLng);

    boolean existsByLicensePlate(String licensePlate);

    Long insertVehicleReturningId(
            Long driverId,
            double latitude,
            double longitude,
            String model,
            String type,
            String licensePlate,
            int seats,
            boolean babyTransport,
            boolean petTransport
    );
}
