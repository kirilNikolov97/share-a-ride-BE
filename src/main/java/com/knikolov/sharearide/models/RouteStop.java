package com.knikolov.sharearide.models;

import javax.persistence.*;
import java.util.UUID;

/**
 * Route stop entity. This entity holds information about every stop for every route
 */
@Entity
public class RouteStop {

    @Id
    @Column(name = "route_stop_id")
    private String id;

    @Column(name = "route_id")
    private String routeId;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address addressId;

    @ManyToOne
    @JoinColumn(name="passenger_id")
    private User user;

    @Column(name = "passenger_enum")
    private String passengerEnum;

    @Column(name = "is_approved")
    private Boolean isApproved;

    public RouteStop() {
        this.id = UUID.randomUUID().toString();
    }

    public RouteStop(String id, String routeId, Address addressId, User user, String passengerEnum, Boolean isApproved) {
        this.id = id;
        this.routeId = routeId;
        this.addressId = addressId;
        this.user = user;
        this.passengerEnum = passengerEnum;
        this.isApproved = isApproved;
    }

    public String getId() {
        return id;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public Address getAddress() {
        return this.addressId;
    }

    public void setAddress(Address addressId) {
        this.addressId = addressId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPassengerEnum() {
        return passengerEnum;
    }

    public void setPassengerEnum(String passengerEnum) {
        this.passengerEnum = passengerEnum;
    }

    public Address getAddressId() {
        return addressId;
    }

    public void setAddressId(Address addressId) {
        this.addressId = addressId;
    }

    public Boolean getApproved() {
        return isApproved;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }
}
