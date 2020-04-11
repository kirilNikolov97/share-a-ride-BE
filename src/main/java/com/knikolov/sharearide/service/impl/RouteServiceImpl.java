package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.dto.TopUser;
import com.knikolov.sharearide.enums.PassengerEnum;
import com.knikolov.sharearide.enums.SortBy;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.repository.*;
import com.knikolov.sharearide.service.CarService;
import com.knikolov.sharearide.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final UserService userService;
    private final CarService carService;
    private final AddressRepository addressRepository;
    private final RouteStopRepository routeStopRepository;
    private final RoutePagingAndSortingRepository routePagingAndSortingRepository;
    private final UserRepository userRepository;


    @Autowired
    public RouteServiceImpl(RouteRepository routeRepository, UserService userService, CarService carService, AddressRepository addressRepository, RouteStopRepository routeStopRepository, RoutePagingAndSortingRepository routePagingAndSortingRepository, UserRepository userRepository) {
        this.routeRepository = routeRepository;
        this.userService = userService;
        this.carService = carService;
        this.addressRepository = addressRepository;
        this.routeStopRepository = routeStopRepository;
        this.routePagingAndSortingRepository = routePagingAndSortingRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Route> getAllRoutesByUserAsDriver(String username) {
        User user = this.userService.getUserByUsername(username);
        List<Route> routes = this.routeRepository.findAllByUserIdAsDriver(user, LocalDateTime.now());
        System.out.println("\n\n\n\n ROUTES SIZE " + routes.size());
        return routes;
    }

    @Override
    public List<Route> getAllFutureRoutesByUserAsDriver(String username) {
        User user = this.userService.getUserByUsername(username);
        List<Route> routes = this.routeRepository.findAllFutureRoutesByUserIdAsDriver(user, LocalDateTime.now());
        return routes;
    }

    @Override
    public List<Route> getAllRoutesByUserAsPassenger(String username) {
        User user = this.userService.getUserByUsername(username);
        return this.routeRepository.findAllByUserIdAsPassenger(user, LocalDateTime.now());
    }

    @Override
    public List<Route> getAllFutureRoutesByUserAsPassenger(String name) {
        User user = this.userService.getUserByUsername(name);
        return this.routeRepository.findAllFutureRoutesByUserIdAsPassenger(user, LocalDateTime.now());
    }

    @Override
    public List<Route> getAllRoutes() {
        return null;
    }

    @Override
    public Route addNewRoute(String carId, String addressId, Boolean officeDirection, LocalDateTime date, String name) {
        User user = this.userService.getUserByUsername(name);
        Car car = this.carService.getCarById(carId);
        Address address = this.addressRepository.findById(addressId).orElse(null);

        if(car != null && address != null) {
            Route route = new Route();
            route.setCar(car);
            route.setDateRoute(date);
            route.setRouteStops(new ArrayList<>());
            route.setOfficeDirection(officeDirection);

            Route savedRoute = this.routeRepository.save(route);

            RouteStop routeStop = new RouteStop();
            routeStop.setUserId(user);
            routeStop.setAddress(address);
            routeStop.setPassengerEnum(PassengerEnum.DRIVER.toString());
            routeStop.setRouteId(savedRoute.getId());
            routeStop.setApproved(true);

            this.routeStopRepository.save(routeStop);
            return route;
        } else {
            throw new IllegalArgumentException("Something went wrong. Try again later.");
        }
    }

    @Override
    public Route updateFutureRoute(String carId, String addressId, String routeId, LocalDateTime date, Boolean officeDirection, String username) {
        Car car = this.carService.getCarById(carId);
        Address address = this.addressRepository.findById(addressId).orElse(null);
        Route route = this.routeRepository.findById(routeId).orElse(null);

        if(car != null && address != null && route != null) {
            route.setCar(car);
            route.setDateRoute(date);
            route.setOfficeDirection(officeDirection);

            RouteStop routeStop = this.routeStopRepository.findByRouteIdAndPassengerEnumEquals(routeId, PassengerEnum.DRIVER.toString());
            routeStop.setAddress(address);
            this.routeStopRepository.save(routeStop);

            return this.routeRepository.save(route);
        } else {
            throw new IllegalArgumentException("Something went wrong. Please try again.");
        }
    }

    @Override
    public Route getRouteById(String routeId, String name) {
        Route route = this.routeRepository.findById(routeId).orElse(null);
        if(route != null) {
            List<RouteStop> stops = this.routeStopRepository.findAllByRouteId(routeId);
            route.setRouteStops(stops);
            return route;
        }
        return null;
    }

    @Override
    public List<Route> getLastRoutes(Integer limit, String name) {
        User user = this.userService.getUserByUsername(name);
        List<Route> routes = this.routeRepository.findAllByOrderByIdDesc(LocalDateTime.now(), user.getId());
        if(routes.size() <= limit) {
            return routes;
        } else {
            return routes.subList(0, limit);
        }
    }

    @Override
    public Route cancelRoute(String routeId, String name) {
        Route routeToCancel = this.routeRepository.findById(routeId).orElse(null);
        routeToCancel.setCanceled(true);
        return this.routeRepository.save(routeToCancel);
    }

    @Override
    public Route saveSeat(String routeId, String addressId, String username) {
        RouteStop routeStop = new RouteStop();
        Address address = this.addressRepository.findById(addressId).orElse(null);
        User user = this.userService.getUserByUsername(username);

        if(address != null && user != null) {
            routeStop.setAddress(address);
            routeStop.setPassengerEnum("PASSENGER");
            routeStop.setRouteId(routeId);
            routeStop.setUserId(user);
            routeStop.setApproved(false);

            this.routeStopRepository.save(routeStop);
        }

        return this.routeRepository.findById(routeId).orElse(null);    }

    @Override
    public Iterable<Route> getRoutes(Integer page, SortBy sort, String filter) {
        if (sort == SortBy.DATE_DESC) {
            return this.routePagingAndSortingRepository.findAllByDateRouteGreaterThanAndCanceledEquals(LocalDateTime.now(), false, PageRequest.of(page, 5, Sort.by("dateRoute").descending()));
        } else if (sort == SortBy.DATE_ASC) {
            return this.routePagingAndSortingRepository.findAllByDateRouteGreaterThanAndCanceledEquals(LocalDateTime.now(), false, PageRequest.of(page, 5, Sort.by("dateRoute").ascending()));
        } else {
            LocalDateTime localDateTime = LocalDateTime.now();
            return this.routePagingAndSortingRepository.findAllByDateRouteGreaterThanAndCanceledEquals(localDateTime, false, PageRequest.of(page, 5));
        }

    }

    @Override
    public Iterable<Route> getRoutesBetween(LocalDateTime start, LocalDateTime end, int page, SortBy sort) {
        if (sort == SortBy.DATE_DESC) {
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEquals(start.plusHours(LocalDateTime.now().getHour()), end, false, PageRequest.of(page, 5, Sort.by("dateRoute").descending()));
        } else if (sort == SortBy.DATE_ASC) {
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEquals(start.plusHours(LocalDateTime.now().getHour()), end, false, PageRequest.of(page, 5, Sort.by("dateRoute").ascending()));
        } else {
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEquals(start.plusHours(LocalDateTime.now().getHour()), end, false,  PageRequest.of(page, 5));
        }
    }

    @Override
    public List<TopUser> getTop15Riders() {
        List<RouteStop> routeStops = this.routeStopRepository.findAllByPassengerEnumEqualsOrderByUserId(PassengerEnum.DRIVER.toString());
        List<TopUser> topUsers = new ArrayList<>();
        int cnt = 0;

        for(int i = 0; i < routeStops.size(); i++) {
            if(i == 0) {
                cnt++;
                continue;
            }
            if(routeStops.get(i-1).getUserId() == routeStops.get(i).getUserId()) {
                cnt++;
                if(i == routeStops.size() - 1) {
                    User u = this.userRepository.findById(routeStops.get(i).getUserId().getId()).orElse(null);
                    TopUser topUser = new TopUser(u, cnt);
                    topUsers.add(topUser);
                }
            } else {
                User u = this.userRepository.findById(routeStops.get(i-1).getUserId().getId()).orElse(null);
                TopUser topUser = new TopUser(u, cnt);
                topUsers.add(topUser);
                cnt = 0;
                if(i == routeStops.size() - 1) {
                    User lastUser = this.userRepository.findById(routeStops.get(i).getUserId().getId()).orElse(null);
                    TopUser lastTopUser = new TopUser(lastUser, cnt);
                    topUsers.add(lastTopUser);
                }
            }
        }

        Collections.sort(topUsers);

        return topUsers;
    }

    @Override
    public Boolean checkIfPassengerWasInDriverRoute(String driver, String passenger) {
        List<String> driverRouteStops = this.routeStopRepository
                .findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals("DRIVER", driver);

        List<String> passengerRouteStops = this.routeStopRepository
                .findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals("PASSENGER", passenger);

        int i = 0;
        //TODO
        return false;
    }

    @Override
    public Iterable<Route> sortAndFilter(LocalDateTime start, LocalDateTime end, int page, SortBy sort, Boolean officeDirection) {
        if (sort == SortBy.DATE_DESC) {
            if (officeDirection == null) {
                return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEquals(start.plusHours(LocalDateTime.now().getHour()), end, false, PageRequest.of(page, 5, Sort.by("dateRoute").descending()));
            }
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEqualsAndOfficeDirectionEquals(start.plusHours(LocalDateTime.now().getHour()), end, false, officeDirection, PageRequest.of(page, 5, Sort.by("dateRoute").descending()));
        } else if (sort == SortBy.DATE_ASC) {
            if (officeDirection == null) {
                return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEquals(start.plusHours(LocalDateTime.now().getHour()), end, false, PageRequest.of(page, 5, Sort.by("dateRoute").ascending()));
            }
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEqualsAndOfficeDirectionEquals(start.plusHours(LocalDateTime.now().getHour()), end, false, officeDirection, PageRequest.of(page, 5, Sort.by("dateRoute").ascending()));
        } else {
            if (officeDirection == null) {
                return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEquals(start.plusHours(LocalDateTime.now().getHour()), end, false,  PageRequest.of(page, 5));
            }
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEqualsAndOfficeDirectionEquals(start.plusHours(LocalDateTime.now().getHour()), end, false, officeDirection, PageRequest.of(page, 5));
        }
    }

}
