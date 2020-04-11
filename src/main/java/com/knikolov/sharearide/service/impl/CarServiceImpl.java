package com.knikolov.sharearide.service.impl;

import com.knikolov.sharearide.dto.CarDto;
import com.knikolov.sharearide.models.Car;
import com.knikolov.sharearide.models.User;
import com.knikolov.sharearide.repository.CarRepository;
import com.knikolov.sharearide.repository.UserRepository;
import com.knikolov.sharearide.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;

    @Autowired
    public CarServiceImpl(CarRepository carRepository, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Car> getAllCarsByUser(String username) {
        User user = this.userRepository.findByUsername(username);
        return this.carRepository.findAllByUserId(user.getId());
    }

    @Override
    public Car getCarById(String carId) {
        return this.carRepository.findById(carId).orElse(null);
    }

    @Override
    public Car addNewCar(CarDto car, String username) {
        User user = this.userRepository.findByUsername(username);
        Car newCar = new Car();

        try {
            newCar.setId(UUID.randomUUID().toString());
            newCar.setUserId(user.getId());
            newCar.setManufacturer(car.getManufacturer());
            newCar.setModel(car.getModel());
            newCar.setSeats(car.getSeats());
            newCar.setColor(car.getColor());
            newCar.setYear(car.getYear());

            return this.carRepository.save(newCar);
        } catch (Exception e) {
            throw new IllegalArgumentException("Something went wrong. Please try again later.");
        }
    }

    @Override
    public Car updateCar(CarDto car, String name) {
        Car carToUpdate = this.carRepository.findById(car.getId()).orElse(null);

        if(carToUpdate != null) {
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
            throw new IllegalArgumentException("Car is not present.");
        }

    }

    @Override
    public Car deleteCar(String carId) {
        Car carToDelete = this.carRepository.findById(carId).orElse(null);
        if(carToDelete != null) {
            this.carRepository.delete(carToDelete);
            return carToDelete;
        } else {
            return null;
        }
    }

}
