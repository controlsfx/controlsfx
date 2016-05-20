package org.controlsfx.samples.tablefilter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;


public final class Flight {

    private final int flightNumber;
    private final String orig;
    private final String dest;
    private final LocalDate departureTime;
    private final int mileaage;
    private final BooleanProperty cancelledInd = new SimpleBooleanProperty(false);
    private final StringProperty gateNumber = new SimpleStringProperty();

    public Flight(int flightNumber, String orig, String dest, LocalDate departureTime, int mileage, String gateNumber) {
        this.flightNumber = flightNumber;
        this.orig = orig;
        this.dest = dest;
        this.departureTime = departureTime;
        this.mileaage = mileage;
        this.gateNumber.set(gateNumber);
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

    public BooleanProperty getCancelledProperty() {
        return cancelledInd;
    }
    public StringProperty getGateNumber() {
        return gateNumber;
    }
}
