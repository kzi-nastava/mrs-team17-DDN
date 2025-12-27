package org.example.backend.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class StartRideRequestDto {

    @NotNull
    private Long driverId;

    private LocalDateTime startedAt;

    public StartRideRequestDto() {}

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
}
