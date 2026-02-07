package com.example.taximobile.feature.user.data.dto.response;

public class RideCheckpointDto {

    private int stopOrder;
    private String address;
    private double lat;
    private double lng;

    public RideCheckpointDto() {}

    public int getStopOrder() { return stopOrder; }
    public void setStopOrder(int stopOrder) { this.stopOrder = stopOrder; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
}
