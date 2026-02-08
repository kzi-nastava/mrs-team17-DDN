package com.example.taximobile.feature.user.data.dto.response;

public class AddFavoriteFromRideResponseDto {
    private Long favoriteRouteId;
    private String status;

    public AddFavoriteFromRideResponseDto() {}

    public Long getFavoriteRouteId() { return favoriteRouteId; }
    public void setFavoriteRouteId(Long favoriteRouteId) { this.favoriteRouteId = favoriteRouteId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
