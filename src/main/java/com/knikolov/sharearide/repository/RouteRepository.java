package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.Route;
import com.knikolov.sharearide.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RouteRepository extends JpaRepository<Route, String> {

    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE rs.userId = ?1 AND rs.passengerEnum = 'DRIVER' AND r.dateRoute <= ?2 AND r.canceled = false")
    List<Route> findAllPastByUserIdAsDriver(User userId, LocalDateTime date);

    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE rs.userId = ?1 AND rs.passengerEnum = 'DRIVER' AND r.dateRoute > ?2 AND r.canceled = false")
    List<Route> findAllFutureRoutesByUserIdAsDriver(User userId, LocalDateTime date);

    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE rs.userId = ?1 AND rs.passengerEnum = 'PASSENGER' AND r.dateRoute <=?2 AND r.canceled = false")
    List<Route> findAllByUserIdAsPassenger(User userIdm, LocalDateTime date);

    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE rs.userId = ?1 AND rs.passengerEnum = 'PASSENGER' AND r.dateRoute > ?2 AND r.canceled = false")
    List<Route> findAllFutureRoutesByUserIdAsPassenger(User user, LocalDateTime now);

    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE r.dateRoute > ?1 AND rs.passengerEnum = 'DRIVER' AND rs.userId.id <> ?2 ORDER BY r.id DESC")
    List<Route> findAllByOrderByIdDesc(LocalDateTime now, String userId);

    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE rs.userId = ?1 AND rs.passengerEnum = 'DRIVER' AND r.canceled = false")
    List<Route> findAllByUserIdAsDriver(User user);

    List<Route> findAllByCarIdAndDateRouteAfterAndCanceledEquals(String car_id, LocalDateTime dateRoute, Boolean canceled);

}
