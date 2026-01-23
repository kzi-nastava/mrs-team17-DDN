package org.example.backend.dto.response;

import java.math.BigDecimal;

public class CreateRideResponseDto {

    private Long rideId;
    private Long driverId;
    private String status;
    private BigDecimal price;

    public CreateRideResponseDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
