package com.example.taximobile.feature.user.data.dto.request;

public class RidePointRequestDto {

    private String address;
    private Double lat;
    private Double lng;

    public RidePointRequestDto() {}

    public RidePointRequestDto(String address, Double lat, Double lng) {
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
}
