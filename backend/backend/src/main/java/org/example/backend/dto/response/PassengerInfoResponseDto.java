package org.example.backend.dto.response;

public class PassengerInfoResponseDto {

    private Long passengerId;
    private String fullName;
    private String email;

    public PassengerInfoResponseDto() {}

    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
