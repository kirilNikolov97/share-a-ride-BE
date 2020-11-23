package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.RouteStop;
import com.knikolov.sharearide.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository for RouteStop entity
 */
public interface RouteStopRepository extends JpaRepository<RouteStop, String> {

    RouteStop findByRouteIdAndPassengerEnumEquals(String routeId, String passengerEnum);

    List<RouteStop> findAllByPassengerEnumAndUserEquals(String passengerEnum, User user);

    // Find all unique RouteStop ids WHERE RouteStop.passengerEnum == "passengerEnum" AND RouteStop.user.id == "userId"
    @Query("SELECT DISTINCT rs.routeId from RouteStop rs WHERE rs.passengerEnum=?1 AND rs.user.id=?2")
    List<String> findAllRouteIdsByPassengerEnumEqualsAndUserIdEquals(String passengerEnum, String userId);
}
