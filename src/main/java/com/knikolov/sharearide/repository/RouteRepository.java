package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.Route;
import com.knikolov.sharearide.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Route entity
 */
public interface RouteRepository extends JpaRepository<Route, String> {

    // Find all routes past specific date by userId WHERE user is driver AND route is not canceled
    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE rs.user = ?1 AND rs.passengerEnum = 'DRIVER' AND r.dateRoute <= ?2 AND r.canceled = false")
    List<Route> findAllPastByUserAsDriverAndIsNotCanceled(User user, LocalDateTime date);

    // Find all routes after specific date by userId WHERE user is driver AND route is not canceled
    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE rs.user = ?1 AND rs.passengerEnum = 'DRIVER' AND r.dateRoute > ?2 AND r.canceled = false")
    List<Route> findAllFutureRoutesByUserIdAsDriverAndIsNotCanceled(User user, LocalDateTime date);

    // Find all routes past specific date by userId WHERE user is passenger AND route is not canceled
    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE rs.user = ?1 AND rs.passengerEnum = 'PASSENGER' AND r.dateRoute <=?2 AND r.canceled = false")
    List<Route> findAllByUserIdAsPassengerAndIsNotCanceled(User user, LocalDateTime date);

    // Find all routes after specific date by userId WHERE user is passenger AND route is not canceled
    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE rs.user = ?1 AND rs.passengerEnum = 'PASSENGER' AND r.dateRoute > ?2 AND r.canceled = false")
    List<Route> findAllFutureRoutesByUserIdAsPassengerAndIsNotCanceled(User user, LocalDateTime now);

    // Find all routes after specific date and time WHERE passed "userId" is not a driver
    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE r.dateRoute > ?1 AND rs.passengerEnum = 'DRIVER' AND rs.user.id <> ?2 ORDER BY r.id DESC")
    List<Route> findAllFutureRoutesExcludingInWhichUserIsDriver(LocalDateTime now, String userId);

    // Find all routes before specific date and time WHERE passed "userId" is a driver AND route is not canceled AND is ordered descending by Route.id
    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE r.dateRoute <= ?1 AND rs.passengerEnum = 'DRIVER' AND rs.user.id = ?2 AND r.canceled = false ORDER BY r.id DESC")
    List<Route> findAllPastOrderByIdDescAsDriverAndIsNotCanceled(LocalDateTime now, String userId);

    // Find all routes before specific date and time WHERE passed "userId" is a driver AND route is not canceled AND is ordered ascending by Route.id
    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE r.dateRoute <= ?1 AND rs.passengerEnum = 'DRIVER' AND rs.user.id = ?2 AND r.canceled = false ORDER BY r.id ASC")
    List<Route> findAllPastOrderByIdAscAsDriverAndIsNotCanceled(LocalDateTime now, String userId);

    // Find all routes before specific date and time WHERE passed "userId" is a passenger AND route is not canceled AND is ordered descending by Route.id
    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE r.dateRoute <= ?1 AND rs.passengerEnum = 'PASSENGER' AND rs.user.id = ?2 AND r.canceled = false ORDER BY r.id DESC")
    List<Route> findAllPastOrderByIdDescAsPassengerAndIsNotCanceled(LocalDateTime now, String userId);

    // Find all routes before specific date and time WHERE passed "userId" is a passenger AND route is not canceled AND is ordered ascending by Route.id
    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE r.dateRoute <= ?1 AND rs.passengerEnum = 'PASSENGER' AND rs.user.id = ?2 AND r.canceled = false ORDER BY r.id ASC")
    List<Route> findAllPastOrderByIdAscAsPassengerAndIsNotCanceled(LocalDateTime now, String userId);

    // Find all routes where passed used is driver AND route is not canceled
    @Query(value = "SELECT r FROM Route r JOIN RouteStop rs ON r.id = rs.routeId WHERE rs.user = ?1 AND rs.passengerEnum = 'DRIVER' AND r.canceled = false")
    List<Route> findAllByUserIdAsDriver(User user);

    // Find all routes where Route.carId == "carId" AND Route.dateRoute is after "dateRoute" AND Route.canceled == "canceled"
    List<Route> findAllByCarIdAndDateRouteAfterAndCanceledEquals(String carId, LocalDateTime dateRoute, Boolean canceled);
}
