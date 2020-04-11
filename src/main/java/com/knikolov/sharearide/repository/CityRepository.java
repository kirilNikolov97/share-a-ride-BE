package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.City;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CityRepository extends CrudRepository<City, String> {
    List<City> findAll();
}
