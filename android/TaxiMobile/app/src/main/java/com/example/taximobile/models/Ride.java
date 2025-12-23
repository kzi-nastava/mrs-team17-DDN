package com.example.taximobile.models;

public class Ride {

    // Ride info
    private String dateStart;
    private String dateEnd;
    private String route;
    private int price;
    private String status;
    private boolean panic;

    // Passenger (optional)
    private String passengerName;
    private String passengerEmail;

    // Constructor WITHOUT passenger
    public Ride(String dateStart, String dateEnd, String route,
                int price, String status, boolean panic) {
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.route = route;
        this.price = price;
        this.status = status;
        this.panic = panic;
    }

    // Constructor WITH passenger
    public Ride(String dateStart, String dateEnd, String route,
                int price, String status, boolean panic,
                String passengerName, String passengerEmail) {

        this(dateStart, dateEnd, route, price, status, panic);
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
    }

    // ===== GETTERS =====

    public String getDateStart() {
        return dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public String getRoute() {
        return route;
    }

    public int getPrice() {
        return price;
    }

    public String getStatus() {
        return status;
    }

    public boolean isPanic() {
        return panic;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    // ===== HELPERS =====

    public boolean hasPassenger() {
        return passengerName != null && passengerEmail != null;
    }
}
