package com.example.taximobile.feature.user.data.dto.request;

public class RideRatingRequestDto {
    private int driverRating;
    private int vehicleRating;
    private String comment;

    public RideRatingRequestDto() {}

    public RideRatingRequestDto(int driverRating, int vehicleRating, String comment) {
        this.driverRating = driverRating;
        this.vehicleRating = vehicleRating;
        this.comment = comment;
    }

    public int getDriverRating() { return driverRating; }
    public void setDriverRating(int driverRating) { this.driverRating = driverRating; }

    public int getVehicleRating() { return vehicleRating; }
    public void setVehicleRating(int vehicleRating) { this.vehicleRating = vehicleRating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
