package org.example.backend.repository;

import org.example.backend.dto.response.ActiveVehicleResponseDto;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InMemoryVehicleRepository implements VehicleRepository {

    @Override
    public List<ActiveVehicleResponseDto> findActiveVehicles(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        return List.of(
                build(1L, 45.2671, 19.8335, false),
                build(2L, 45.2638, 19.8412, true),
                build(3L, 45.2704, 19.8289, false)
        );
    }

    private ActiveVehicleResponseDto build(Long id, double lat, double lng, boolean busy) {
        ActiveVehicleResponseDto dto = new ActiveVehicleResponseDto();
        dto.setId(id);
        dto.setLatitude(lat);
        dto.setLongitude(lng);
        dto.setBusy(busy);
        return dto;
    }
}
