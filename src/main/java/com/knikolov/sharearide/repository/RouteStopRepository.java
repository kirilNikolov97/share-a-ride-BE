package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.RouteStop;
import com.knikolov.sharearide.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RouteStopRepository extends JpaRepository<RouteStop, String> {

    List<RouteStop> findAllByRouteId(String routeId);

    RouteStop findByRouteIdAndPassengerEnumEquals(String routeId, String passengerEnum);

    List<RouteStop> findAllByPassengerEnumEqualsOrderByUserId(String passengerEnum);

    List<RouteStop> findAllByPassengerEnumAndUserIdEquals(String passengerEnum, User userId);

    @Query("SELECT DISTINCT rs.routeId from RouteStop rs WHERE rs.passengerEnum=?1 AND rs.userId.id=?2")
    List<String> findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals(String passengerEnum, String userId);
}
