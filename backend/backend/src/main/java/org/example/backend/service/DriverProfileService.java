package org.example.backend.service;

import org.example.backend.dto.request.UpdateDriverProfileRequestDto;
import org.example.backend.dto.response.DriverProfileResponseDto;
import org.example.backend.dto.response.ProfileChangeRequestResponseDto;
import org.example.backend.dto.response.UserProfileResponseDto;
import org.example.backend.dto.response.VehicleInfoResponseDto;
import org.example.backend.repository.DriverProfileRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class DriverProfileService {

    private final DriverProfileRepository repo;

    public DriverProfileService(DriverProfileRepository repo) {
        this.repo = repo;
    }

    public Optional<DriverProfileResponseDto> getProfile(Long driverId) {
        Optional<UserProfileResponseDto> driverOpt = repo.findDriverUserProfile(driverId);
        if (!driverOpt.isPresent()) return Optional.empty();

        Optional<VehicleInfoResponseDto> vehicleOpt = repo.findDriverVehicleInfo(driverId);
        if (!vehicleOpt.isPresent()) {
            // Ako po tvom domenu svaki driver MORA imati vehicle,
            // onda je najčistije tretirati kao 404 / "incomplete profile".
            return Optional.empty();
        }

        int activeMinutes = repo.calcActiveMinutesLast24h(driverId);

        DriverProfileResponseDto response = new DriverProfileResponseDto();
        response.setDriver(driverOpt.get());
        response.setVehicle(vehicleOpt.get());
        response.setActiveMinutesLast24h(activeMinutes);

        return Optional.of(response);
    }

    public Optional<ProfileChangeRequestResponseDto> createProfileChangeRequest(
            Long driverId,
            UpdateDriverProfileRequestDto request
    ) {
        if (!repo.findDriverUserProfile(driverId).isPresent()) {
            return Optional.empty();
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long requestId = repo.insertProfileChangeRequest(driverId, request, now);

        ProfileChangeRequestResponseDto response = new ProfileChangeRequestResponseDto();
        response.setRequestId(requestId);
        response.setDriverId(driverId);
        response.setStatus("PENDING");
        response.setCreatedAt(now.toLocalDateTime());

        return Optional.of(response);
    }
}
