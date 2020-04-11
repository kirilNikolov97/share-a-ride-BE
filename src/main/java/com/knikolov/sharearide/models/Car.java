package com.knikolov.sharearide.models;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Car {

    @Id
    @Column(name = "car_id")
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column
    @Length(max = 30)
    private String manufacturer;

    @Column
    @Length(max = 30)
    private String model;

    @Column
    private int seats;

    @Column
    private int year;

    @Column
    @Length(max = 30)
    private String color;

    public Car() {
        this.id = UUID.randomUUID().toString();
    }

    public Car(String userId, String manufacturer, String model, int seats, int year, String color) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.manufacturer = manufacturer;
        this.model = model;
        this.seats = seats;
        this.year = year;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
