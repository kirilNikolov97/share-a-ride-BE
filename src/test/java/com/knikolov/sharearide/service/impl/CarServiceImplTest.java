package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.dto.CarDto;
import com.knikolov.sharearide.models.Car;
import com.knikolov.sharearide.models.Route;
import com.knikolov.sharearide.models.User;
import com.knikolov.sharearide.repository.CarRepository;
import com.knikolov.sharearide.repository.RouteRepository;
import com.knikolov.sharearide.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(SpringJUnit4ClassRunner.class)
class CarServiceImplTest {

    @InjectMocks
    CarServiceImpl carService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RouteRepository routeRepository;

    private Car car = new Car("carId", "userId", "manufacturer", "model", 4, 1999,
            "color", false);
    private Car updatedCar = new Car("updatedCarId", "userId", "updated manufacturer", "updated model", 5, 2000,
            "color", false);
    private Car deletedCar = new Car("deletedCarId", "userId", "deleted manufacturer", "deleted model", 5, 2000,
            "color", true);
    private CarDto carDto = new CarDto("carId", "userId", "manufacturer", "model", 4, 1999,
            "color");
    private User user = new User("userId", "username", "user@user.mail", "nz", "first",
            "last", "123321", false, true);
    private User notDriver = new User("notDriverId", "notdriver", "not@driver.mail", "nz", "not",
            "driver", "1232221", false, false);

    @Test
    void whenValidName_thenCarShouldBeFound() {
        // given
        when(carRepository.findById(any())).thenReturn(Optional.of(car));

        // when
        Car found = carService.getById("carId");

        // then
        assertEquals("carId", found.getId());
    }

    @Test
    void whenUpdateCar_thenSuccess() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(carRepository.findById(any())).thenReturn(Optional.of(car));
        when(carRepository.save(any())).thenReturn(updatedCar);

        // when
        Car returned = carService.update(carDto, "username");

        // then
        assertEquals(updatedCar.getId(), returned.getId());
    }

    @Test
    void whenUpdateCar_thenThrowNotADriver() {
        // given
        when(userRepository.findByUsername("notdriver")).thenReturn(notDriver);

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> carService.update(carDto, "notdriver"));

        // then
        assertEquals("You are not a driver.", exception.getMessage());
    }

    @Test
    void whenUpdateCar_thenThrowCarNotPresent() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(carRepository.findById(any())).thenReturn(Optional.ofNullable(null));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> carService.update(carDto, "username"));

        // then
        assertEquals("Car is not present in your profile.", exception.getMessage());
    }

    @Test
    void whenDeleteCar_Success() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(carRepository.findById(any())).thenReturn(Optional.of(car));
        when(routeRepository.findAllByCarIdAndDateRouteAfterAndCanceledEquals(any(), any(), any())).thenReturn(new ArrayList<>());
        when(carRepository.save(any())).thenReturn(deletedCar);

        // when
        Car returned = carService.delete("carId", "username");

        // then
        assertTrue(returned.getDeleted());
    }

    @Test
    void whenDeleteCar_thenThrowCarNotAvailable() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(carRepository.findById(any())).thenReturn(Optional.ofNullable(null));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> carService.delete("carId", "username"));

        // then
        assertEquals("This car is not available.", exception.getMessage());
    }

    @Test
    void whenDeleteCar_thenThrowCarNotFound() {
        // given
        Car someoneElsesCar = new Car("carId", "otherUserId", "manufacturer", "model", 4, 1999,
                "color", false);
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(carRepository.findById(any())).thenReturn(Optional.of(someoneElsesCar));

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> carService.delete("carId", "username"));

        // then
        assertEquals("Could not find this car in your profile.", exception.getMessage());
    }

    @Test
    void whenDeleteCar_thenThrowCarAssignedToRoute() {
        // given
        when(userRepository.findByUsername("username")).thenReturn(user);
        when(carRepository.findById(any())).thenReturn(Optional.of(car));
        when(routeRepository.findAllByCarIdAndDateRouteAfterAndCanceledEquals(any(), any(), any())).thenReturn(new ArrayList<Route>() {{ add(new Route()); }});

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> carService.delete("carId", "username"));

        // then
        assertEquals("The car is assigned to future route. Change the car for the route first and then delete this car", exception.getMessage());
    }

}