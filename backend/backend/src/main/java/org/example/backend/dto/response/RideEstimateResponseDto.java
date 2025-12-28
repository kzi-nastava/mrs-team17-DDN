package org.example.backend.dto.response;

public class RideEstimateResponseDto {
    private int estimatedTimeMinutes;
    private double estimatedDistanceKm;

    public RideEstimateResponseDto() {}

    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(int estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }

    public double getEstimatedDistanceKm() { return estimatedDistanceKm; }
    public void setEstimatedDistanceKm(double estimatedDistanceKm) { this.estimatedDistanceKm = estimatedDistanceKm; }
}

