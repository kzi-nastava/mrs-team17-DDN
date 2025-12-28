package org.example.backend.dto.response;

public class DriverRideHistoryResponseDto {

    private Long rideId;
    private String startAddress;
    private String endAddress;
    private boolean canceled;
    private double price;

    public DriverRideHistoryResponseDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getEndAddress() { return endAddress; }
    public void setEndAddress(String endAddress) { this.endAddress = endAddress; }

    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
