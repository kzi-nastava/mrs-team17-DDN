package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class RoutePointRequestDto {

    @NotBlank
    private String address;

    public RoutePointRequestDto() {
    }

    public RoutePointRequestDto(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
