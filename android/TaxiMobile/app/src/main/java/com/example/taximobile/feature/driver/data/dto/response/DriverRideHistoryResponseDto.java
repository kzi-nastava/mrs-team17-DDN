package com.example.taximobile.feature.driver.data.dto.response;

public class DriverRideHistoryResponseDto {

    private Long rideId;
    private String startedAt; // OffsetDateTime as String
    private String startAddress;
    private String endAddress;
    private boolean canceled;
    private String status;
    private double price;

    public DriverRideHistoryResponseDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getEndAddress() { return endAddress; }
    public void setEndAddress(String endAddress) { this.endAddress = endAddress; }

    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
