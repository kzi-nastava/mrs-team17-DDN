package com.example.taximobile.feature.user.data.dto.response;

public class RideStatsPointDto {
    private String date;

    private long rides;
    private double kilometers;
    private double money;

    private long cumulativeRides;
    private double cumulativeKilometers;
    private double cumulativeMoney;

    public RideStatsPointDto() {}

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getRides() { return rides; }
    public void setRides(long rides) { this.rides = rides; }

    public double getKilometers() { return kilometers; }
    public void setKilometers(double kilometers) { this.kilometers = kilometers; }

    public double getMoney() { return money; }
    public void setMoney(double money) { this.money = money; }

    public long getCumulativeRides() { return cumulativeRides; }
    public void setCumulativeRides(long cumulativeRides) { this.cumulativeRides = cumulativeRides; }

    public double getCumulativeKilometers() { return cumulativeKilometers; }
    public void setCumulativeKilometers(double cumulativeKilometers) { this.cumulativeKilometers = cumulativeKilometers; }

    public double getCumulativeMoney() { return cumulativeMoney; }
    public void setCumulativeMoney(double cumulativeMoney) { this.cumulativeMoney = cumulativeMoney; }
}
