package com.example.taximobile.feature.admin.data.dto.response;

public class AdminRideStatusRowDto {

    private long rideId;
    private long driverId;
    private Long userId;

    private String driverEmail;
    private String driverFirstName;
    private String driverLastName;

    private String status;
    private String startedAt; // OffsetDateTime comes as ISO string

    private double carLat;
    private double carLng;

    public long getRideId() { return rideId; }
    public void setRideId(long rideId) { this.rideId = rideId; }

    public long getDriverId() { return driverId; }
    public void setDriverId(long driverId) { this.driverId = driverId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDriverEmail() { return driverEmail; }
    public void setDriverEmail(String driverEmail) { this.driverEmail = driverEmail; }

    public String getDriverFirstName() { return driverFirstName; }
    public void setDriverFirstName(String driverFirstName) { this.driverFirstName = driverFirstName; }

    public String getDriverLastName() { return driverLastName; }
    public void setDriverLastName(String driverLastName) { this.driverLastName = driverLastName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }

    public double getCarLat() { return carLat; }
    public void setCarLat(double carLat) { this.carLat = carLat; }

    public double getCarLng() { return carLng; }
    public void setCarLng(double carLng) { this.carLng = carLng; }

    public String driverDisplayName() {
        String fn = driverFirstName != null ? driverFirstName : "";
        String ln = driverLastName != null ? driverLastName : "";
        String name = (fn + " " + ln).trim();
        if (!name.isEmpty()) return name;
        return driverEmail != null ? driverEmail : "Driver";
    }
}
