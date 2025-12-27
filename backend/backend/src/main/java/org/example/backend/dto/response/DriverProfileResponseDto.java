package org.example.backend.dto.response;

public class DriverProfileResponseDto {
    private UserProfileResponseDto driver;
    private long activeMinutesLast24h;
    private VehicleInfoResponseDto vehicle;

    public DriverProfileResponseDto() {}

    public UserProfileResponseDto getDriver() { return driver; }
    public void setDriver(UserProfileResponseDto driver) { this.driver = driver; }

    public long getActiveMinutesLast24h() { return activeMinutesLast24h; }
    public void setActiveMinutesLast24h(long activeMinutesLast24h) { this.activeMinutesLast24h = activeMinutesLast24h; }

    public VehicleInfoResponseDto getVehicle() { return vehicle; }
    public void setVehicle(VehicleInfoResponseDto vehicle) { this.vehicle = vehicle; }
}
