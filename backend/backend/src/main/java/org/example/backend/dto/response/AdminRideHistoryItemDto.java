package org.example.backend.dto.response;

import java.time.OffsetDateTime;

public class AdminRideHistoryItemDto {

    private Long rideId;

    private String startAddress;
    private String destinationAddress;

    // "startAddress ⟶ destinationAddress", handy for a single-column display
    private String route;

    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    private String status;

    private boolean canceled;
    private String canceledBy;

    private double price;

    private boolean panicActivated;

    public AdminRideHistoryItemDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public OffsetDateTime getStartDate() { return startDate; }
    public void setStartDate(OffsetDateTime startDate) { this.startDate = startDate; }

    public OffsetDateTime getEndDate() { return endDate; }
    public void setEndDate(OffsetDateTime endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }

    public String getCanceledBy() { return canceledBy; }
    public void setCanceledBy(String canceledBy) { this.canceledBy = canceledBy; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isPanicActivated() { return panicActivated; }
    public void setPanicActivated(boolean panicActivated) { this.panicActivated = panicActivated; }
}
