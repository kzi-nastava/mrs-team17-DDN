package com.example.taximobile.feature.driver.data.dto.response;

public class RideReportResponseDto {

    private Long id;
    private Long rideId;
    private String description;
    private String createdAt; // OffsetDateTime as String

    public RideReportResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
