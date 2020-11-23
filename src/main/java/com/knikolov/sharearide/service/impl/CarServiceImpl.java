package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.dto.CarDto;
import com.knikolov.sharearide.models.Car;
import com.knikolov.sharearide.models.User;
import com.knikolov.sharearide.repository.CarRepository;
import com.knikolov.sharearide.repository.RouteRepository;
import com.knikolov.sharearide.repository.UserRepository;
import com.knikolov.sharearide.service.CarService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * CarService implementation
 */
@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;

    public CarServiceImpl(CarRepository carRepository, UserRepository userRepository, RouteRepository routeRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    public List<Car> getAllByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return carRepository.findAllByUserIdAndDeletedEquals(user.getId(), false);
    }

    @Override
    public Car getById(String carId) {
        return carRepository.findById(carId).orElse(null);
    }

    @Override
    public Car insert(CarDto carDto, String username) {
        User user = userRepository.findByUsername(username);
        validateUser(user);

        Car car = new Car();
        car.setId(UUID.randomUUID().toString());
        car.setUserId(user.getId());
        car.setManufacturer(carDto.getManufacturer());
        car.setModel(carDto.getModel());
        car.setSeats(carDto.getSeats());
        car.setColor(carDto.getColor());
        car.setYear(carDto.getYear());
        car.setDeleted(false);

        try {
            return carRepository.save(car);
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong. Please try again later.");
        }
    }

    @Override
    public Car update(CarDto carDto, String username) {
        User user = userRepository.findByUsername(username);
        validateUser(user);

        Car car = carRepository.findById(carDto.getId()).orElse(null);
        if (car != null && carDto.getUserId().equals(user.getId())) {
            car.setManufacturer(carDto.getManufacturer());
            car.setModel(carDto.getModel());
            car.setColor(carDto.getColor());
            car.setSeats(carDto.getSeats());
            car.setYear(carDto.getYear());

            try {
                return carRepository.save(car);
            } catch (Exception e) {
                throw new IllegalArgumentException("Something went wrong. Please try again later");
            }
        } else {
            throw new IllegalArgumentException("Car is not present in your profile.");
        }
    }

    @Override
    public Car delete(String carId, String username) {
        User user = userRepository.findByUsername(username);
        validateUser(user);

        Car car = carRepository.findById(carId).orElse(null);
        if (car == null) {
            throw new IllegalArgumentException("This car is not available.");
        }
        if (!car.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("Could not find this car in your profile.");
        }
        if (routeRepository.findAllByCarIdAndDateRouteAfterAndCanceledEquals(carId, LocalDateTime.now(), false).size() > 0) {
            throw new IllegalArgumentException("The car is assigned to future route. Change the car for the route first and then delete this car");
        }

        car.setDeleted(true);
        try {
            return carRepository.save(car);
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong. Please try again later.");
        }
    }

    private void validateUser(User user) {
        if (!user.isDriver()) {
            throw new IllegalArgumentException("You are not a driver.");
        }
    }
}
