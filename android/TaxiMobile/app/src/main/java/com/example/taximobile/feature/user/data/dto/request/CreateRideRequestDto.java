package com.example.taximobile.feature.user.data.dto.request;

import java.util.List;

public class CreateRideRequestDto {

    private String orderType;
    private String scheduledAt;
    private RidePointRequestDto start;
    private RidePointRequestDto destination;
    private java.util.List<RidePointRequestDto> checkpoints;
    private java.util.List<String> linkedUsers;
    private String vehicleType;
    private Boolean babyTransport;
    private Boolean petTransport;

    public CreateRideRequestDto() {}

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(String scheduledAt) { this.scheduledAt = scheduledAt; }

    public RidePointRequestDto getStart() { return start; }
    public void setStart(RidePointRequestDto start) { this.start = start; }

    public RidePointRequestDto getDestination() { return destination; }
    public void setDestination(RidePointRequestDto destination) { this.destination = destination; }

    public java.util.List<RidePointRequestDto> getCheckpoints() { return checkpoints; }
    public void setCheckpoints(java.util.List<RidePointRequestDto> checkpoints) { this.checkpoints = checkpoints; }

    public java.util.List<String> getLinkedUsers() { return linkedUsers; }
    public void setLinkedUsers(java.util.List<String> linkedUsers) { this.linkedUsers = linkedUsers; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Boolean getBabyTransport() { return babyTransport; }
    public void setBabyTransport(Boolean babyTransport) { this.babyTransport = babyTransport; }

    public Boolean getPetTransport() { return petTransport; }
    public void setPetTransport(Boolean petTransport) { this.petTransport = petTransport; }
}
