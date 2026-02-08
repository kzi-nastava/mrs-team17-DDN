package com.example.taximobile.feature.user.data.dto.response;

import java.util.List;

public class PassengerRideHistoryResponseDto {
    private Long rideId;
    private String startedAt;
    private String startAddress;
    private String destinationAddress;
    private List<String> stops;

    public PassengerRideHistoryResponseDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public List<String> getStops() { return stops; }
    public void setStops(List<String> stops) { this.stops = stops; }
}
