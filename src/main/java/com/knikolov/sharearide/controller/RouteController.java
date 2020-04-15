package com.knikolov.sharearide.controller;

import com.knikolov.sharearide.dto.TopUser;
import com.knikolov.sharearide.enums.SortBy;
import com.knikolov.sharearide.models.Route;
import com.knikolov.sharearide.models.RouteStop;
import com.knikolov.sharearide.service.EmailService;
import com.knikolov.sharearide.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
public class RouteController {

    private final RouteService routeService;
    private final EmailService emailService;

    @Autowired
    public RouteController(RouteService routeService, EmailService emailService) {
        this.routeService = routeService;
        this.emailService = emailService;
    }

    @RequestMapping(value = "/routesAsDriver", method = RequestMethod.GET)
    List<Route> getRoutesByUsernameAsDriver(@RequestParam String username, Principal principal) {
        if ("".equals(username)) {
            return this.routeService.getAllRoutesByUserAsDriver(principal.getName());
        }
        return this.routeService.getAllRoutesByUserAsDriver(username);
    }

    @RequestMapping(value = "/futureRoutesAsDriver", method = RequestMethod.GET)
    List<Route> getFutureRoutesByUsernameAsDriver(@RequestParam String username, Principal principal) {
        if ("".equals(username)) {
            return this.routeService.getAllFutureRoutesByUserAsDriver(principal.getName());
        }

        return this.routeService.getAllFutureRoutesByUserAsDriver(username);
    }

    @RequestMapping(value = "/futureRoutesAsPassenger", method = RequestMethod.GET)
    List<Route> getFutureRoutesByUsernameAsPassenger(Principal principal) {
        return this.routeService.getAllFutureRoutesByUserAsPassenger(principal.getName());
    }

    @RequestMapping(value = "/routesAsPassenger", method = RequestMethod.GET)
    List<Route> getRoutesByUsernameAsPassenger(Principal principal) {
        return this.routeService.getAllRoutesByUserAsPassenger(principal.getName());
    }

    @RequestMapping(value = "/route/{routeId}", method = RequestMethod.GET)
    Route getRouteById(@PathVariable String routeId, @RequestParam Boolean validate, Principal principal) {
        return this.routeService.getRouteById(routeId, validate, principal.getName());
    }

    @RequestMapping(value = "/route", method = RequestMethod.POST)
    Route addNewRoute(@RequestParam String carId, @RequestParam String addressId, @RequestParam Boolean officeDirection,
                      @RequestBody LocalDateTime date, Principal principal) {
        validateRoute(carId, addressId, officeDirection, date);

        emailService.sendTestEmail();
        // TODO: check if correct
        date = date.plusHours(3);
        return this.routeService.addNewRoute(carId, addressId, officeDirection, date, principal.getName());
    }

    @RequestMapping(value = "/route", method = RequestMethod.PATCH)
    Route updateFutureRoute(@RequestParam String carId, @RequestParam String addressId, @RequestParam String routeId,
                            @RequestParam Boolean officeDirection, @RequestBody LocalDateTime date, Principal principal) {
        validateRoute(carId, addressId, officeDirection, date);

        return this.routeService.updateFutureRoute(carId, addressId, routeId, date, officeDirection, principal.getName());
    }

    @RequestMapping(value = "/cancelRoute", method = RequestMethod.PATCH)
    Route cancelRoute(@RequestParam String routeId, Principal principal) {
        return this.routeService.cancelRoute(routeId, principal.getName());
    }

    @RequestMapping(value = "/lastRoutes", method = RequestMethod.GET)
    List<Route> getLastRoutes(@RequestParam Integer limit, Principal principal) {
        return this.routeService.getLastRoutes(limit, principal.getName());
    }

    @RequestMapping(value = "/saveSeat/{routeId}", method = RequestMethod.GET)
    RouteStop saveSeat(@PathVariable String routeId, @RequestParam String addressId, Principal principal) {
        return this.routeService.saveSeat(routeId, addressId, principal.getName());
    }

    @RequestMapping(value = "/route/allRoutes", method = RequestMethod.GET)
    List<Route> getRoutes(@RequestParam Integer currPage, @RequestParam String sortBy, @RequestParam String filter) {
        SortBy sortByEnum;
        if (sortBy.equals("date_desc")) {
            sortByEnum = SortBy.DATE_DESC;
        } else if (sortBy.equals("date_asc")) {
            sortByEnum = SortBy.DATE_ASC;
        } else {
            sortByEnum = SortBy.NONE;
        }

        List<Route> routes = new ArrayList<>();
        this.routeService.getRoutes(currPage - 1, sortByEnum, filter).forEach(routes::add);
        return routes;
    }

    @RequestMapping(value = "/route/betweenDates", method = RequestMethod.GET)
    List<Route> getRoutesBetweenDates(@RequestParam Integer currPage, @RequestParam String sortBy,
                                      @RequestParam String filter, @RequestParam String startDate, @RequestParam String endDate) {
        LocalDateTime start = Instant.ofEpochMilli(Long.parseLong(startDate)).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = Instant.ofEpochMilli(Long.parseLong(endDate)).atZone(ZoneId.systemDefault()).toLocalDateTime();

        SortBy sortByEnum;
        if (sortBy.equals("date_desc")) {
            sortByEnum = SortBy.DATE_DESC;
        } else if (sortBy.equals("date_asc")) {
            sortByEnum = SortBy.DATE_ASC;
        } else {
            sortByEnum = SortBy.NONE;
        }

        List<Route> routes = new ArrayList<>();
        this.routeService.getRoutesBetween(start, end, currPage-1, sortByEnum).forEach(routes::add);
        return routes;
    }

    @RequestMapping(value = "/top15Users", method = RequestMethod.GET)
    List<TopUser> getTop15Users() {
        List<TopUser> y = this.routeService.getTop15Riders();
        return y;
    }

    @RequestMapping(value = "route/sortAndFilter", method = RequestMethod.GET)
    List<Route> getSortAndFilterRoutes(@RequestParam Integer currPage, @RequestParam String sortBy,
                                       @RequestParam String filter, @RequestParam String startDate,
                                       @RequestParam String endDate, @RequestParam Boolean officeDirection) {

        LocalDateTime start = Instant.ofEpochMilli(Long.parseLong(startDate)).atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = Instant.ofEpochMilli(Long.parseLong(endDate)).atZone(ZoneId.systemDefault()).toLocalDateTime();

        SortBy sortByEnum;
        if (sortBy.equals("date_desc")) {
            sortByEnum = SortBy.DATE_DESC;
        } else if (sortBy.equals("date_asc")) {
            sortByEnum = SortBy.DATE_ASC;
        } else {
            sortByEnum = SortBy.NONE;
        }

        List<Route> routes = new ArrayList<>();
        this.routeService.sortAndFilter(start, end, currPage-1, sortByEnum, officeDirection).forEach(routes::add);
        return routes;
    }


    private void validateRoute(String carId, String addressId, Boolean officeDirection, LocalDateTime date) {
        if (carId == null || "".equals(carId.trim())) {
            throw new IllegalArgumentException("Car is not selected.");
        } else if (addressId == null || "".equals(addressId.trim())) {
            throw new IllegalArgumentException("Address is not selected.");
        } else if (officeDirection == null) {
            throw new IllegalArgumentException("Office direction is not selected.");
        } else if (date == null) {
            throw new IllegalArgumentException("Date is not selected.");
        }
    }

}
