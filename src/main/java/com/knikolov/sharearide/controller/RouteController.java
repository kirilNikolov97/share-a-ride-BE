package com.knikolov.sharearide.controller;

import com.knikolov.sharearide.dto.TopUser;
import com.knikolov.sharearide.enums.SortBy;
import com.knikolov.sharearide.models.Route;
import com.knikolov.sharearide.models.RouteStop;
import com.knikolov.sharearide.service.EmailService;
import com.knikolov.sharearide.service.RouteService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class RouteController {

    private final RouteService routeService;
    private final EmailService emailService;

    public RouteController(RouteService routeService, EmailService emailService) {
        this.routeService = routeService;
        this.emailService = emailService;
    }

    @RequestMapping(value = "/routesAsDriver", method = RequestMethod.GET)
    List<Route> getRoutesByUsernameAsDriver(@RequestParam String username, @RequestParam String sortBy,
            @RequestParam Integer limit, Principal principal) {

        SortBy sortByEnum;
        if (sortBy.equals("date_desc")) {
            sortByEnum = SortBy.DATE_DESC;
        } else if (sortBy.equals("date_asc")) {
            sortByEnum = SortBy.DATE_ASC;
        } else {
            sortByEnum = SortBy.NONE;
        }

        if ("".equals(username)) {
            List<Route> routes = this.routeService.getAllRoutesByUserAsDriver(principal.getName(), sortByEnum);
            if (limit == -1) {
                return routes;
            } else if (routes.size() <= limit) {
                return routes;
            } else {
                return routes.subList(0, limit);
            }
        }
        List<Route> routes = this.routeService.getAllRoutesByUserAsDriver(username, sortByEnum);
        if (limit == -1) {
            return routes;
        } else if (routes.size() <= limit) {
            return routes;
        } else {
            return routes.subList(0, limit);
        }
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
    List<Route> getRoutesByUsernameAsPassenger(@RequestParam String sortBy, @RequestParam Integer limit, Principal principal) {

        SortBy sortByEnum;
        if (sortBy.equals("date_desc")) {
            sortByEnum = SortBy.DATE_DESC;
        } else if (sortBy.equals("date_asc")) {
            sortByEnum = SortBy.DATE_ASC;
        } else {
            sortByEnum = SortBy.NONE;
        }

        List<Route> routes = this.routeService.getAllRoutesByUserAsPassenger(sortByEnum, principal.getName());
        if (limit == -1) {
            return routes;
        } else if (routes.size() <= limit) {
            return routes;
        } else {
            return routes.subList(0, limit);
        }
    }

    @RequestMapping(value = "/route/{routeId}", method = RequestMethod.GET)
    Route getRouteById(@PathVariable String routeId, @RequestParam Boolean validate, Principal principal) {
        return this.routeService.getRouteById(routeId, validate, principal.getName());
    }

    @RequestMapping(value = "/route", method = RequestMethod.POST)
    Route addNewRoute(@RequestParam String carId, @RequestParam String addressId,
                      @RequestParam Boolean officeDirection, @RequestParam String companyAddressId,
                      @RequestBody LocalDateTime date, Principal principal) {
        validateRoute(carId, addressId, officeDirection, date);

        // TODO: check if correct
        date = date.plusHours(3);
        return this.routeService.addNewRoute(carId, addressId, officeDirection, date, companyAddressId, principal.getName());
    }

    @RequestMapping(value = "/route", method = RequestMethod.PATCH)
    Route updateFutureRoute(@RequestParam String carId, @RequestParam String addressId, @RequestParam String routeId,
                            @RequestParam Boolean officeDirection, @RequestParam String officeAddressId,
                            @RequestBody LocalDateTime date, Principal principal) {
        validateRoute(carId, addressId, officeDirection, date);

        return this.routeService.updateFutureRoute(carId, addressId, routeId, date, officeDirection, officeAddressId, principal.getName());
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
    List<Route> getRoutes(@RequestParam Integer currPage, @RequestParam String sortBy,
                          @RequestParam String filter, Principal principal) {
        SortBy sortByEnum;
        if (sortBy.equals("date_desc")) {
            sortByEnum = SortBy.DATE_DESC;
        } else if (sortBy.equals("date_asc")) {
            sortByEnum = SortBy.DATE_ASC;
        } else {
            sortByEnum = SortBy.NONE;
        }

        List<Route> routes = new ArrayList<>();
        this.routeService.getRoutes(currPage - 1, sortByEnum, filter, principal.getName()).forEach(routes::add);
        return routes;
    }

    @RequestMapping(value = "/route/betweenDates", method = RequestMethod.GET)
    List<Route> getRoutesBetweenDates(@RequestParam Integer currPage, @RequestParam String sortBy,
                                      @RequestParam String filter, @RequestParam String startDate,
                                      @RequestParam String endDate, @RequestParam String officeAddressId, Principal principal) {
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
        this.routeService.getRoutesBetween(start, end, currPage-1, sortByEnum, principal.getName(), officeAddressId).forEach(routes::add);
        return routes;
    }

    @RequestMapping(value = "/top15Users", method = RequestMethod.GET)
    List<TopUser> getTop15Users() {
        return this.routeService.getTop15Riders();
    }

    @RequestMapping(value = "/top15UsersByDrives", method = RequestMethod.GET)
    List<TopUser> getTop15UsersByDrives() {
        return this.routeService.getTop15RidersByDrives();
    }

    @RequestMapping(value = "/top15UsersByRating", method = RequestMethod.GET)
    List<TopUser> getTop15RidersByRating() {
        return this.routeService.getTop15RidersByRating();
    }

    @RequestMapping(value = "route/sortAndFilter", method = RequestMethod.GET)
    List<Route> getSortAndFilterRoutes(@RequestParam Integer currPage, @RequestParam String sortBy,
                                       @RequestParam String filter, @RequestParam String startDate,
                                       @RequestParam String endDate, @RequestParam Boolean officeDirection,
                                       @RequestParam String officeAddressId,
                                       Principal principal) {

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

        if ("".equals(officeAddressId)) {
            officeDirection = null;
        }

        List<Route> routes = new ArrayList<>();
        this.routeService.sortAndFilter(start, end, currPage-1, sortByEnum, officeDirection, principal.getName(), officeAddressId).forEach(routes::add);
        return routes;
    }

    @RequestMapping(value = "/allRoutes", method = RequestMethod.GET)
    List<Route> getAllRoutes() {
        return this.routeService.getAllRoutes();
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
