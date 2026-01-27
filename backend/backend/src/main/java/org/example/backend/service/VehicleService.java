package org.example.backend.service;

import org.example.backend.dto.response.ActiveVehicleResponseDto;
import org.example.backend.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository repository;

    public VehicleService(VehicleRepository repository) {
        this.repository = repository;
    }

    public List<ActiveVehicleResponseDto> getActiveVehicles(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        boolean any = minLat != null || maxLat != null || minLng != null || maxLng != null;
        boolean all = minLat != null && maxLat != null && minLng != null && maxLng != null;

        if (any && !all) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All bbox params must be provided.");
        }

        return repository.findActiveVehicles(minLat, maxLat, minLng, maxLng);
    }
}
