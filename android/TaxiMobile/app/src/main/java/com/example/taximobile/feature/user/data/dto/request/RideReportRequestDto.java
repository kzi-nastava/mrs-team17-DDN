package com.example.taximobile.feature.user.data.dto.request;

public class RideReportRequestDto {
    private String description;

    public RideReportRequestDto() {}

    public RideReportRequestDto(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
