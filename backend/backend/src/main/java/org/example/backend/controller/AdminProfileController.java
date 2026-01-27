package org.example.backend.controller;

import org.example.backend.dto.request.UpdateAdminProfileRequestDto;
import org.example.backend.dto.response.AdminProfileResponseDto;
import org.example.backend.dto.response.ProfileImageUploadResponseDto;
import org.example.backend.service.AdminProfileService;
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
@RequestMapping("/api/admins")
public class AdminProfileController {

    private final AdminProfileService service;

    public AdminProfileController(AdminProfileService service) {
        this.service = service;
    }

    @GetMapping("/{adminId}/profile")
    public ResponseEntity<AdminProfileResponseDto> getAdminProfile(@PathVariable Long adminId) {
        return service.getProfile(adminId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{adminId}/profile")
    public ResponseEntity<Void> updateAdminProfile(
            @PathVariable Long adminId,
            @RequestBody UpdateAdminProfileRequestDto request
    ) {
        boolean ok = service.updateProfile(adminId, request);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/{adminId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileImageUploadResponseDto> uploadAdminProfileImage(
            @PathVariable Long adminId,
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

        String filename = "admin-" + adminId + "-" + System.currentTimeMillis() + "." + ext;
        File target = new File(baseDir, filename);

        Files.copy(file.getInputStream(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String url = "/public/profile-images/" + filename;

        service.updateProfileImage(adminId, url);

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
