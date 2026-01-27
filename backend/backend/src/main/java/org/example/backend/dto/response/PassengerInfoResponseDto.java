package org.example.backend.dto.response;

public class PassengerInfoResponseDto {

    private String name;
    private String email;

    public PassengerInfoResponseDto() {}

    public PassengerInfoResponseDto(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
