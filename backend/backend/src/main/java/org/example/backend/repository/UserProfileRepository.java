package org.example.backend.repository;

import org.example.backend.dto.request.UpdateUserProfileRequestDto;
import org.example.backend.dto.response.UserProfileResponseDto;

import java.util.Optional;

public interface UserProfileRepository {
    Optional<UserProfileResponseDto> findById(Long userId);
    int updateProfile(Long userId, UpdateUserProfileRequestDto req);
    int updateProfileImage(Long userId, String profileImageUrl);
}
