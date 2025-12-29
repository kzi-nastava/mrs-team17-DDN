package org.example.backend.dto.response;

public class RegisterResponseDto {

    private Long userId;
    private String message;

    public RegisterResponseDto() {}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
