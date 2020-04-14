package com.knikolov.sharearide.models;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class Address {

    @Id
    @Column(name = "address_id")
    private String id;

    @ManyToOne
    @JoinColumn(name = "city")
    private City city;

    @Length(max = 64)
    private String district;

    @Length(max = 128)
    private String street;

    @Column(name = "additional_info")
    @Length(max = 500)
    private String additionalInfo;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private Boolean deleted;

    public Address() {
        this.id = UUID.randomUUID().toString();
    }

    public Address(City city, String district, String street) {
        this.id = UUID.randomUUID().toString();
        this.city = city;
        this.district = district;
        this.street = street;
    }

    public Address(City city, String district, String street, String additionalInfo) {
        this.id = UUID.randomUUID().toString();
        this.city = city;
        this.district = district;
        this.street = street;
        this.additionalInfo = additionalInfo;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
