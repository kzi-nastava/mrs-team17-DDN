package com.example.taximobile.feature.publichome.data.dto.response;

public class ActiveVehicleResponseDto {
    private Long id;
    private double latitude;
    private double longitude;
    private boolean busy;

    public ActiveVehicleResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isBusy() { return busy; }
    public void setBusy(boolean busy) { this.busy = busy; }
}
