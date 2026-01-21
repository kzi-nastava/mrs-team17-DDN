package org.example.backend.service;

import org.example.backend.dto.request.UpdateUserProfileRequestDto;
import org.example.backend.dto.response.UserProfileResponseDto;
import org.example.backend.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserProfileService {

    private final UserProfileRepository repo;

    public UserProfileService(UserProfileRepository repo) {
        this.repo = repo;
    }

    public Optional<UserProfileResponseDto> getProfile(Long userId) {
        return repo.findById(userId);
    }

    public boolean updateProfile(Long userId, UpdateUserProfileRequestDto req) {
        return repo.updateProfile(userId, req) == 1;
    }

    public boolean updateProfileImage(Long userId, String url) {
        return repo.updateProfileImage(userId, url) == 1;
    }
}
