package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.dto.TopUser;
import com.knikolov.sharearide.enums.PassengerEnum;
import com.knikolov.sharearide.models.*;
import com.knikolov.sharearide.repository.*;
import com.knikolov.sharearide.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
class RouteServiceImplTest {

    @InjectMocks
    RouteServiceImpl routeService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private RouteStopRepository routeStopRepository;


    private Car car = new Car("carId", "userId", "manufacturer", "model", 4, 1999,
            "color", false);
    private Car anotherCar = new Car("carId", "anotherUserId", "manufacturer", "model", 0, 1999,
            "color", false);
    private User user = new User("userId", "username", "user@user.mail", "nz", "first",
            "last", "123321", false, true);
    private User anotherUser = new User("anotherUserId", "anotherUsername", "another@user.mail", "nz", "first",
            "last", "123321", false, true);
    private User notDriver = new User("notDriverId", "notDriver", "not@driver.mail", "nz", "not",
            "driver", "123322", false, false);
    private Address address = new Address("addressId", "district", "street", "");
    private Route route = new Route("routeId", LocalDateTime.now(), false, true, "officeAddressId", car);
    private Route canceledRoute = new Route("routeId", LocalDateTime.now(), true, true, "officeAddressId", car);
    private Route anotherRoute = new Route("anotherRouteId", LocalDateTime.now(), false, true, "officeAddressId", anotherCar);
    private Route futureRoute = new Route("anotherRouteId", LocalDateTime.now().plusHours(1) , false, true, "officeAddressId", car);
    private Route futureRouteAnotherCar = new Route("anotherRouteId", LocalDateTime.now().plusHours(1) , false, true, "officeAddressId", anotherCar);
    private Route futureRouteCanceled = new Route("anotherRouteId", LocalDateTime.now().plusHours(1) , true, true, "officeAddressId", car);
    private RouteStop routeStop = new RouteStop("routeStopId", "routeId", address, user, PassengerEnum.DRIVER.toString(), false);
    private RouteStop routeStopAnotherUser = new RouteStop("routeStopId", "routeId", address, anotherUser, PassengerEnum.PASSENGER.toString(), false);

    @Test
    void whenAddNewCar_thenSuccess() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(carRepository.findById("carId")).thenReturn(Optional.of(car));
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));
        when(routeRepository.findAllFutureRoutesByUserIdAsDriverAndIsNotCanceled(any(), any())).thenReturn(new ArrayList<>());
        when(routeRepository.save(any())).thenReturn(route);
        when(routeStopRepository.save(any())).thenReturn(null);

        // when
        Route returned = routeService.insert("carId", "addressId", true,
                LocalDateTime.now(), "companyAddressId","username");

        // then
        assertEquals(car.getId(), returned.getCar().getId());
        assertEquals("officeAddressId", returned.getOfficeAddressId());
        assertTrue(returned.getOfficeDirection());
    }

    @Test
    void whenAddNewCar_thenThrowNotADriver() {
        // given
        when(userRepository.findByUsername("notDriver")).thenReturn(notDriver);
        when(carRepository.findById("carId")).thenReturn(Optional.of(car));
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.insert("carId", "addressId", true, LocalDateTime.now(), "companyAddressId","notDriver"));

        // then
        assertEquals("Can not add new route. You are not a driver.", exception.getMessage());
    }

    @Test
    void whenAddNewCar_thenThrowDuplicateRouteForDayAndDirection() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(carRepository.findById("carId")).thenReturn(Optional.of(car));
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));
        when(routeRepository.findAllFutureRoutesByUserIdAsDriverAndIsNotCanceled(any(), any()))
                .thenReturn(new ArrayList<Route>() {{ add(new Route("newRoute", LocalDateTime.now(), false, true, "officeAddressId", car)); }});

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.insert("carId", "addressId", true, LocalDateTime.now(), "companyAddressId","username"));

        // then
        assertEquals("You already have a route for this day and this direction", exception.getMessage());
    }

    @Test
    void whenUpdateFutureRoute_thenSuccess() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.getOne(any())).thenReturn(route);
        when(carRepository.findById("carId")).thenReturn(Optional.of(car));
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));
        when(routeRepository.findById("routeId")).thenReturn(Optional.of(route));
        when(routeStopRepository.findByRouteIdAndPassengerEnumEquals("routeId", PassengerEnum.DRIVER.toString())).thenReturn(new RouteStop());
        when(routeRepository.save(any())).thenReturn(route);

        // when
        Route returned = routeService.update("carId", "addressId", "routeId", LocalDateTime.now(), true, "officeAddressId", "username");

        // then
        assertEquals(route.getId(), returned.getId());
    }

    @Test
    void whenUpdateFutureRoute_thenThrowRouteNotFoundInProfile() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.getOne(any())).thenReturn(anotherRoute);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.update("carId", "addressId", "routeId", LocalDateTime.now(), true, "officeAddressId", "username"));

        // then
        assertEquals("This route was not found in your profile.", exception.getMessage());
    }

    @Test
    void whenGetRouteById_thenSuccess() {
        // given
        when(routeRepository.findById("routeId")).thenReturn(Optional.of(route));

        // when
        Route returned = routeService.getById("routeId", false, "username");

        // then
        assertEquals(route.getId(), returned.getId());
    }

    @Test
    void whenGetRouteById_thenThrowNotFoundInProfile() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.findAllFutureRoutesByUserIdAsDriverAndIsNotCanceled(any(), any())).thenReturn(new ArrayList<>());

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.getById("routeId", true, "username"));

        // then
        assertEquals("Could not find this route in your profile", exception.getMessage());
    }

    @Test
    void whenGetLastRoutes_thenSuccessSublist() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.findAllFutureRoutesExcludingInWhichUserIsDriver(any(), any())).thenReturn(new ArrayList<Route>() {{ add(new Route()); add(new Route());}});

        // when
        List<Route> routes = routeService.getLastRoutes(1, "username");

        //then
        assertEquals(1, routes.size());
    }

    @Test
    void whenGetLastRoutes_thenSuccess() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.findAllFutureRoutesExcludingInWhichUserIsDriver(any(), any())).thenReturn(new ArrayList<Route>() {{ add(new Route()); add(new Route());}});

        // when
        List<Route> routes = routeService.getLastRoutes(10, "username");

        //then
        assertEquals(2, routes.size());
    }

    @Test
    void whenCancelRoute_thenSuccess() {
        // given
        when(routeRepository.findById("routeId")).thenReturn(Optional.of(route));
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.save(any())).thenReturn(canceledRoute);
        doNothing().when(emailService).sendEmailsForCanceledRoute(any(), any());

        // when
        Route returned = routeService.cancelRoute("routeId", "username");

        // then
        assertTrue(returned.getCanceled());
    }

    @Test
    void whenCancelRoute_thenThrowRouteNotInProfile() {
        // given
        when(routeRepository.findById("routeId")).thenReturn(Optional.of(route));
        when(userRepository.findByUsername("notDriverId")).thenReturn(notDriver);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.cancelRoute("routeId", "notDriverId"));

        // then
        assertEquals("This route is not present in your profile", exception.getMessage());
    }

    @Test
    void whenSaveSeat_thenSuccess() {
        // given
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.findById("routeId")).thenReturn(Optional.of(futureRoute));
        when(routeStopRepository.save(any())).thenReturn(routeStop);
        doNothing().when(emailService).sendEmailForSavingASeat(any(), any());

        user.setAddresses(new ArrayList<Address>() {{ add(address); }});
        futureRoute.setRouteStops(new ArrayList<>());

        // when
        RouteStop returned = routeService.saveSeat("routeId", "addressId", "username");

        // then
        assertEquals("routeStopId", returned.getId());
        assertFalse(returned.getApproved());
    }

    @Test
    void whenSaveSeat_throwSomethingWentWrong() {
        // given
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.findById("routeId")).thenReturn(Optional.ofNullable(null));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.saveSeat("routeId", "addressId", "username"));

        // then
        assertEquals("Something went wrong. Try again later", exception.getMessage());
    }

    @Test
    void whenSaveSeat_throwAddressNotAssigned() {
        // given
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.findById("routeId")).thenReturn(Optional.ofNullable(futureRoute));

        user.setAddresses(new ArrayList<Address>() {{}});
        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.saveSeat("routeId", "addressId", "username"));

        // then
        assertEquals("This address is not assigned to this user", exception.getMessage());
    }

    @Test
    void whenSaveSeat_throwAlreadyPassed() {
        // given
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.findById("routeId")).thenReturn(Optional.ofNullable(route));

        user.setAddresses(new ArrayList<Address>() {{ add(address); }});
        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.saveSeat("routeId", "addressId", "username"));

        // then
        assertEquals("This route is already passed. You can not save a seat for this route", exception.getMessage());
    }

    @Test
    void whenSaveSeat_throwNoFreeSeats() {
        // given
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.findById("routeId")).thenReturn(Optional.ofNullable(futureRouteAnotherCar));

        user.setAddresses(new ArrayList<Address>() {{ add(address); }});
        futureRouteAnotherCar.setRouteStops(new ArrayList<>());

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.saveSeat("routeId", "addressId", "username"));

        // then
        assertEquals("No free seats in the car.", exception.getMessage());
    }

    @Test
    void whenSaveSeat_throwCanceledRoute() {
        // given
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.findById("routeId")).thenReturn(Optional.ofNullable(futureRouteCanceled));

        user.setAddresses(new ArrayList<Address>() {{ add(address); }});
        futureRouteCanceled.setRouteStops(new ArrayList<>());

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.saveSeat("routeId", "addressId", "username"));

        // then
        assertEquals("Route is canceled. You can not save a seat for this route", exception.getMessage());
    }

    @Test
    void whenSaveSeat_throwDriverOfTheRoute() {
        // given
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(routeRepository.findById("routeId")).thenReturn(Optional.ofNullable(futureRoute));

        user.setAddresses(new ArrayList<Address>() {{ add(address); }});
        futureRoute.setRouteStops(new ArrayList<RouteStop>() {{ add(routeStop); }});

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.saveSeat("routeId", "addressId", "username"));

        // then
        assertEquals("You are driver for this route. Can not save a seat as a passenger", exception.getMessage());
    }

    @Test
    void whenSaveSeat_throwAlreadySavedASeat() {
        // given
        when(addressRepository.findById("addressId")).thenReturn(Optional.of(address));
        when(userRepository.findByUsername("anotherUsername")).thenReturn(anotherUser);
        when(routeRepository.findById("routeId")).thenReturn(Optional.ofNullable(futureRoute));

        anotherUser.setAddresses(new ArrayList<Address>() {{ add(address); }});
        futureRoute.setRouteStops(new ArrayList<RouteStop>() {{ add(routeStopAnotherUser); add(routeStopAnotherUser); }});

        // when
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> routeService.saveSeat("routeId", "addressId", "anotherUsername"));

        // then
        assertEquals("You already saved a seat for this route.", exception.getMessage());
    }

    @Test
    void whenGetTop15Riders_thenSuccess() {
        // given
        when(userRepository.findAll()).thenReturn(new ArrayList<User>() {{ add(user); add(anotherUser); }});
        doReturn(new ArrayList<Route>() {{ add(route); }}).when(routeRepository).findAllByUserIdAsDriver(user);
        doReturn(new ArrayList<Route>() {{ add(anotherRoute); }}).when(routeRepository).findAllByUserIdAsDriver(anotherUser);

        user.setRatings(new ArrayList<Rating>() {{ add(new Rating(new RatingId("userId", "anotherUserId"), 3, LocalDateTime.now())); }});
        anotherUser.setRatings(new ArrayList<Rating>() {{ add(new Rating(new RatingId("anotherUserId", "userId"), 4, LocalDateTime.now())); }});
        route.setRouteStops(new ArrayList<RouteStop>() {{ add(routeStop); }});
        anotherRoute.setRouteStops(new ArrayList<RouteStop>() {{ add(routeStopAnotherUser); }});

        // when
        List<TopUser> returnedList = routeService.getTop15RidersByNumberOfPassengers();

        // then
        assertEquals(2, returnedList.size());
        assertEquals(user.getId(), returnedList.get(0).getUser().getId());
        assertEquals(anotherUser.getId(), returnedList.get(1).getUser().getId());
    }

    @Test
    void whenGetTop15RidersByRating_thenSuccess() {
        // given
        when(userRepository.findAll()).thenReturn(new ArrayList<User>() {{ add(user); add(anotherUser); }});
        doReturn(new ArrayList<Route>() {{ add(route); }}).when(routeRepository).findAllByUserIdAsDriver(user);
        doReturn(new ArrayList<Route>() {{ add(anotherRoute); }}).when(routeRepository).findAllByUserIdAsDriver(anotherUser);

        user.setRatings(new ArrayList<Rating>() {{ add(new Rating(new RatingId("userId", "anotherUserId"), 3, LocalDateTime.now())); }});
        anotherUser.setRatings(new ArrayList<Rating>() {{ add(new Rating(new RatingId("anotherUserId", "userId"), 4, LocalDateTime.now())); }});
        route.setRouteStops(new ArrayList<RouteStop>() {{ add(routeStop); }});
        anotherRoute.setRouteStops(new ArrayList<RouteStop>() {{ add(routeStopAnotherUser); }});

        // when
        List<TopUser> returnedList = routeService.getTop15RidersByRating();

        // then
        assertEquals(2, returnedList.size());
        assertEquals(anotherUser.getId(), returnedList.get(0).getUser().getId());
        assertEquals(user.getId(), returnedList.get(1).getUser().getId());
    }


}