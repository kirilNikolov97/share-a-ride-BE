package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.dto.CarDto;
import com.knikolov.sharearide.models.Car;
import com.knikolov.sharearide.models.User;
import com.knikolov.sharearide.repository.CarRepository;
import com.knikolov.sharearide.repository.RouteRepository;
import com.knikolov.sharearide.repository.UserRepository;
import com.knikolov.sharearide.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;

    @Autowired
    public CarServiceImpl(CarRepository carRepository, UserRepository userRepository, RouteRepository routeRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    public List<Car> getAllCarsByUser(String username) {
        User user = this.userRepository.findByUsername(username);
        return this.carRepository.findAllByUserIdAndDeletedEquals(user.getId(), false);
    }

    @Override
    public Car getCarById(String carId) {
        return this.carRepository.findById(carId).orElse(null);
    }

    @Override
    public Car addNewCar(CarDto car, String username) {
        User user = this.userRepository.findByUsername(username);
        validateIfDriver(user);

        Car newCar = new Car();

        try {
            newCar.setId(UUID.randomUUID().toString());
            newCar.setUserId(user.getId());
            newCar.setManufacturer(car.getManufacturer());
            newCar.setModel(car.getModel());
            newCar.setSeats(car.getSeats());
            newCar.setColor(car.getColor());
            newCar.setYear(car.getYear());
            newCar.setDeleted(false);

            return this.carRepository.save(newCar);
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong. Please try again later.");
        }
    }

    @Override
    public Car updateCar(CarDto car, String name) {
        User user = this.userRepository.findByUsername(name);
        validateIfDriver(user);

        Car carToUpdate = this.carRepository.findById(car.getId()).orElse(null);

        if(carToUpdate != null && car.getUserId().equals(user.getId())) {
            try {
                carToUpdate.setManufacturer(car.getManufacturer());
                carToUpdate.setModel(car.getModel());
                carToUpdate.setColor(car.getColor());
                carToUpdate.setSeats(car.getSeats());
                carToUpdate.setYear(car.getYear());

                return this.carRepository.save(carToUpdate);
            } catch (Exception e) {
                throw new IllegalArgumentException("Something went wrong. Please try again later");
            }
        } else {
            throw new IllegalArgumentException("Car is not present in your profile.");
        }

    }

    @Override
    public Car deleteCar(String carId, String name) {
        User user = this.userRepository.findByUsername(name);
        validateIfDriver(user);
        Car carToDelete = this.carRepository.findById(carId).orElse(null);

        if (carToDelete == null) {
            throw new IllegalArgumentException("This car is not available.");
        }
        if (!carToDelete.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("Could not find this car in your profile.");
        }
        if (routeRepository.findAllByCarIdAndDateRouteAfterAndCanceledEquals(carId, LocalDateTime.now(), false).size() > 0) {
            throw new IllegalArgumentException("The car is assigned to future route. Change the car for the route first and then delete this car");
        }

        carToDelete.setDeleted(true);
        return this.carRepository.save(carToDelete);
    }

    private void validateIfDriver(User user) {
        if (!user.isDriver()) {
            throw new IllegalArgumentException("You are not a driver. Can not create new car.");
        }
    }

}
