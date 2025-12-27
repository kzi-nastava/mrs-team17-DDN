package org.example.backend.controller;

import org.example.backend.dto.request.ChangePasswordRequestDto;
import org.example.backend.dto.request.UpdateUserProfileRequestDto;
import org.example.backend.dto.response.UserProfileResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(@PathVariable Long userId) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setId(userId);
        dto.setEmail("user@example.com");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setAddress("Bulevar Example 1");
        dto.setPhoneNumber("+38160111222");
        dto.setProfileImageUrl("https://example.com/default-avatar.png");
        dto.setRole("USER");

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponseDto> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UpdateUserProfileRequestDto request) {

        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setId(userId);
        dto.setEmail("user@example.com");
        dto.setFirstName(request.getFirstName());
        dto.setLastName(request.getLastName());
        dto.setAddress(request.getAddress());
        dto.setPhoneNumber(request.getPhoneNumber());

        String image = request.getProfileImageUrl();
        dto.setProfileImageUrl((image == null || image.isBlank()) ? "https://example.com/default-avatar.png" : image);

        dto.setRole("USER");

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{userId}/profile/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long userId,
            @RequestBody ChangePasswordRequestDto request) {

        return ResponseEntity.noContent().build();
    }
}
