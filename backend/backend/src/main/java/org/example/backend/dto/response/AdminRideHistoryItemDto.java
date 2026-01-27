package org.example.backend.dto.response;

public class AdminRideHistoryItemDto {
    private String route;
    private String startDate;
    private String endDate;
    private boolean canceled;
    private String canceledBy;
    private double price;
    private boolean panicActivated;

    public AdminRideHistoryItemDto() {}

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean isCanceled() { return canceled; }
    public void setCanceled(boolean canceled) { this.canceled = canceled; }

    public String getCanceledBy() { return canceledBy; }
    public void setCanceledBy(String canceledBy) { this.canceledBy = canceledBy; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isPanicActivated() { return panicActivated; }
    public void setPanicActivated(boolean panicActivated) { this.panicActivated = panicActivated; }
}
