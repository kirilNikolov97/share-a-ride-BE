package com.knikolov.sharearide.service;

import com.knikolov.sharearide.dto.CarDto;
import com.knikolov.sharearide.models.Car;

import java.util.List;

public interface CarService {

    List<Car> getAllByUsername(String username);

    Car getById(String carId);

    Car insert(CarDto car, String name);

    Car delete(String carId, String name);

    Car update(CarDto car, String name);
}
