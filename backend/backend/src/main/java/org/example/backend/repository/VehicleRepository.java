package org.example.backend.repository;

import org.example.backend.dto.response.ActiveVehicleResponseDto;

import java.util.List;

public interface VehicleRepository {
    List<ActiveVehicleResponseDto> findActiveVehicles(Double minLat, Double maxLat, Double minLng, Double maxLng);
}
