package org.example.backend.dto.response;

public class ProfileImageUploadResponseDto {
    private String profileImageUrl;

    public ProfileImageUploadResponseDto() {}

    public ProfileImageUploadResponseDto(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
