package org.example.backend.controller;

import org.example.backend.dto.request.UpdateAdminProfileRequestDto;
import org.example.backend.dto.response.AdminProfileResponseDto;
import org.example.backend.dto.response.ProfileImageUploadResponseDto;
import org.example.backend.service.AdminProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
        long currentAdminId = requireAdminUserId();
        enforceSameAdmin(adminId, currentAdminId);

        return service.getProfile(currentAdminId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{adminId}/profile")
    public ResponseEntity<Void> updateAdminProfile(
            @PathVariable Long adminId,
            @RequestBody UpdateAdminProfileRequestDto request
    ) {
        long currentAdminId = requireAdminUserId();
        enforceSameAdmin(adminId, currentAdminId);

        boolean ok = service.updateProfile(currentAdminId, request);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/{adminId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileImageUploadResponseDto> uploadAdminProfileImage(
            @PathVariable Long adminId,
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        long currentAdminId = requireAdminUserId();
        enforceSameAdmin(adminId, currentAdminId);

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

        String filename = "admin-" + currentAdminId + "-" + System.currentTimeMillis() + "." + ext;
        File target = new File(baseDir, filename);

        Files.copy(file.getInputStream(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String url = "/public/profile-images/" + filename;

        service.updateProfileImage(currentAdminId, url);

        return ResponseEntity.ok(new ProfileImageUploadResponseDto(url));
    }

    private void enforceSameAdmin(Long pathAdminId, long currentAdminId) {
        if (pathAdminId == null || pathAdminId.longValue() != currentAdminId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can access only your own admin profile.");
        }
    }

    private long requireAdminUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can access this endpoint");
        }

        try {
            return Long.parseLong(auth.getPrincipal().toString());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) return "";
        String ext = filename.substring(idx + 1).toLowerCase(Locale.ROOT);
        if (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("webp")) return ext;
        return "";
    }
}
