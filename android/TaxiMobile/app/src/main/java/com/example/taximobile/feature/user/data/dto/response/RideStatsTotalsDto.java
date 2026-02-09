package com.example.taximobile.feature.user.data.dto.response;

public class RideStatsTotalsDto {
    private long rides;
    private double kilometers;
    private double money;

    public RideStatsTotalsDto() {}

    public long getRides() { return rides; }
    public void setRides(long rides) { this.rides = rides; }

    public double getKilometers() { return kilometers; }
    public void setKilometers(double kilometers) { this.kilometers = kilometers; }

    public double getMoney() { return money; }
    public void setMoney(double money) { this.money = money; }
}
