package com.knikolov.sharearide.service;

import com.knikolov.sharearide.dto.TopUser;
import com.knikolov.sharearide.enums.SortBy;
import com.knikolov.sharearide.models.Route;
import com.knikolov.sharearide.models.RouteStop;

import java.time.LocalDateTime;
import java.util.List;

public interface RouteService {

    List<Route> getAllRoutesByUserAsDriver(String username);

    List<Route> getAllFutureRoutesByUserAsDriver(String name);

    List<Route> getAllRoutesByUserAsPassenger(String username);

    List<Route> getAllFutureRoutesByUserAsPassenger(String name);

    List<Route> getAllRoutes();

    Route addNewRoute(String carId, String addressId, Boolean officeDirection, LocalDateTime date, String name);

    Route updateFutureRoute(String carId, String addressId, String routeId, LocalDateTime date, Boolean officeDirection, String name);

    Route getRouteById(String routeId, Boolean validate, String name);

    List<Route> getLastRoutes(Integer limit, String name);

    Route cancelRoute(String routeId, String name);

    RouteStop saveSeat(String routeId, String addressId, String name);

    Iterable<Route> getRoutes(Integer page, SortBy sort, String filter);

    Iterable<Route> getRoutesBetween(LocalDateTime start, LocalDateTime end, int i, SortBy sortByEnum);

    List<TopUser> getTop15Riders();

    Boolean checkIfPassengerWasInDriverRoute(String driverId, String passengerId);

    Iterable<Route> sortAndFilter(LocalDateTime start, LocalDateTime end, int i, SortBy sortByEnum, Boolean officeDirection);
}
