package com.knikolov.sharearide.controller;

import com.knikolov.sharearide.dto.CarDto;
import com.knikolov.sharearide.models.Car;
import com.knikolov.sharearide.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
public class CarController {

    private final CarService carService;

    @Autowired
    public CarController(CarService carService) {
        this.carService = carService;
    }

    @RequestMapping(value = "/cars", method = RequestMethod.GET)
    List<Car> getAllCarsByUser(Principal principal) {
        return this.carService.getAllCarsByUser(principal.getName());
    }

    @RequestMapping(value = "/car/{carId}", method = RequestMethod.GET)
    Car getCarById(@PathVariable String carId, Principal principal) {
        return carService.getCarById(carId);
    }

    @RequestMapping(value = "/car", method = RequestMethod.DELETE)
    Car deleteCarById(@RequestParam("carId") String carId, Principal principal) {
        return carService.deleteCar(carId, principal.getName());
    }

    @RequestMapping(value = "/car", method = RequestMethod.POST)
    Car addNewCar(@RequestBody CarDto car, Principal principal) {
        validateCar(car);
        return carService.addNewCar(car, principal.getName());
    }

    @RequestMapping(value = "/car", method = RequestMethod.PATCH)
    Car updateCar(@RequestBody CarDto car, Principal principal) {
        validateCar(car);
        return carService.updateCar(car, principal.getName());
    }

    private void validateCar(CarDto car) {
        if (car == null) {
            throw new IllegalArgumentException("Something went wrong. Try again later.");
        } else if (car.getManufacturer() == null || "".equals(car.getManufacturer().trim())) {
            throw new IllegalArgumentException("Manufacturer field is not filled correct.");
        } else if (car.getModel() == null || "".equals(car.getModel().trim())) {
            throw new IllegalArgumentException("Model field is not filled correct.");
        } else if (car.getSeats() == 0) {
            throw new IllegalArgumentException("Seats field must be more than 0.");
        } else if (car.getYear() <= 1900 || car.getYear() > LocalDateTime.now().getYear() + 1) {
            throw new IllegalArgumentException("Year field must be between 1900 and " + (LocalDateTime.now().getYear() + 1));
        } else if (car.getColor() == null || "".equals(car.getColor().trim())) {
            throw new IllegalArgumentException("Color field is not filled correct.");
        }
    }
}
