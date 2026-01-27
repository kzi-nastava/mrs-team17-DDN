package org.example.backend.controller;

import org.example.backend.dto.request.UpdateUserProfileRequestDto;
import org.example.backend.dto.response.ProfileImageUploadResponseDto;
import org.example.backend.dto.response.UserProfileResponseDto;
import org.example.backend.service.UserProfileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService service;

    public UserProfileController(UserProfileService service) {
        this.service = service;
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponseDto> getUserProfile(@PathVariable Long userId) {
        return service.getProfile(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<Void> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UpdateUserProfileRequestDto request
    ) {
        boolean ok = service.updateProfile(userId, request);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/{userId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileImageUploadResponseDto> uploadUserProfileImage(
            @PathVariable Long userId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ProfileImageUploadResponseDto(null));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return ResponseEntity.badRequest().body(new ProfileImageUploadResponseDto(null));
        }

        File baseDir = new File("uploads/profile-images");
        if (!baseDir.exists()) baseDir.mkdirs();

        String original = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        String ext = getExtension(original);
        if (ext.isEmpty()) ext = "png";

        String filename = "user-" + userId + "-" + System.currentTimeMillis() + "." + ext;
        File target = new File(baseDir, filename);

        Files.copy(file.getInputStream(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String url = "/public/profile-images/" + filename;

        service.updateProfileImage(userId, url);

        return ResponseEntity.ok(new ProfileImageUploadResponseDto(url));
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "";
        String ext = filename.substring(idx + 1).toLowerCase(Locale.ROOT);
        if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("webp")) return ext;
        return "";
    }
}
