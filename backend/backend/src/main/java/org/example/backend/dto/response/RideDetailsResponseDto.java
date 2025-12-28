package org.example.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class RideDetailsResponseDto {

    private Long rideId;
    private String status;
    private String startAddress;
    private String destinationAddress;
    private List<String> stops;
    private List<String> passengerEmails;

    private String vehicleType;
    private boolean babyTransport;
    private boolean petTransport;

    private double price;
    private LocalDateTime scheduledFor;

    private Long driverId;

    public RideDetailsResponseDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStartAddress() { return startAddress; }
    public void setStartAddress(String startAddress) { this.startAddress = startAddress; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public List<String> getStops() { return stops; }
    public void setStops(List<String> stops) { this.stops = stops; }

    public List<String> getPassengerEmails() { return passengerEmails; }
    public void setPassengerEmails(List<String> passengerEmails) { this.passengerEmails = passengerEmails; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public boolean isBabyTransport() { return babyTransport; }
    public void setBabyTransport(boolean babyTransport) { this.babyTransport = babyTransport; }

    public boolean isPetTransport() { return petTransport; }
    public void setPetTransport(boolean petTransport) { this.petTransport = petTransport; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public LocalDateTime getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(LocalDateTime scheduledFor) { this.scheduledFor = scheduledFor; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
}
