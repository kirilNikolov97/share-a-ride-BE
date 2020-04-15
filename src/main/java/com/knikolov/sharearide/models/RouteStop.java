package com.knikolov.sharearide.models;

import javax.persistence.*;
import java.util.UUID;

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
    private User userId;

    @Column(name = "passenger_enum")
    private String passengerEnum;

    @Column(name = "is_approved")
    private Boolean isApproved;

    public RouteStop() {
        this.id = UUID.randomUUID().toString();
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

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
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
