package com.knikolov.sharearide.dto;

public class CarDto {

    private String id;
    private String userId;
    private String manufacturer;
    private String model;
    private int seats;
    private int year;
    private String color;

    public CarDto() {
    }

    public CarDto(String id, String userId, String manufacturer, String model, int seats, int year, String color) {
        this.id = id;
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
