package org.example.backend.dto.response;

public class RideTrackingResponseDto {

    private LatLngDto car;
    private LatLngDto pickup;
    private LatLngDto destination;

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

    public int getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(int etaMinutes) { this.etaMinutes = etaMinutes; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
