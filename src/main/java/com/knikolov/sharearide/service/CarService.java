package com.knikolov.sharearide.service;

import com.knikolov.sharearide.dto.CarDto;
import com.knikolov.sharearide.models.Car;

import java.util.List;

public interface CarService {

    List<Car> getAllCarsByUser(String username);

    Car getCarById(String carId);

    Car addNewCar(CarDto car, String name);

    Car deleteCar(String carId);

    Car updateCar(CarDto car, String name);
}
