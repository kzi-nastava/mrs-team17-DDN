package com.example.taximobile.feature.driver.data.dto.response;

public class VehicleInfoResponseDto {

    private String model;
    private String type;
    private String licensePlate;
    private int seats;
    private boolean babyTransport;
    private boolean petTransport;

    public VehicleInfoResponseDto() {}

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }

    public boolean isBabyTransport() { return babyTransport; }
    public void setBabyTransport(boolean babyTransport) { this.babyTransport = babyTransport; }

    public boolean isPetTransport() { return petTransport; }
    public void setPetTransport(boolean petTransport) { this.petTransport = petTransport; }
}
