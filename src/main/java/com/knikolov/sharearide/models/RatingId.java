package com.knikolov.sharearide.models;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class RatingId implements Serializable {

    private String driverId;

    private String passengerId;

    public RatingId() {
    }

    public RatingId(String driverId, String passengerId) {
        this.driverId = driverId;
        this.passengerId = passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }
}
