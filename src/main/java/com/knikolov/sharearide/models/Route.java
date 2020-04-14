package com.knikolov.sharearide.models;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
public class Route implements Serializable {

    @Id
    @Column(name = "route_id")
    private String id;

    @Column(name = "date_route")
    private LocalDateTime dateRoute;

    @Column(name = "canceled")
    private Boolean canceled;

    @Column(name = "office_direction")
    private Boolean officeDirection;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @OneToMany(mappedBy = "routeId", fetch = FetchType.EAGER)
    private List<RouteStop> routeStops;

    public Route() {
        this.id = UUID.randomUUID().toString();
        this.canceled = Boolean.FALSE;
    }

    public Route(LocalDateTime dateRoute, Boolean canceled, Car car) {
        this.id = UUID.randomUUID().toString();
        this.dateRoute = dateRoute;
        this.canceled = canceled;
        this.car = car;
    }

    public Route(LocalDateTime dateRoute, Boolean canceled, Car car, List<RouteStop> routeStops) {
        this.id = UUID.randomUUID().toString();
        this.dateRoute = dateRoute;
        this.canceled = canceled;
        this.car = car;
        this.routeStops = routeStops;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getDateRoute() {
        return dateRoute;
    }

    public void setDateRoute(LocalDateTime dateRoute) {
        this.dateRoute = dateRoute;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public List<RouteStop> getRouteStops() {
        return routeStops;
    }

    public void setRouteStops(List<RouteStop> routeStops) {
        this.routeStops = routeStops;
    }

    public Boolean getCanceled() {
        return canceled;
    }

    public void setCanceled(Boolean canceled) {
        this.canceled = canceled;
    }

    public Boolean getOfficeDirection() {
        return officeDirection;
    }

    public void setOfficeDirection(Boolean officeDirection) {
        this.officeDirection = officeDirection;
    }
}
