package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, String> {

    List<Car> findAllByUserId(String userId);

    List<Car> findAllByUserIdAndDeletedEquals(String userId, Boolean deleted);

}
