package com.example.taximobile.feature.user.data.dto.response;

public class CreateRideResponseDto {

    private Long rideId;
    private Long driverId;
    private String status;
    private Double price;

    public CreateRideResponseDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
