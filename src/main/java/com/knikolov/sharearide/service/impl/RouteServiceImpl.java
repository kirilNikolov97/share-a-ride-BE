package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.dto.TopUser;
import com.knikolov.sharearide.enums.PassengerEnum;
import com.knikolov.sharearide.enums.SortBy;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.repository.*;
import com.knikolov.sharearide.service.CarService;
import com.knikolov.sharearide.service.EmailService;
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
    private final EmailService emailService;


    @Autowired
    public RouteServiceImpl(RouteRepository routeRepository, UserService userService, CarService carService,
                            AddressRepository addressRepository, RouteStopRepository routeStopRepository,
                            RoutePagingAndSortingRepository routePagingAndSortingRepository,
                            UserRepository userRepository, EmailService emailService) {
        this.routeRepository = routeRepository;
        this.userService = userService;
        this.carService = carService;
        this.addressRepository = addressRepository;
        this.routeStopRepository = routeStopRepository;
        this.routePagingAndSortingRepository = routePagingAndSortingRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
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

        if (!user.isDriver()) {
            throw new IllegalArgumentException("Can not add new route. You are not a driver.");
        }

        List<Route> futureRoutes = this.routeRepository.findAllFutureRoutesByUserIdAsDriver(user, LocalDateTime.now());
        for (Route futureRoute : futureRoutes) {
            if (futureRoute.getOfficeDirection() == officeDirection &&
                    futureRoute.getDateRoute().getYear() == date.getYear() &&
                    futureRoute.getDateRoute().getDayOfYear() == date.getDayOfYear()) {
                throw new IllegalArgumentException("You already have a route for this day and this direction");
            }
        }


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
        User user = this.userRepository.findByUsername(username);
        if (!this.routeRepository.getOne(routeId).getCar().getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("This route was not found in your profile.");
        }

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
    public Route getRouteById(String routeId, Boolean validate, String name) {
        if (validate) {
            User user = this.userRepository.findByUsername(name);
            if (this.routeRepository.findAllFutureRoutesByUserIdAsDriver(user, LocalDateTime.now()).stream().filter(r -> r.getCar().getUserId().equals(user.getId())).count() == 0) {
                throw new IllegalArgumentException("Could not find this route in your profile");
            }

        }
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
    public Route cancelRoute(String routeId, String driverName) {
        Route routeToCancel = this.routeRepository.findById(routeId).orElse(null);
        User driver = this.userRepository.findByUsername(driverName);

        if (routeToCancel != null) {
            if (!routeToCancel.getCar().getUserId().equals(driver.getId())) {
                throw new IllegalArgumentException("This route is not present in your profile");
            }

            routeToCancel.setCanceled(true);
            Route savedRoute = this.routeRepository.save(routeToCancel);

            this.emailService.sendEmailsForCanceledRoute(routeId, driverName);
            return savedRoute;
        } else {
            throw new IllegalArgumentException("Something went wrong. Try again later");
        }
    }

    @Override
    public RouteStop saveSeat(String routeId, String addressId, String username) {
        Address address = this.addressRepository.findById(addressId).orElse(null);
        User user = this.userService.getUserByUsername(username);
        Route route = this.routeRepository.findById(routeId).orElse(null);

        if (address != null && user != null && route != null) {
            if (user.getAddresses().stream().noneMatch(addrs -> addrs.getId().equals(addressId))) {
                throw new IllegalArgumentException("This address is not assigned to this user");
            }
            if (route.getDateRoute().compareTo(LocalDateTime.now()) <= 0) {
                throw new IllegalArgumentException("This route is already passed. You can not save a seat for this route");
            }
            if (route.getCar().getSeats() - route.getRouteStops().size() < 1) {
                throw new IllegalArgumentException("No free seats in the car.");
            }
            if (route.getCanceled()) {
                throw new IllegalArgumentException("Route is canceled. You can not save a seat for this route");
            }
            if (route.getRouteStops().stream().anyMatch(rs -> rs.getPassengerEnum().equals("DRIVER") && rs.getUserId().getId().equals(user.getId()))) {
                throw new IllegalArgumentException("You are driver. Can not save a seat as a passenger");
            }
            if (route.getRouteStops().stream().anyMatch(rs -> rs.getUserId().getId().equals(user.getId()))) {
                throw new IllegalArgumentException("You already saved a seat for this route.");
            }

            RouteStop routeStop = new RouteStop();

            routeStop.setAddress(address);
            routeStop.setPassengerEnum("PASSENGER");
            routeStop.setRouteId(routeId);
            routeStop.setUserId(user);
            routeStop.setApproved(false);

            RouteStop savedRouteStop = this.routeStopRepository.save(routeStop);
            this.emailService.sendEmailForSavingASeat(route.getCar().getUserId(), username);

            return savedRouteStop;
        }
        throw new IllegalArgumentException("Something went wrong. Try again later");
    }

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
        int stops = 0;

        for(int i = 0; i < routeStops.size(); i++) {
            if(i == 0) {
                if (!this.routeRepository.findById(routeStops.get(i).getRouteId()).get().getCanceled()) { // if not canceled
                    cnt++;
                    stops += this.routeStopRepository.findAllByRouteId(routeStops.get(i).getRouteId()).size();
                }
                continue;
            }
            if(routeStops.get(i-1).getUserId().getId()
                    .equals(routeStops.get(i).getUserId().getId())) {
                if (!this.routeRepository.findById(routeStops.get(i).getRouteId()).get().getCanceled()) { // if not canceled
                    cnt++;
                    stops += this.routeStopRepository.findAllByRouteId(routeStops.get(i).getRouteId()).size();
                }
                if(i == routeStops.size() - 1) {
                    User u = this.userRepository.findById(routeStops.get(i).getUserId().getId()).orElse(null);
                    TopUser topUser = new TopUser(u, cnt, stops - cnt);
                    topUsers.add(topUser);
                }
            } else {
                User u = this.userRepository.findById(routeStops.get(i-1).getUserId().getId()).orElse(null);
                if (!this.routeRepository.findById(routeStops.get(i).getRouteId()).get().getCanceled()) { // if not canceled
                    stops += this.routeStopRepository.findAllByRouteId(routeStops.get(i - 1).getRouteId()).size();
                    cnt++;
                }
                TopUser topUser = new TopUser(u, cnt, stops - cnt);
                topUsers.add(topUser);

                if(i == routeStops.size() - 1) {
                    User lastUser = this.userRepository.findById(routeStops.get(i).getUserId().getId()).orElse(null);
                    TopUser lastTopUser = new TopUser(lastUser, cnt, stops - cnt);
                    topUsers.add(lastTopUser);
                }
                stops = 0;
                cnt = 0;
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
