package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterDeviceTokenRequestDto {

    @NotBlank
    @Size(max = 4096)
    private String token;

    @Size(max = 20)
    private String platform;

    public RegisterDeviceTokenRequestDto() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
