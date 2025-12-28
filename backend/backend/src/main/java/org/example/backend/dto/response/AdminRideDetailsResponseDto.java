package org.example.backend.dto.response;

public class AdminRideDetailsResponseDto {
    private String route;
    private String startDate;
    private String endDate;
    private String driverName;
    private String passengers;
    private boolean canceled;
    private double price;
    private boolean panicActivated;

    public AdminRideDetailsResponseDto() {}

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getPassengers() { return passengers; }
    public void setPassengers(String passengers) { this.passengers = passengers; }

    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isPanicActivated() { return panicActivated; }
    public void setPanicActivated(boolean panicActivated) { this.panicActivated = panicActivated; }
}
