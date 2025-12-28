package org.example.backend.dto.response;

import java.time.OffsetDateTime;

public class RideRatingResponseDto {

    private Long id;
    private Long rideId;
    private int driverRating;
    private int vehicleRating;
    private String comment;
    private OffsetDateTime createdAt;

    public RideRatingResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }

    public int getDriverRating() { return driverRating; }
    public void setDriverRating(int driverRating) { this.driverRating = driverRating; }

    public int getVehicleRating() { return vehicleRating; }
    public void setVehicleRating(int vehicleRating) { this.vehicleRating = vehicleRating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
