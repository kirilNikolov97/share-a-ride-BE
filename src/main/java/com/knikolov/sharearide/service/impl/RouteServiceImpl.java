package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.dto.TopUser;
import com.knikolov.sharearide.enums.PassengerEnum;
import com.knikolov.sharearide.enums.SortBy;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.repository.*;
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

/**
 * RouteService implementation
 */
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
    public List<Route> getAllPastNotCanceledRoutesWhereUserIsDriver(String username, SortBy sortByEnum) {
        User user = userRepository.findByUsername(username);

        if (sortByEnum.equals(SortBy.DATE_ASC)) {
            return routeRepository.findAllPastOrderByIdAscAsDriverAndIsNotCanceled(LocalDateTime.now(), user.getId());
        } else if (sortByEnum.equals(SortBy.DATE_DESC)) {
            return routeRepository.findAllPastOrderByIdDescAsDriverAndIsNotCanceled(LocalDateTime.now(), user.getId());
        } else {
            return routeRepository.findAllPastByUserAsDriverAndIsNotCanceled(user, LocalDateTime.now());
        }
    }

    @Override
    public List<Route> getAllFutureNotCanceledRoutesWhereUserIsDriver(String username) {
        User user = userRepository.findByUsername(username);
        return routeRepository.findAllFutureRoutesByUserIdAsDriverAndIsNotCanceled(user, LocalDateTime.now());
    }

    @Override
    public List<Route> getAllPastNotCanceledRoutesWhereUserIsPassenger(SortBy sortByEnum, String username) {
        User user = userRepository.findByUsername(username);

        if (sortByEnum.equals(SortBy.DATE_ASC)) {
            return routeRepository.findAllPastOrderByIdAscAsPassengerAndIsNotCanceled(LocalDateTime.now(), user.getId());
        } else if (sortByEnum.equals(SortBy.DATE_DESC)) {
            return routeRepository.findAllPastOrderByIdDescAsPassengerAndIsNotCanceled(LocalDateTime.now(), user.getId());
        } else {
            return routeRepository.findAllByUserIdAsPassengerAndIsNotCanceled(user, LocalDateTime.now());
        }
    }

    @Override
    public List<Route> getAllFutureNotCanceledRoutesWhereUserIsPassenger(String username) {
        User user = userRepository.findByUsername(username);
        return routeRepository.findAllFutureRoutesByUserIdAsPassengerAndIsNotCanceled(user, LocalDateTime.now());
    }

    @Override
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Override
    public Route insert(String carId, String addressId, Boolean officeDirection, LocalDateTime date, String companyAddressId, String username) {
        User user = userRepository.findByUsername(username);
        Car car = carRepository.findById(carId).orElse(null);
        Address address = addressRepository.findById(addressId).orElse(null);

        if (car != null && address != null && user != null) {
            validateInsert(user, officeDirection, date);

            Route route = new Route();
            route.setCar(car);
            route.setDateRoute(date);
            route.setRouteStops(new ArrayList<>());
            route.setOfficeDirection(officeDirection);
            route.setOfficeAddressId(companyAddressId);
            Route savedRoute = routeRepository.save(route);

            RouteStop routeStop = new RouteStop();
            routeStop.setUser(user);
            routeStop.setAddress(address);
            routeStop.setPassengerEnum(PassengerEnum.DRIVER.toString());
            routeStop.setRouteId(savedRoute.getId());
            routeStop.setApproved(true);
            routeStopRepository.save(routeStop);

            return savedRoute;
        } else {
            throw new IllegalArgumentException("Something went wrong. Try again later.");
        }
    }

    private void validateInsert(User user, Boolean officeDirection, LocalDateTime date) {
        if (!user.isDriver()) {
            throw new IllegalArgumentException("Can not add new route. You are not a driver.");
        }

        List<Route> futureRoutes = routeRepository.findAllFutureRoutesByUserIdAsDriverAndIsNotCanceled(user, LocalDateTime.now());
        for (Route futureRoute : futureRoutes) {
            if (futureRoute.getOfficeDirection() == officeDirection &&
                    futureRoute.getDateRoute().getYear() == date.getYear() &&
                    futureRoute.getDateRoute().getDayOfYear() == date.getDayOfYear()) {
                throw new IllegalArgumentException("You already have a route for this day and this direction");
            }
        }
    }

    @Override
    public Route update(String carId, String addressId, String routeId, LocalDateTime date, Boolean officeDirection,
                        String officeAddressId, String username) {
        User user = userRepository.findByUsername(username);
        validateIfRouteInProfile(routeId, user.getId());

        Car car = carRepository.findById(carId).orElse(null);
        Address address = addressRepository.findById(addressId).orElse(null);
        Route route = routeRepository.findById(routeId).orElse(null);

        if (car != null && address != null && route != null) {
            route.setCar(car);
            route.setDateRoute(date);
            route.setOfficeDirection(officeDirection);
            route.setOfficeAddressId(officeAddressId);

            RouteStop routeStop = routeStopRepository.findByRouteIdAndPassengerEnumEquals(routeId, PassengerEnum.DRIVER.toString());
            routeStop.setAddress(address);
            routeStopRepository.save(routeStop);

            return routeRepository.save(route);
        } else {
            throw new IllegalArgumentException("Something went wrong. Please try again.");
        }
    }

    private void validateIfRouteInProfile(String routeId, String userId) {
        String routeUserId = routeRepository.getOne(routeId).getCar().getUserId();
        if (!routeUserId.equals(userId)) {
            throw new IllegalArgumentException("This route was not found in your profile.");
        }
    }

    @Override
    public Route getById(String routeId, Boolean toValidate, String username) {
        if (toValidate) {
            User user = userRepository.findByUsername(username);
            List<Route> routes = routeRepository.findAllFutureRoutesByUserIdAsDriverAndIsNotCanceled(user, LocalDateTime.now());
            boolean routeNotFound = routes.stream().filter(r -> r.getCar().getUserId().equals(user.getId())).count() == 0;
            if (routeNotFound) {
                throw new IllegalArgumentException("Could not find this route in your profile");
            }
        }

        return routeRepository.findById(routeId).orElse(null);
    }

    @Override
    public List<Route> getLastRoutes(Integer limit, String username) {
        User user = userRepository.findByUsername(username);
        List<Route> routes = routeRepository.findAllFutureRoutesExcludingInWhichUserIsDriver(LocalDateTime.now(), user.getId());
        if (routes.size() <= limit) {
            return routes;
        } else {
            return routes.subList(0, limit);
        }
    }

    @Override
    public Route cancelRoute(String routeId, String driverUsername) {
        Route routeToCancel = routeRepository.findById(routeId).orElse(null);
        User driver = userRepository.findByUsername(driverUsername);

        if (routeToCancel != null && driver != null) {
            if (!routeToCancel.getCar().getUserId().equals(driver.getId())) {
                throw new IllegalArgumentException("This route is not present in your profile");
            }

            routeToCancel.setCanceled(true);
            Route savedRoute = routeRepository.save(routeToCancel);

            emailService.sendEmailsForCanceledRoute(routeId, driverUsername);

            return savedRoute;
        } else {
            throw new IllegalArgumentException("Something went wrong. Try again later");
        }
    }

    @Override
    public RouteStop saveSeat(String routeId, String addressId, String username) {
        Address address = addressRepository.findById(addressId).orElse(null);
        User user = userRepository.findByUsername(username);
        Route route = routeRepository.findById(routeId).orElse(null);

        if (address != null && user != null && route != null) {
            validateSaveSeat(addressId, user, route);

            RouteStop routeStop = new RouteStop();
            routeStop.setAddress(address);
            routeStop.setPassengerEnum(PassengerEnum.PASSENGER.toString());
            routeStop.setRouteId(routeId);
            routeStop.setUser(user);
            routeStop.setApproved(false);
            RouteStop savedRouteStop = routeStopRepository.save(routeStop);

            emailService.sendEmailForSavingASeat(route.getCar().getUserId(), username);

            return savedRouteStop;
        } else {
            throw new IllegalArgumentException("Something went wrong. Try again later");
        }
    }

    private void validateSaveSeat(String addressId, User user, Route route) {
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
        if (route.getRouteStops().stream().anyMatch(rs -> rs.getPassengerEnum().equals("DRIVER") && rs.getUser().getId().equals(user.getId()))) {
            throw new IllegalArgumentException("You are driver for this route. Can not save a seat as a passenger");
        }
        if (route.getRouteStops().stream().anyMatch(rs -> rs.getUser().getId().equals(user.getId()))) {
            throw new IllegalArgumentException("You already saved a seat for this route.");
        }
    }

    @Override
    public Iterable<Route> getFutureNotCanceledRoutes(Integer page, SortBy sort, String filter, String username) {
        LocalDateTime localDateTime = LocalDateTime.now();
        User user = userRepository.findByUsername(username);
        PageRequest pageRequest = getPageRequest(sort, page);

        return routePagingAndSortingRepository
                .findAllByDateRouteGreaterThanAndCanceledEqualsAndCar_UserIdIsNot(localDateTime, false, user.getId(), pageRequest);
    }

    @Override
    public Iterable<Route> getNotCanceledRoutesBetweenDatesExcludingUserRoutes(LocalDateTime start, LocalDateTime end, int page,
                                                                               SortBy sort, String username, String officeAddressId) {
        User user = userRepository.findByUsername(username);
        PageRequest pageRequest = getPageRequest(sort, page);
        return routePagingAndSortingRepository
                .findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(start, end, false, user.getId(), pageRequest);
    }

    private PageRequest getPageRequest(SortBy sort, Integer page) {
        PageRequest pageRequest;
        String dateRouteField = "dateRoute";

        if (sort == SortBy.DATE_DESC) {
            pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(dateRouteField).descending());
        } else if (sort == SortBy.DATE_ASC) {
            pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(dateRouteField).ascending());
        } else {
            pageRequest = PageRequest.of(page, PAGE_SIZE);
        }

        return pageRequest;
    }

    @Override
    public List<TopUser> getTop15RidersByNumberOfPassengers() {
        List<TopUser> topUsers = getTopUsers();
        Collections.sort(topUsers);
        return getFirst15Users(topUsers);
    }

    @Override
    public List<TopUser> getTop15RidersByNumberOfDrives() {
        List<TopUser> topUsers = getTopUsers();
        Comparator<TopUser> compareByDrives = (TopUser o1, TopUser o2) -> o2.getNumberRides().compareTo(o1.getNumberRides());
        topUsers.sort(compareByDrives);
        return getFirst15Users(topUsers);
    }

    @Override
    public List<TopUser> getTop15RidersByRating() {
        List<TopUser> topUsers = getTopUsers();
        Comparator<TopUser> compareByDrives = (TopUser o1, TopUser o2) -> o2.getRating().compareTo( o1.getRating());
        topUsers.sort(compareByDrives);
        return getFirst15Users(topUsers);
    }

    private List<TopUser> getFirst15Users(List<TopUser> topUsers) {
        if (topUsers.size() < 15) {
            return topUsers;
        }
        return topUsers.subList(0, 14);
    }

    private List<TopUser> getTopUsers() {
        List<TopUser> topUsers = new ArrayList<>();
        List<User> drivers = userRepository.findAll().stream().filter(User::isDriver).collect(Collectors.toList());
        for (User driver : drivers) {
            List<Route> userRoutes = routeRepository.findAllByUserIdAsDriver(driver);
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
        int passengersCount = 0;
        for (Route userRoute : userRoutes) {
            passengersCount += getNumberOfPassengersInRoute(userRoute);
        }
        return passengersCount;
    }

    private int getNumberOfPassengersInRoute(Route route) {
        return route.getRouteStops().size() - 1;
    }

    @Override
    public Iterable<Route> sortAndFilter(LocalDateTime start, LocalDateTime end, int page, SortBy sort, Boolean officeDirection,
                                         String username, String officeAddressId) {
        User user = userRepository.findByUsername(username);
        PageRequest pageRequest;
        String dateRouteField = "dateRoute";

        if (sort == SortBy.DATE_DESC) {
            if (officeDirection == null) {
                pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(dateRouteField).descending());
                return routePagingAndSortingRepository
                        .findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(start, end, false, user.getId(), pageRequest);
            }
            pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(dateRouteField).descending());
        } else if (sort == SortBy.DATE_ASC) {
            if (officeDirection == null) {
                start = start.plusHours(LocalDateTime.now().getHour());
                pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(dateRouteField).ascending());
                return routePagingAndSortingRepository
                        .findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(start, end, false, user.getId(), pageRequest);
            }
            pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(dateRouteField).ascending());
        } else {
            if (officeDirection == null) {
                pageRequest = PageRequest.of(page, PAGE_SIZE);
                return routePagingAndSortingRepository
                        .findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(start, end, false, user.getId(), pageRequest);
            }
            pageRequest = PageRequest.of(page, PAGE_SIZE);
        }
        return routePagingAndSortingRepository
                .findAllByDateRouteBetweenAndCanceledEqualsAndOfficeDirectionEqualsAndCar_UserIdIsNotAndOfficeAddressIdEquals(start, end,
                        false, officeDirection, user.getId(), officeAddressId, pageRequest);
    }
}
