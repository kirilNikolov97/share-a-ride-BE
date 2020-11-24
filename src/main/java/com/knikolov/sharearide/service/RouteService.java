package com.knikolov.sharearide.service;

import com.knikolov.sharearide.dto.TopUser;
import com.knikolov.sharearide.enums.SortBy;
import com.knikolov.sharearide.models.Route;
import com.knikolov.sharearide.models.RouteStop;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for Route actions
 */
public interface RouteService {

    List<Route> getAllPastNotCanceledRoutesWhereUserIsDriver(String username, SortBy sortByEnum);

    List<Route> getAllFutureNotCanceledRoutesWhereUserIsDriver(String name);

    List<Route> getAllPastNotCanceledRoutesWhereUserIsPassenger(SortBy sortByEnum, String username);

    List<Route> getAllFutureNotCanceledRoutesWhereUserIsPassenger(String name);

    List<Route> getAllRoutes();

    Route insert(String carId, String addressId, Boolean officeDirection, LocalDateTime date, String companyAddressId, String name);

    Route update(String carId, String addressId, String routeId, LocalDateTime date, Boolean officeDirection, String officeAddressId, String name);

    Route getById(String routeId, Boolean validate, String name);

    List<Route> getLastRoutes(Integer limit, String name);

    Route cancelRoute(String routeId, String name);

    RouteStop saveSeat(String routeId, String addressId, String name);

    Iterable<Route> getFutureNotCanceledRoutes(Integer page, SortBy sort, String filter, String name);

    Iterable<Route> getNotCanceledRoutesBetweenDatesExcludingUserRoutes(LocalDateTime start, LocalDateTime end, int i, SortBy sortByEnum, String name, String officeAddressId);

    List<TopUser> getTop15RidersByNumberOfPassengers();

    Iterable<Route> sortAndFilter(LocalDateTime start, LocalDateTime end, int i, SortBy sortByEnum, Boolean officeDirection, String name, String officeAddressId);

    List<TopUser> getTop15RidersByNumberOfDrives();

    List<TopUser> getTop15RidersByRating();
}
