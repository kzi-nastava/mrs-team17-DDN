package org.example.backend.dto.response;

import java.time.LocalDateTime;

public class RideOrderResponseDto {

    private Long rideId;
    private String status;
    private double price;
    private LocalDateTime scheduledFor;
    private String message;

    public RideOrderResponseDto() {
    }

    public RideOrderResponseDto(Long rideId, String status, double price, LocalDateTime scheduledFor, String message) {
        this.rideId = rideId;
        this.status = status;
        this.price = price;
        this.scheduledFor = scheduledFor;
        this.message = message;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
