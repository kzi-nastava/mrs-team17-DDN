package org.example.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public class OrderRideFromFavoriteRouteRequestDto {

    private List<@Email String> passengerEmails;

    @NotBlank
    private String vehicleType;

    private boolean babyTransport;
    private boolean petTransport;

    private LocalDateTime scheduledFor;

    public OrderRideFromFavoriteRouteRequestDto() {}

    public List<String> getPassengerEmails() { return passengerEmails; }
    public void setPassengerEmails(List<String> passengerEmails) { this.passengerEmails = passengerEmails; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public boolean isBabyTransport() { return babyTransport; }
    public void setBabyTransport(boolean babyTransport) { this.babyTransport = babyTransport; }

    public boolean isPetTransport() { return petTransport; }
    public void setPetTransport(boolean petTransport) { this.petTransport = petTransport; }

    public LocalDateTime getScheduledFor() { return scheduledFor; }
    public void setScheduledFor(LocalDateTime scheduledFor) { this.scheduledFor = scheduledFor; }
}
