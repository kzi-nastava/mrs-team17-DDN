package org.example.backend.dto.response;

import java.util.List;

public class RoutePreviewResponseDto {

    private List<LatLngDto> route;
    private int etaMinutes;
    private double distanceKm;

    public RoutePreviewResponseDto() {}

    public RoutePreviewResponseDto(List<LatLngDto> route, int etaMinutes, double distanceKm) {
        this.route = route;
        this.etaMinutes = etaMinutes;
        this.distanceKm = distanceKm;
    }

    public List<LatLngDto> getRoute() { return route; }
    public void setRoute(List<LatLngDto> route) { this.route = route; }

    public int getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(int etaMinutes) { this.etaMinutes = etaMinutes; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
}
