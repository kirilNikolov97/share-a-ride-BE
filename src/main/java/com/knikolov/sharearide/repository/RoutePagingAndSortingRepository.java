package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;

/**
 * Repository for Route entity that includes paging and sorting functionality
 */
public interface RoutePagingAndSortingRepository extends PagingAndSortingRepository<Route, Integer> {

    // Find all routes by date greater than "dateRoute" AND Route.canceled == "canceled" AND Route.car.userId != "carUserId"
    Page<Route> findAllByDateRouteGreaterThanAndCanceledEqualsAndCar_UserIdIsNot(LocalDateTime dateRoute, Boolean canceled, String carUserId, Pageable pageable);

    // Find all routes where Route.dateRoute is between "start" and "end" AND Route.canceled == "canceled" AND Route.car.userId != "carUserId"
    Page<Route> findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(LocalDateTime start, LocalDateTime end, Boolean canceled, String carUserId, Pageable pageable);

    // Find all routes where Route.dateRoute is between "start" and "end" AND Route.canceled == "canceled" AND Route.officeDirection == "officeDirection" AND Route.car.userId != "carUserId" AND Route.officeAddressId == "officeAddressId"
    Page<Route> findAllByDateRouteBetweenAndCanceledEqualsAndOfficeDirectionEqualsAndCar_UserIdIsNotAndOfficeAddressIdEquals(LocalDateTime start, LocalDateTime end, Boolean canceled, Boolean officeDirection, String carUserId, String officeAddressId, Pageable pageable);
}