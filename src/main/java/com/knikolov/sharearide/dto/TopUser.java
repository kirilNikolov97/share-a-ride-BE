package com.knikolov.sharearide.dto;

import com.knikolov.sharearide.models.User;

public class TopUser implements Comparable<TopUser>{

    private User user;
    private Integer numberRides;
    private Integer passengersNumber;

    public TopUser(User user, Integer numberRides, Integer passengersNumber) {
        this.user = user;
        this.numberRides = numberRides;
        this.passengersNumber = passengersNumber;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getNumberRides() {
        return numberRides;
    }

    public void setNumberRides(Integer numberRides) {
        this.numberRides = numberRides;
    }

    public Integer getPassengersNumber() {
        return passengersNumber;
    }

    public void setPassengersNumber(Integer passengersNumber) {
        this.passengersNumber = passengersNumber;
    }

    @Override
    public int compareTo(TopUser o) {
        return o.getPassengersNumber().compareTo(this.getPassengersNumber());
    }
}
