package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;

public interface RoutePagingAndSortingRepository extends PagingAndSortingRepository<Route, Integer> {

    // TODO: tests
    Page<Route> findAllByDateRouteAfter(LocalDateTime dateRoute, Pageable pageable);

    Page<Route> findAllByDateRouteGreaterThanAndCanceledEqualsAndCar_UserIdIsNot(LocalDateTime dateRoute, Boolean canceled, String car_userId, Pageable pageable);

    Page<Route> findAllByDateRouteBetweenAndCanceledEqualsAndCar_UserIdIsNot(LocalDateTime dateRoute, LocalDateTime dateRoute2, Boolean canceled, String car_userId, Pageable pageable);

    Page<Route> findAllByDateRouteBetweenAndCanceledEqualsAndOfficeDirectionEqualsAndCar_UserIdIsNotAndOfficeAddressIdEquals(LocalDateTime dateRoute, LocalDateTime dateRoute2, Boolean canceled, Boolean officeDirection, String car_userId, String officeAddressId, Pageable pageable);

}