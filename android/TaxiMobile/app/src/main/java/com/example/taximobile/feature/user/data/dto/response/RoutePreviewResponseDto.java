package com.example.taximobile.feature.user.data.dto.response;

import java.util.List;

public class RoutePreviewResponseDto {

    private List<LatLngDto> route;
    private Integer etaMinutes;
    private Double distanceKm;

    public RoutePreviewResponseDto() {}

    public List<LatLngDto> getRoute() { return route; }
    public void setRoute(List<LatLngDto> route) { this.route = route; }

    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
}
