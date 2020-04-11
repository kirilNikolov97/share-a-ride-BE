package com.knikolov.sharearide.dto;

import com.knikolov.sharearide.models.User;

public class TopUser implements Comparable<TopUser>{

    private User user;
    private Integer numberRides;

    public TopUser(User user, Integer numberRides) {
        this.user = user;
        this.numberRides = numberRides;
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

    @Override
    public int compareTo(TopUser o) {
        return o.getNumberRides().compareTo(this.getNumberRides());
    }
}
