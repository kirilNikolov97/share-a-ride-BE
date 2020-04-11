package com.knikolov.sharearide.repository;

import com.knikolov.sharearide.models.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;

public interface RoutePagingAndSortingRepository extends PagingAndSortingRepository<Route, Integer> {

    Page<Route> findAllByDateRouteAfter(LocalDateTime dateRoute, Pageable pageable);

    Page<Route> findAllByDateRouteGreaterThanAndCanceledEquals(LocalDateTime dateRoute, Boolean canceled, Pageable pageable);

    Page<Route> findAllByDateRouteBetweenAndCanceledEquals(LocalDateTime dateRoute, LocalDateTime dateRoute2, Boolean canceled, Pageable pageable);

    Page<Route> findAllByDateRouteBetweenAndCanceledEqualsAndOfficeDirectionEquals(LocalDateTime dateRoute, LocalDateTime dateRoute2, Boolean canceled, Boolean officeDirection, Pageable pageable);

}