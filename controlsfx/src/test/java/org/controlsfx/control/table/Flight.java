package org.controlsfx.control.table;

import java.time.LocalDate;


public final class Flight {

    private final int flightNumber;
    private final String orig;
    private final String dest;
    private final LocalDate departureTime;
    private final int mileaage;


    public Flight(int flightNumber, String orig, String dest, LocalDate departureTime, int mileage) {
        this.flightNumber = flightNumber;
        this.orig = orig;
        this.dest = dest;
        this.departureTime = departureTime;
        this.mileaage = mileage;
    }

    public int getFlightNumber() {
        return flightNumber;
    }

    public String getOrig() {
        return orig;
    }

    public String getDest() {
        return dest;
    }

    public LocalDate getDepartureDate() {
        return departureTime;
    }

    public int getMileaage() {
        return mileaage;
    }
}
