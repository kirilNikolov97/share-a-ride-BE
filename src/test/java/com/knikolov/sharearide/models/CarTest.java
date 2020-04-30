package com.knikolov.sharearide.models;

import com.knikolov.sharearide.repository.CarRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@DataJpaTest
public class CarTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CarRepository carRepository;

    @BeforeAll
    static void setup() {

    }

    @Test
    public void whenFindById_thenReturnCar() {
        // given
        User user = new User("userId", "username", "email", "nz", "first", "last", "123321", false, true);
        entityManager.persist(user);
        entityManager.flush();

        Car car = new Car("carId", "userId", "manufacturer", "model", 4, 1999, "color", false);
        entityManager.persist(car);
        entityManager.flush();

        // when
        Car found = carRepository.findById(car.getId()).get();

        // then
        assertEquals(car.getUserId(), found.getUserId());
    }

}