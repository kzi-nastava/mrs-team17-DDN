package com.example.taximobile.feature.driver.model;

public class Ride {

    private long rideId; // <--- NEW

    private String dateStart;
    private String dateEnd;
    private String route;
    private int price;
    private String status;
    private boolean panic;

    private String passengerName;
    private String passengerEmail;

    public Ride(String dateStart, String dateEnd, String route,
                int price, String status, boolean panic) {
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.route = route;
        this.price = price;
        this.status = status;
        this.panic = panic;
    }

    public Ride(String dateStart, String dateEnd, String route,
                int price, String status, boolean panic,
                String passengerName, String passengerEmail) {

        this(dateStart, dateEnd, route, price, status, panic);
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
    }

    public long getRideId() { return rideId; }
    public void setRideId(long rideId) { this.rideId = rideId; }

    public String getDateStart() { return dateStart; }
    public String getDateEnd() { return dateEnd; }
    public String getRoute() { return route; }
    public int getPrice() { return price; }
    public String getStatus() { return status; }
    public boolean isPanic() { return panic; }
    public String getPassengerName() { return passengerName; }
    public String getPassengerEmail() { return passengerEmail; }

    public boolean hasPassenger() {
        return passengerName != null && passengerEmail != null;
    }
}
