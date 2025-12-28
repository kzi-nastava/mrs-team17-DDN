package org.example.backend.dto.request;

public class RideEstimateRequestDto {
    private String startAddress;
    private String destinationAddress;

    public RideEstimateRequestDto() {}

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
}
