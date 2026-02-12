package com.example.taximobile.feature.user.data.dto.response;

public class RideStatsAveragesDto {
    private double ridesPerDay;
    private double kilometersPerDay;
    private double moneyPerDay;

    private double kilometersPerRide;
    private double moneyPerRide;

    public RideStatsAveragesDto() {}

    public double getRidesPerDay() { return ridesPerDay; }
    public void setRidesPerDay(double ridesPerDay) { this.ridesPerDay = ridesPerDay; }

    public double getKilometersPerDay() { return kilometersPerDay; }
    public void setKilometersPerDay(double kilometersPerDay) { this.kilometersPerDay = kilometersPerDay; }

    public double getMoneyPerDay() { return moneyPerDay; }
    public void setMoneyPerDay(double moneyPerDay) { this.moneyPerDay = moneyPerDay; }

    public double getKilometersPerRide() { return kilometersPerRide; }
    public void setKilometersPerRide(double kilometersPerRide) { this.kilometersPerRide = kilometersPerRide; }

    public double getMoneyPerRide() { return moneyPerRide; }
    public void setMoneyPerRide(double moneyPerRide) { this.moneyPerRide = moneyPerRide; }
}
