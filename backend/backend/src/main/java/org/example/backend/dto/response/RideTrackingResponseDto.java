package org.example.backend.dto.response;

import java.util.List;

public class RideTrackingResponseDto {

    private LatLngDto car;
    private LatLngDto pickup;
    private LatLngDto destination;

    private List<LatLngDto> route;

    private List<RideCheckpointDto> checkpoints;

    private int etaMinutes;
    private double distanceKm;
    private String status;

    public RideTrackingResponseDto() {}

    public LatLngDto getCar() { return car; }
    public void setCar(LatLngDto car) { this.car = car; }

    public LatLngDto getPickup() { return pickup; }
    public void setPickup(LatLngDto pickup) { this.pickup = pickup; }

    public LatLngDto getDestination() { return destination; }
    public void setDestination(LatLngDto destination) { this.destination = destination; }

    public List<LatLngDto> getRoute() { return route; }
    public void setRoute(List<LatLngDto> route) { this.route = route; }

    public List<RideCheckpointDto> getCheckpoints() { return checkpoints; }
    public void setCheckpoints(List<RideCheckpointDto> checkpoints) { this.checkpoints = checkpoints; }

    public int getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(int etaMinutes) { this.etaMinutes = etaMinutes; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
