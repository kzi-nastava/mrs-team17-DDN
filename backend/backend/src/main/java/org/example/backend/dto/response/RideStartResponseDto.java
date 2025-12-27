package org.example.backend.dto.response;

import java.time.LocalDateTime;

public class RideStartResponseDto {

    private Long rideId;
    private String status;
    private LocalDateTime startedAt;
    private String message;

    public RideStartResponseDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
