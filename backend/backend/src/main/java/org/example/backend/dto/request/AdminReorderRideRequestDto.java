package org.example.backend.dto.request;

import java.time.OffsetDateTime;

public class AdminReorderRideRequestDto {

    // null / absent => order immediately, present => schedule for later
    private OffsetDateTime scheduledAt;

    public AdminReorderRideRequestDto() {}

    public OffsetDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(OffsetDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
}
