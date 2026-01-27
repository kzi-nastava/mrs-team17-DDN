package org.example.backend.service;

import org.example.backend.dto.request.UpdateAdminProfileRequestDto;
import org.example.backend.dto.response.AdminProfileResponseDto;
import org.example.backend.repository.AdminProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminProfileService {

    private final AdminProfileRepository repo;

    public AdminProfileService(AdminProfileRepository repo) {
        this.repo = repo;
    }

    public Optional<AdminProfileResponseDto> getProfile(Long adminId) {
        return repo.findById(adminId);
    }

    public boolean updateProfile(Long adminId, UpdateAdminProfileRequestDto req) {
        return repo.updateProfile(adminId, req) == 1;
    }

    public boolean updateProfileImage(Long adminId, String url) {
        return repo.updateProfileImage(adminId, url) == 1;
    }
}
