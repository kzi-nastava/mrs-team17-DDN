package org.example.backend.dto.response;

import java.time.LocalDateTime;

public class ProfileChangeRequestResponseDto {
    private Long requestId;
    private Long driverId;
    private String status;
    private LocalDateTime createdAt;

    public ProfileChangeRequestResponseDto() {}

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
