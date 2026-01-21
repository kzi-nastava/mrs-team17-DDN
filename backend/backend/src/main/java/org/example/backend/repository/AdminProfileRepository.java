package org.example.backend.repository;

import org.example.backend.dto.request.UpdateAdminProfileRequestDto;
import org.example.backend.dto.response.AdminProfileResponseDto;

import java.util.Optional;

public interface AdminProfileRepository {
    Optional<AdminProfileResponseDto> findById(Long adminId);
    int updateProfile(Long adminId, UpdateAdminProfileRequestDto req);
    int updateProfileImage(Long adminId, String profileImageUrl);
}