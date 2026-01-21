package org.example.backend.repository;

import org.example.backend.dto.request.UpdateDriverProfileRequestDto;
import org.example.backend.dto.response.UserProfileResponseDto;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface DriverProfileRepository {

    Optional<UserProfileResponseDto> findDriverUserProfile(Long driverId);

    int calcActiveMinutesLast24h(Long driverId);

    Long insertProfileChangeRequest(Long driverId,
                                    UpdateDriverProfileRequestDto req,
                                    OffsetDateTime now);
}
