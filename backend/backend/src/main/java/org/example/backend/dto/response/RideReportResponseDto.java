package org.example.backend.dto.response;

import java.time.OffsetDateTime;

public class RideReportResponseDto {

    private Long id;
    private Long rideId;
    private String description;
    private OffsetDateTime createdAt;

    public RideReportResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
