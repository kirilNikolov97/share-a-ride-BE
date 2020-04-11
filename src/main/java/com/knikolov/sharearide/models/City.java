package com.knikolov.sharearide.models;

import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class City {

    @Id
    @Column
    @Length(min = 1, max = 30, message = "City name should be more than 1 letter and less than 30 letters!")
    private String name;

    public City() {
    }

    public City(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
