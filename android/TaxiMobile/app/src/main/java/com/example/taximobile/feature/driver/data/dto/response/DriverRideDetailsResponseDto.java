package com.example.taximobile.feature.driver.data.dto.response;

import java.util.List;

public class DriverRideDetailsResponseDto {

    private Long rideId;

    private String startedAt; // OffsetDateTime as String
    private String endedAt;   // OffsetDateTime as String (nullable)

    private String startAddress;
    private String destinationAddress;
    private List<String> stops;

    private boolean canceled;
    private String canceledBy;

    private String status;

    private double price;
    private boolean panicTriggered;

    private List<PassengerInfoResponseDto> passengers;

    private List<RideReportResponseDto> reports;

    public DriverRideDetailsResponseDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }

    public String getEndedAt() { return endedAt; }
    public void setEndedAt(String endedAt) { this.endedAt = endedAt; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public List<String> getStops() { return stops; }
    public void setStops(List<String> stops) { this.stops = stops; }

    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }

    public String getCanceledBy() { return canceledBy; }
    public void setCanceledBy(String canceledBy) { this.canceledBy = canceledBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isPanicTriggered() { return panicTriggered; }
    public void setPanicTriggered(boolean panicTriggered) { this.panicTriggered = panicTriggered; }

    public List<PassengerInfoResponseDto> getPassengers() { return passengers; }
    public void setPassengers(List<PassengerInfoResponseDto> passengers) { this.passengers = passengers; }

    public List<RideReportResponseDto> getReports() { return reports; }
    public void setReports(List<RideReportResponseDto> reports) { this.reports = reports; }
}
