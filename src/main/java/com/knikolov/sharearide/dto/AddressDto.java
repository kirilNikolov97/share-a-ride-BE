package com.knikolov.sharearide.dto;

import com.knikolov.sharearide.models.City;

public class AddressDto {

    private String id;
    private City city;
    private String district;
    private String street;
    private String additionalInfo;
    private Double latitude;
    private Double longitude;

    public AddressDto() {
    }

    public AddressDto(String id, City city, String district, String street, String additionalInfo,
                      Double latitude, Double longitude) {
        this.id = id;
        this.city = city;
        this.district = district;
        this.street = street;
        this.additionalInfo = additionalInfo;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
