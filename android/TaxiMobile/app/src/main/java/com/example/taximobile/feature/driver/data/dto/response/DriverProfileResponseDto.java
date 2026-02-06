package com.example.taximobile.feature.driver.data.dto.response;

public class DriverProfileResponseDto {

    private UserProfileResponseDto driver;
    private VehicleInfoResponseDto vehicle;
    private long activeMinutesLast24h;

    public DriverProfileResponseDto() {}

    public UserProfileResponseDto getDriver() { return driver; }
    public void setDriver(UserProfileResponseDto driver) { this.driver = driver; }

    public VehicleInfoResponseDto getVehicle() { return vehicle; }
    public void setVehicle(VehicleInfoResponseDto vehicle) { this.vehicle = vehicle; }

    public long getActiveMinutesLast24h() { return activeMinutesLast24h; }
    public void setActiveMinutesLast24h(long activeMinutesLast24h) { this.activeMinutesLast24h = activeMinutesLast24h; }
}
