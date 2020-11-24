package com.knikolov.sharearide.controller;

import com.knikolov.sharearide.dto.CarDto;
import com.knikolov.sharearide.models.Car;
import com.knikolov.sharearide.service.CarService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for Car entities
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @RequestMapping(value = "/cars", method = RequestMethod.GET)
    public List<Car> getAllCars(Principal principal) {
        return carService.getAllByUsername(principal.getName());
    }

    @RequestMapping(value = "/car/{carId}", method = RequestMethod.GET)
    public Car getCarById(@PathVariable String carId) {
        return carService.getById(carId);
    }

    @RequestMapping(value = "/car", method = RequestMethod.DELETE)
    public Car deleteCarById(@RequestParam("carId") String carId, Principal principal) {
        return carService.delete(carId, principal.getName());
    }

    @RequestMapping(value = "/car", method = RequestMethod.POST)
    public Car addNewCar(@RequestBody CarDto car, Principal principal) {
        validateCar(car);
        return carService.insert(car, principal.getName());
    }

    @RequestMapping(value = "/car", method = RequestMethod.PATCH)
    public Car updateCar(@RequestBody CarDto car, Principal principal) {
        validateCar(car);
        return carService.update(car, principal.getName());
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
