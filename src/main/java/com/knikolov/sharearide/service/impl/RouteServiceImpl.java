package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.dto.TopUser;
import com.knikolov.sharearide.enums.PassengerEnum;
import com.knikolov.sharearide.enums.SortBy;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.repository.*;
import com.knikolov.sharearide.service.EmailService;
import com.knikolov.sharearide.service.RouteService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final CarRepository carRepository;
    private final AddressRepository addressRepository;
    private final RouteStopRepository routeStopRepository;
    private final RoutePagingAndSortingRepository routePagingAndSortingRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private static int PAGE_SIZE = 5;


    public RouteServiceImpl(RouteRepository routeRepository, CarRepository carRepository,
                            AddressRepository addressRepository, RouteStopRepository routeStopRepository,
                            RoutePagingAndSortingRepository routePagingAndSortingRepository,
                            UserRepository userRepository, EmailService emailService) {
        this.routeRepository = routeRepository;
        this.carRepository = carRepository;
        this.addressRepository = addressRepository;
        this.routeStopRepository = routeStopRepository;
        this.routePagingAndSortingRepository = routePagingAndSortingRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public List<Route> getAllRoutesByUserAsDriver(String username, SortBy sortByEnum) {
        User user = this.userRepository.findByUsername(username);

        if (sortByEnum.equals(SortBy.DATE_ASC)) {
            return this.routeRepository.findAllPastByOrderByIdAscAsDriver(LocalDateTime.now(), user.getId());
        } else if (sortByEnum.equals(SortBy.DATE_DESC)) {
            return this.routeRepository.findAllPastByOrderByIdDescAsDriver(LocalDateTime.now(), user.getId());
        } else {
            return this.routeRepository.findAllPastByUserIdAsDriver(user, LocalDateTime.now());
        }
    }

    @Override
    public List<Route> getAllFutureRoutesByUserAsDriver(String username) {
        User user = this.userRepository.findByUsername(username);
        return this.routeRepository.findAllFutureRoutesByUserIdAsDriver(user, LocalDateTime.now());
    }

    @Override
    public List<Route> getAllRoutesByUserAsPassenger(SortBy sortByEnum, String username) {
        User user = this.userRepository.findByUsername(username);

        if (sortByEnum.equals(SortBy.DATE_ASC)) {
            return this.routeRepository.findAllPastByOrderByIdAscAsPassenger(LocalDateTime.now(), user.getId());
        } else if (sortByEnum.equals(SortBy.DATE_DESC)) {
            return this.routeRepository.findAllPastByOrderByIdDescAsPassenger(LocalDateTime.now(), user.getId());
        } else {
            return this.routeRepository.findAllByUserIdAsPassenger(user, LocalDateTime.now());
        }
    }

    @Override
    public List<Route> getAllFutureRoutesByUserAsPassenger(String name) {
        User user = this.userRepository.findByUsername(name);
        return this.routeRepository.findAllFutureRoutesByUserIdAsPassenger(user, LocalDateTime.now());
    }

    @Override
    public List<Route> getAllRoutes() {
        return this.routeRepository.findAll();
    }

    @Override
    public Route addNewRoute(String carId, String addressId, Boolean officeDirection, LocalDateTime date, String companyAddressId, String username) {
        User user = this.userRepository.findByUsername(username);
        Car car = this.carRepository.findById(carId).orElse(null);
        Address address = this.addressRepository.findById(addressId).orElse(null);

        validateAddNewRoute(user, officeDirection, date);

        if (car != null && address != null) {
            Route route = new Route();
            route.setCar(car);
            route.setDateRoute(date);
            route.setRouteStops(new ArrayList<>());
            route.setOfficeDirection(officeDirection);
            route.setOfficeAddressId(companyAddressId);

            Route savedRoute = this.routeRepository.save(route);

            RouteStop routeStop = new RouteStop();
            routeStop.setUserId(user);
            routeStop.setAddress(address);
            routeStop.setPassengerEnum(PassengerEnum.DRIVER.toString());
            routeStop.setRouteId(savedRoute.getId());
            routeStop.setApproved(true);

            this.routeStopRepository.save(routeStop);
            return savedRoute;
        } else {
            throw new IllegalArgumentException("Something went wrong. Try again later.");
        }
    }

    private void validateAddNewRoute(User user, Boolean officeDirection, LocalDateTime date) {
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
    }

    @Override
    public Route updateFutureRoute(String carId, String addressId, String routeId, LocalDateTime date,
                                   Boolean officeDirection, String officeAddressId, String username) {
        User user = this.userRepository.findByUsername(username);
        if (!this.routeRepository.getOne(routeId).getCar().getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("This route was not found in your profile.");
        }

        Car car = this.carRepository.findById(carId).orElse(null);
        Address address = this.addressRepository.findById(addressId).orElse(null);
        Route route = this.routeRepository.findById(routeId).orElse(null);


        if (car != null && address != null && route != null) {
            route.setCar(car);
            route.setDateRoute(date);
            route.setOfficeDirection(officeDirection);
            route.setOfficeAddressId(officeAddressId);

            RouteStop routeStop = this.routeStopRepository.findByRouteIdAndPassengerEnumEquals(routeId, PassengerEnum.DRIVER.toString());
            routeStop.setAddress(address);
            this.routeStopRepository.save(routeStop);

            return this.routeRepository.save(route);
        } else {
            throw new IllegalArgumentException("Something went wrong. Please try again.");
        }
    }

    @Override
    public Route getRouteById(String routeId, Boolean toValidate, String name) {
        if (toValidate) {
            User user = this.userRepository.findByUsername(name);
            if (this.routeRepository.findAllFutureRoutesByUserIdAsDriver(user,
                    LocalDateTime.now()).stream().filter(r -> r.getCar().getUserId().equals(user.getId())).count() == 0) {
                throw new IllegalArgumentException("Could not find this route in your profile");
            }
        }

        return this.routeRepository.findById(routeId).orElse(null);
    }

    @Override
    public List<Route> getLastRoutes(Integer limit, String name) {
        User user = this.userRepository.findByUsername(name);
        List<Route> routes = this.routeRepository.findAllByOrderByIdDesc(LocalDateTime.now(), user.getId());
        if (routes.size() <= limit) {
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
        User user = this.userRepository.findByUsername(username);
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
                throw new IllegalArgumentException("You are driver for this route. Can not save a seat as a passenger");
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
    public Iterable<Route> getRoutes(Integer page, SortBy sort, String filter, String name) {
        User user = this.userRepository.findByUsername(name);
        if (sort == SortBy.DATE_DESC) {
            return this.routePagingAndSortingRepository.findAllByDateRouteGreaterThanAndCanceledEqualsAndCar_UserIdIsNot(LocalDateTime.now(), false, user.getId(), PageRequest.of(page, PAGE_SIZE, Sort.by("dateRoute").descending()));
        } else if (sort == SortBy.DATE_ASC) {
            return this.routePagingAndSortingRepository.findAllByDateRouteGreaterThanAndCanceledEqualsAndCar_UserIdIsNot(LocalDateTime.now(), false, user.getId(), PageRequest.of(page, PAGE_SIZE, Sort.by("dateRoute").ascending()));
        } else {
            LocalDateTime localDateTime = LocalDateTime.now();
            return this.routePagingAndSortingRepository.findAllByDateRouteGreaterThanAndCanceledEqualsAndCar_UserIdIsNot(localDateTime, false, user.getId(), PageRequest.of(page, PAGE_SIZE));
        }

    }

    @Override
    public Iterable<Route> getRoutesBetween(LocalDateTime start, LocalDateTime end, int page, SortBy sort, String name, String officeAddressId) {
        User user = this.userRepository.findByUsername(name);
        if (sort == SortBy.DATE_DESC) {
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(start, end, false, user.getId(), PageRequest.of(page, PAGE_SIZE, Sort.by("dateRoute").descending()));
        } else if (sort == SortBy.DATE_ASC) {
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(start, end, false, user.getId(), PageRequest.of(page, PAGE_SIZE, Sort.by("dateRoute").ascending()));
        } else {
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(start, end, false, user.getId(), PageRequest.of(page, PAGE_SIZE));
        }
    }

    @Override
    public List<TopUser> getTop15Riders() {
        List<TopUser> topUsers = getTopUsers();

        Collections.sort(topUsers);

        if (topUsers.size() < 15) {
            return topUsers;
        }
        return topUsers.subList(0, 14);
    }

    @Override
    public List<TopUser> getTop15RidersByDrives() {
        List<TopUser> topUsers = getTopUsers();

        Comparator<TopUser> compareByDrives = (TopUser o1, TopUser o2) ->
                o2.getNumberRides().compareTo( o1.getNumberRides() );

        topUsers.sort(compareByDrives);

        if (topUsers.size() < 15) {
            return topUsers;
        }
        return topUsers.subList(0, 14);
    }

    @Override
    public List<TopUser> getTop15RidersByRating() {
        List<TopUser> topUsers = getTopUsers();

        Comparator<TopUser> compareByDrives = (TopUser o1, TopUser o2) ->
                o2.getRating().compareTo( o1.getRating());

        topUsers.sort(compareByDrives);

        if (topUsers.size() < 15) {
            return topUsers;
        }
        return topUsers.subList(0, 14);
    }

    private List<TopUser> getTopUsers() {
        List<TopUser> topUsers = new ArrayList<>();
        List<User> drivers = this.userRepository.findAll().stream().filter(User::isDriver).collect(Collectors.toList());
        for (User driver : drivers) {
            List<Route> userRoutes = this.routeRepository.findAllByUserIdAsDriver(driver);
            int passengers = getAllPassengersCount(userRoutes);
            Double rating = getRating(driver);
            topUsers.add(new TopUser(driver, userRoutes.size(), passengers, rating));
        }
        return topUsers;
    }

    private Double getRating(User driver) {
        List<Rating> ratings = driver.getRatings();
        double cnt = 0;
        for (Rating rating : ratings) {
            cnt += rating.getRate();
        }
        if (ratings.size() == 0) {
            return (double) 0;
        }
        return cnt / ratings.size();
    }

    private int getAllPassengersCount(List<Route> userRoutes) {
        int passengers = 0;
        for (Route userRoute : userRoutes) {
            passengers += getPassengersInRoute(userRoute);
        }

        return passengers;
    }

    private int getPassengersInRoute(Route route) {
        return route.getRouteStops().size() - 1;
    }

    @Override
    public Iterable<Route> sortAndFilter(LocalDateTime start, LocalDateTime end, int page, SortBy sort, Boolean officeDirection, String name, String officeAddressId) {
        User user = this.userRepository.findByUsername(name);
        if (sort == SortBy.DATE_DESC) {
            if (officeDirection == null) {
                return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(start, end, false, user.getId(), PageRequest.of(page, PAGE_SIZE, Sort.by("dateRoute").descending()));
            }
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEqualsAndOfficeDirectionEqualsAndCar_UserIdIsNotAndOfficeAddressIdEquals(start, end, false, officeDirection, user.getId(), officeAddressId, PageRequest.of(page, PAGE_SIZE, Sort.by("dateRoute").descending()));
        } else if (sort == SortBy.DATE_ASC) {
            if (officeDirection == null) {
                return this.routePagingAndSortingRepository
                        .findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(
                                start.plusHours(LocalDateTime.now().getHour()),
                                end,
                                false,
                                user.getId(),
                                PageRequest.of(page, PAGE_SIZE, Sort.by("dateRoute").ascending()));
            }
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEqualsAndOfficeDirectionEqualsAndCar_UserIdIsNotAndOfficeAddressIdEquals(start, end, false, officeDirection, user.getId(), officeAddressId, PageRequest.of(page, PAGE_SIZE, Sort.by("dateRoute").ascending()));
        } else {
            if (officeDirection == null) {
                return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(start, end, false, user.getId(), PageRequest.of(page, PAGE_SIZE));
            }
            return this.routePagingAndSortingRepository.findAllByDateRouteBetweenAndCanceledEqualsAndOfficeDirectionEqualsAndCar_UserIdIsNotAndOfficeAddressIdEquals(start, end, false, officeDirection, user.getId(), officeAddressId, PageRequest.of(page, PAGE_SIZE));
        }
    }

}
