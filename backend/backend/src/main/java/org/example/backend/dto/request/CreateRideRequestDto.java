package org.example.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public class CreateRideRequestDto {

    @NotNull
    private Long requesterUserId;

    @NotBlank
    private String orderType;

    private OffsetDateTime scheduledAt;

    @NotNull @Valid
    private RidePointRequestDto start;

    @NotNull @Valid
    private RidePointRequestDto destination;

    @Valid
    private List<RidePointRequestDto> checkpoints;

    private List<String> linkedUsers;

    @NotBlank
    private String vehicleType;

    @NotNull
    private Boolean babyTransport;

    @NotNull
    private Boolean petTransport;

    public CreateRideRequestDto() {}

    public Long getRequesterUserId() { return requesterUserId; }
    public void setRequesterUserId(Long requesterUserId) { this.requesterUserId = requesterUserId; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public OffsetDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(OffsetDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public RidePointRequestDto getStart() { return start; }
    public void setStart(RidePointRequestDto start) { this.start = start; }

    public RidePointRequestDto getDestination() { return destination; }
    public void setDestination(RidePointRequestDto destination) { this.destination = destination; }

    public List<RidePointRequestDto> getCheckpoints() { return checkpoints; }
    public void setCheckpoints(List<RidePointRequestDto> checkpoints) { this.checkpoints = checkpoints; }

    public List<String> getLinkedUsers() { return linkedUsers; }
    public void setLinkedUsers(List<String> linkedUsers) { this.linkedUsers = linkedUsers; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public Boolean getBabyTransport() { return babyTransport; }
    public void setBabyTransport(Boolean babyTransport) { this.babyTransport = babyTransport; }

    public Boolean getPetTransport() { return petTransport; }
    public void setPetTransport(Boolean petTransport) { this.petTransport = petTransport; }
}
