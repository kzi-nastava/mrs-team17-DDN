package org.example.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class AddFavoriteRouteRequestDto {

    @NotNull
    private Long rideId;

    public AddFavoriteRouteRequestDto() {}

    public Long getRideId() { return rideId; }
    public void setRideId(Long rideId) { this.rideId = rideId; }
}
